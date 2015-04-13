/*	This program is released under European Union Public License.
 *  for details please refer to the file LICENSE
 *   
 *  Disclaimer: the program comes "as is" and with absolutely no warranty 
 *  
 *  © 2010,2011 TCM
 */

package plugin.download.gui;

import static init.JID2.rm;
import static init.JID2.settings;
import gui.util.GuiUtil;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import model.DownloadManager;
import plugin.Script;
import plugin.download.SessionLoadException;
import plugin.download.convert.DownloadScript;
import plugin.download.convert.ScriptManager;
import plugin.download.script.DownloadScriptIO;
import plugin.gui.IPluginSettingsDialog;
import plugin.gui.IfBin2imgWindow;
import settings.Settings;
import settings.SettingsLoadable;
import util.exception.CancelException;
import converter.Bin2ImgModel;
import converter.gui.settings.NewSettingsDialog;

public class ScriptProcessorTab extends JPanel implements SettingsLoadable,IPluginSettingsDialog {

	private static final long serialVersionUID = 1L;
	private JTabbedPane tabs;
	private OverviewTab overviewTab;
	private IfBin2imgWindow window;
	
	public ScriptProcessorTab(IfBin2imgWindow window) {
		this.window=window;
		tabs = new JTabbedPane();
		overviewTab = new OverviewTab(this);
		tabs.addTab(rm().getLocalizedString("tabOverview"), overviewTab);
		setLayout(new BorderLayout());
		if (settings().getBoolean("downloadManagerIcons"))
			add(getIconToolbar(), BorderLayout.NORTH);
		else 
			add(getButtonPanel(), BorderLayout.NORTH);
		add(tabs, BorderLayout.CENTER);
		loadSessions();
	}

	public void addScripts() {
			
		final DownloadChooser dc = new DownloadChooser();
		try {
			dc.showDialog();
			if (dc.getOption()==DownloadChooser.TAB_FILE) {
				Path[] files=dc.getSelectedFiles();
				if (files!=null) {
					for (final Path f : files) {
						new SwingWorker<Void, Void>() {
							@Override
							protected Void doInBackground() throws Exception {
								loadScript(DownloadScriptIO.read(f),true);
								return null;
							}
						}.execute();
						
					}
				}
			} else if (dc.getOption()==DownloadChooser.TAB_URL) {
				final FetchDownloadMessage fetch=new FetchDownloadMessage((Window) window);
				final List<String> urls=dc.getUrls();
				fetch.showMessage(urls);
				
				new SwingWorker<Void, Void>() {
					List<DownloadScript> scripts;
					Exception e;
					
					@Override
					protected Void doInBackground()  {
						scripts=new ArrayList<>(urls.size());
						try {
							for(String url:urls) {
								DownloadScript s=DownloadScriptIO.read(url);
								scripts.add(s);
								ScriptManager.getManager().addScript(s);
							}
						} catch (Exception e) {
							this.e = e;
						}
						return null;
					}
					
					protected void done() {
						fetch.close();
						if (e == null) {	
							for(DownloadScript s:scripts)
								ScriptManager.getManager().start(s);
						} else {
							e.printStackTrace();
							String err=e.getMessage();
							if (err == null || err.isEmpty()) err="Unknown I/O error";
							JOptionPane.showMessageDialog(null, err ,"Error",JOptionPane.ERROR_MESSAGE);
						}
					}
				}.execute();
				
			}
		} catch (CancelException e) {
		}
	}
	
	
	public void addScripts(final String[] locations){
		final FetchDownloadMessage fetch=new FetchDownloadMessage((Window) window);
		new SwingWorker<Void, String>() {
			protected Void doInBackground() throws Exception {
				for (String l:locations){
					process(Arrays.asList(l));
					addScript(l); 
				}
				
				return null;
			}
			
			@Override
			protected void process(List<String> chunks) {
				fetch.showMessage(chunks);
			}
			
			protected void done() {
				fetch.close();
			}
		}.execute();
	}
	
	public void addScript(String location) throws IOException, CancelException, SessionLoadException{
		if (location.startsWith("b2i://")){
			loadScript(DownloadScriptIO.read(location),true);
		} else {
			loadScript(DownloadScriptIO.read(Paths.get(location)),true);
		}
	}
	
	private void loadScript(DownloadScript s,boolean start) throws IOException, CancelException {
		ScriptManager.getManager().addScript(s);
		if (start) ScriptManager.getManager().start(s);
	}
	
	public void loadSessions() {
		List<DownloadScript> scripts;
		try {
			scripts = DownloadScriptIO.loadDownloadSessions();
			if (scripts.size() > 0) {
				for (DownloadScript s : scripts) {
					loadScript(s, s.autoResume());
				}
			}
//		} catch (InvalidSessionsException ioex) {
//			ioex.printStackTrace();
//			int res=JOptionPane.showOptionDialog(this, "Found invalid download sessions. Delete?", "Error", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, null, JOptionPane.YES_OPTION);
//			
//			if (res==JOptionPane.YES_OPTION){
//				try {
//					Iterator<DownloadScript> it=ioex.getScripts();
//					while (it.hasNext())
//						DownloadScriptIO.clearSession(it.next());
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//			System.out.println("test");
		} catch (IOException ioex) {
			ioex.printStackTrace();
		} catch (CancelException e) {
			e.printStackTrace();
		}
	}

	public void removeTab(DownloadScript script) {
		for (int i = 1; i < tabs.getTabCount(); i++) {
			ScriptDownloadPanel t = (ScriptDownloadPanel) tabs.getComponentAt(i);
			if (t.getScript() == script) {
				tabs.removeTabAt(i);
				return;
			}
		}
	}

	private JPanel getButtonPanel() {
		JButton bAdd, bStartStopAll, bStartStop,  bRemoveFinished;
		bAdd = new JButton(rm().getLocalizedString("scriptNewDownload"));
		bAdd.addActionListener(getAddScriptActionListener());

		bStartStopAll = new JButton(rm().getLocalizedString("scriptStartStopAll"));
		bStartStopAll.addActionListener(getStartStopAllActionListener());

		bRemoveFinished=new JButton(rm().getLocalizedString("scriptRemoveFinished"));
		bRemoveFinished.addActionListener(getRemoveFinishedActionListener());
		
		bStartStop=new JButton(rm().getLocalizedString("scriptStartStop"));
		bStartStop.addActionListener(getStartStopActionListener());
		
		JPanel bPanel = new JPanel();
		bPanel.setLayout(new GridLayout(1, 5));
		bPanel.add(bAdd);
		bPanel.add(bStartStopAll);
		bPanel.add(bStartStop);
		bPanel.add(bRemoveFinished);
		return bPanel;
	}
	
	private ActionListener getRemoveFinishedActionListener() {
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				overviewTab.removeFinished();
			}
		};
	}
	
	private ActionListener getAddScriptActionListener() {
		return new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				addScripts();
			}
		};
	}
	
	private ActionListener getStartStopAllActionListener() {
		return new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				ScriptManager.getManager().startStopAll();
			}
		};
	}
	
	private ActionListener getStartStopActionListener() {
		return new ActionListener() {
		
			@Override
			public void actionPerformed(ActionEvent e) {
				overviewTab.startStopSelected();
			}
		};
	}
	
	private JToolBar getIconToolbar() {
		JToolBar tb=new JToolBar();
		tb.setFloatable(false);
		
		JButton bAdd=new JButton(rm().getIcon("addIcon"));
		bAdd.setToolTipText(rm().getLocalizedString("scriptNewDownload"));
		bAdd.addActionListener(getAddScriptActionListener());
		
		JButton bStartStop=new JButton(rm().getIcon("startStopIcon"));
		bStartStop.setToolTipText(rm().getLocalizedString("scriptStartStop"));
		bStartStop.addActionListener(getStartStopActionListener());
		
		JButton bStartStopAll=new JButton(rm().getIcon("startStopAllIcon"));
		bStartStopAll.setToolTipText(rm().getLocalizedString("scriptStartStopAll"));
		bStartStopAll.addActionListener(getStartStopAllActionListener());
		
		JButton bRemoveFinished=new JButton(rm().getIcon("removeFinishedIcon"));
		bRemoveFinished.setToolTipText(rm().getLocalizedString("scriptRemoveFinished"));
		bRemoveFinished.addActionListener(getRemoveFinishedActionListener());
		
		JButton bExplorer=new JButton(rm().getIcon("browserIcon"));
		bExplorer.setToolTipText(rm().getLocalizedString("scriptBrowser"));
		bExplorer.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				GuiUtil.openBrowser(Bin2ImgModel.getModel().getOutputBin());		
			}
		});
		
		tb.add(bAdd);
		tb.add(bStartStopAll);
		tb.add(bStartStop);
		tb.add(bRemoveFinished);
		tb.add(bExplorer);
		return tb;
	}

	public void updateUITree() {
		SwingUtilities.updateComponentTreeUI(this);
		overviewTab.updateUITree();
	}

	
	@Override
	public void loadSettings(Settings settings) {
		overviewTab.loadSettings(settings);
	}

	@Override
	public void saveSettings(Settings settings) {
		overviewTab.saveSettings(settings);
		
		if (tabs.getTabCount()>1){
			ScriptDownloadPanel p=(ScriptDownloadPanel) tabs.getComponentAt(1);
			p.saveSettings(settings);
		}
	}

	public void addTab(Script info, DownloadManager dm){
		ScriptDownloadPanel p=new ScriptDownloadPanel(dm, info);
		tabs.addTab(info.getName(),p );
	}
	
	@Override
	public void addSettingsPages(NewSettingsDialog sd) {
		/*ResourceManager rm=DownloaderPlugin.rm();
		Settings settings=DownloaderPlugin.settings();
		MultiTabSettingsPage multi=new MultiTabSettingsPage("Download", rm, settings);
		multi.addPage(new DownloadSettingsPage("Download"));
		sd.addPage(multi);*/
		sd.addPage(new DownloadSettingsPage("Download"));
	}
}
