/*	This program is released under European Union Public License.
 *  for details please refer to the file LICENSE
 *   
 *  Disclaimer: the program comes "as is" and with absolutely no warranty 
 *  
 *  © 2010,2011 TCM
 */

package converter.gui;

import init.Bin2ImgInit;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import plugin.download.DownloaderPlugin;
import plugin.download.gui.ScriptProcessorTab;
import plugin.gui.IfBin2imgWindow;
import plugin.gui.PluginGuiManager;
import plugin.upload.UploaderPlugin;
import plugin.upload.gui.UploadPluginGUI;

import settings.Settings;
import settings.SettingsLoadable;
import singleInstance.SingleInstanceHandler;
import util.exception.CancelException;
import converter.Bin2ImgModel;
import converter.gui.settings.NewSettingsDialog;
import gui.filetree.FileTreeNode;
import gui.settings.GUISettingsManager;
import gui.settings.SettingsFrame;
import gui.util.buttonhandler.ButtonHandler;
import gui.util.lookandfeel.LAFMenuRestart;

import static init.Bin2ImgInit.rm;
import static converter.ConverterSettings.*;

public class ConverterGUI extends SettingsFrame implements SingleInstanceHandler, SettingsLoadable, IfBin2imgWindow {
	private static final long serialVersionUID = 1L;
	public static final String LOCALE_VIEW_IMG="viewImg"
							,LOCALE_VIEW_BIN="viewBin"
							,LOCALE_VIEW_UP="viewUpload"
							, LOCALE_VIEW_DOWN="viewDownload"
	;
	
//	private SettingsTabbedPane tabs;

	public static final String 
		VIEW_IMG="viewImg",
		VIEW_BIN="viewBin",
		VIEW_UP="viewUpload",
		VIEW_DOWN="viewDownload"
	;
	
	private HashMap<String, ConverterView> views;
	
//	private ScriptProcessorTab tabScriptProcessing;

	private JMenuBar mbar;
	private JMenu mFile, mView, mHelp;
	private JMenuItem mSettings, mExit, mAbout;
	private JRadioButtonMenuItem  mViewBin,mViewImg,mViewUp,mViewDown;
	private LAFMenuRestart lafMenu;
	private ButtonHandler viewHandler;
//	private UploadPluginGUI uploadPanel;
	private JPopupMenu trayPopup;

	private static final GUISettingsManager GSM = new GUISettingsManager();

	private SystemTray tray;
	private TrayIcon ti;
	private ConverterView currentView;
	private int laststate; 
	
	public ConverterGUI() {
		super("bin2img " + Bin2ImgInit.getInstance().getVersion(), "main");
		
		JPopupMenu.setDefaultLightWeightPopupEnabled(true);
		setIconImage(new ImageIcon(Bin2ImgInit.getInstance().getExecPath().resolve("bin2img_48x48.png").toString()).getImage());

		
		ActionListener alExit = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				exit();
			}
		};

		addWindowListener(new WindowListener() {
			
			@Override
			public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowDeiconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowDeactivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowClosing(WindowEvent e) {
				laststate=getExtendedState();
				setVisible(false);
				saveSettings(Bin2ImgInit.settings());
			}
			
			@Override
			public void windowClosed(WindowEvent e) {
				
			}
			
			@Override
			public void windowActivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		FileTreeNode.setEmpty(rm().getLocalizedString("emptyNode"));
		views=new HashMap<>(4);
		
		
		
		mbar = new JMenuBar();
		mFile = new JMenu(rm().getLocalizedString("menuFile"));
		JMenu mScript = new JMenu(rm().getLocalizedString("menuScripts"));
		JMenuItem mCreateScript = new JMenuItem(rm().getLocalizedString("menuNewScript"));
		JMenuItem mAddScript = new JMenuItem(rm().getLocalizedString("menuAddScript"));
		mScript.add(mAddScript);
		mScript.add(mCreateScript);
		mCreateScript.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					new ScriptCreatorGUI(null).show(null);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});

		mAddScript.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				showView(VIEW_DOWN);
				((ScriptProcessorTab) currentView.getComponent()).addScripts();
			}
		});
		
		
		lafMenu = new LAFMenuRestart(Bin2ImgInit.getInstance(),SETTING_LAF,rm().getLocalizedString("msgLaFRestart"));
		
		
		mExit = new JMenuItem(rm().getLocalizedString("menuExit"));
		mExit.addActionListener(alExit);
		createTray(alExit);
		mView = new JMenu(rm().getLocalizedString("menuView"));
		
		
		mHelp = new JMenu(rm().getLocalizedString("menuHelp"));
		mAbout = new JMenuItem(rm().getLocalizedString("menuAbout"));
		mAbout.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new AboutDialog().setVisible(true);
			}
		});
		mSettings = new JMenuItem(rm().getLocalizedString("menuSettings"));

		mbar.add(mFile);
		mbar.add(mScript);
		mbar.add(mView);
		mbar.add(mHelp);
		mFile.add(mSettings);
		PluginGuiManager.addMenus(mFile);
		mFile.add(mExit);
		mHelp.add(mAbout);
		setJMenuBar(mbar);
		mSettings.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					NewSettingsDialog sd = getSettingsDialog();
					sd.showDialog(0);
					Bin2ImgInit.getInstance().saveSettings();

				} catch (CancelException ce) {

				} catch (IOException ex) {
					JOptionPane.showMessageDialog(null, ex.getLocalizedMessage());
				}
			}
		});

		
		String tImg=rm().getLocalizedString(LOCALE_VIEW_IMG);
		String tBin=rm().getLocalizedString(LOCALE_VIEW_BIN);
		String tUp=rm().getLocalizedString(LOCALE_VIEW_UP);
		String tDown=rm().getLocalizedString(LOCALE_VIEW_DOWN);
		
		Settings s=Bin2ImgInit.settings();

		mViewImg=new ShowViewMenuItem(VIEW_IMG, tImg);
		mViewBin=new ShowViewMenuItem(VIEW_BIN,tBin);
		mViewUp=new ShowViewMenuItem(VIEW_UP,tUp);
		mViewDown=new ShowViewMenuItem(VIEW_DOWN,tDown);
		viewHandler=new ButtonHandler(mViewImg,mViewBin,mViewUp,mViewDown);
		
		mView.add(mViewImg);
		mView.add(mViewBin);
		mView.add(mViewUp);	
		mView.add(mViewDown);
		mView.add(new javax.swing.JSeparator());
		mView.add(lafMenu);
		
		views.put(VIEW_IMG, new ConverterView(VIEW_IMG,mViewImg,Bin2ImgInit.settings()){
			@Override
			public void createComp() {
				comp=new ToImgPanel();
			}
		});
		views.put(VIEW_BIN, new ConverterView(VIEW_BIN,mViewBin,Bin2ImgInit.settings()){
			@Override
			public void createComp() {
				comp=new ToBinPanel();
			}
		});
		views.put(VIEW_UP, new ConverterView(VIEW_UP,mViewUp,UploaderPlugin.getInstance().getSettings()){
			@Override
			public void createComp() {
				comp=new UploadPluginGUI(ConverterGUI.this);
			}
		});
		views.put(VIEW_DOWN, new ConverterView(VIEW_DOWN,mViewDown,DownloaderPlugin.settings()){
			@Override
			public void createComp() {
				comp=new ScriptProcessorTab(ConverterGUI.this);
			}
			
		});
		
		loadSettings(s);
		setVisible(true);
	}
	
	private void createTray(ActionListener alExit) {
		if (SystemTray.isSupported()) {
			tray = SystemTray.getSystemTray();
			Dimension d = tray.getTrayIconSize();
			ti = new TrayIcon(getIconImage().getScaledInstance(d.width, d.height, Image.SCALE_DEFAULT));

			trayPopup = new JPopupMenu();
			JMenuItem mnuShow = getTrayMenuItem("Show window");
			JMenuItem mnuPopupExit = getTrayMenuItem(rm().getLocalizedString("menuExit"));
			mnuShow.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					setVisible(true);
					trayPopup.setVisible(false);
				}
			});
			
			trayPopup.add(mnuShow);
			trayPopup.add(mnuPopupExit);
			ti.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					showPopup(e);
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					showPopup(e);
					if (e.getButton()==MouseEvent.BUTTON1){
						setVisible(!isVisible());
						setExtendedState(laststate);
						trayPopup.setVisible(false);
					}
				}

				private void showPopup(MouseEvent e) {
					if (e.isPopupTrigger()) {
						trayPopup.show(e.getComponent(), e.getX(), e.getY());
					}  
				}
			});
			mnuPopupExit.addActionListener(alExit);
			try {
				tray.add(ti);

			} catch (AWTException e2) {
				e2.printStackTrace();
				throw new RuntimeException(e2);
			}
		} else {

		}
	}

	// public void refreshFileSystem() {
	// toBinPanel.refreshFileSystem();
	// toImgPanel.refreshFileSystem();
	// }

	public void addAboutDialog(String menuCaption, final JDialog aboutDialog) {
		JMenuItem mAboutPlugin = new JMenuItem(menuCaption);
		mAboutPlugin.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				aboutDialog.setVisible(true);
			}
		});

		mHelp.add(mAboutPlugin);
	}

	@Override
	public void passArguments(String[] args) {
		if (args.length>0){
			((ScriptProcessorTab) views.get(VIEW_DOWN).getComponent()).addScripts(args);
			viewHandler.setSelectedButton(mViewDown);
			showView(VIEW_DOWN);
		}
	}


	@Override
	public void loadSettings(Settings settings) {
		super.loadSettings(settings);
		GSM.loadSettings(settings);
		showView(settings.getString(SETTING_LAST_VIEW));
		viewHandler.setSelectedButton(currentView.getBtn());
		try {
			DownloaderPlugin.getInstance().loadSettings();
			UploaderPlugin.getInstance().loadSettings();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void saveSettings(Settings settings) {
		super.saveSettings(settings);
		GSM.saveSettings(settings);
		currentView.saveSettings();
		settings.set(SETTING_LAST_VIEW, currentView.toString());
		try {
			DownloaderPlugin.getInstance().saveSettings();
			((UploaderPlugin) UploaderPlugin.getInstance()).logoutAll();
			UploaderPlugin.getInstance().saveSettings();
			Bin2ImgInit.getInstance().saveSettings();
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getLocalizedMessage());
		}
	}

	@Override
	public JMenu getViewMenu() {
		return mView;
	}

	@Override
	public NewSettingsDialog getSettingsDialog() {
		NewSettingsDialog sd = new NewSettingsDialog();
		((UploadPluginGUI) getViewComponent(VIEW_UP)).addSettingsPages(sd);
		((ScriptProcessorTab) getViewComponent(VIEW_DOWN)).addSettingsPages(sd);
		return sd;
	}

	private void exit() {
		tray.remove(ti);
		saveSettings(Bin2ImgModel.getModel().getSettings());
		System.exit(0);
	}
	
	private MouseListener trayMenuItemListner=new MouseAdapter() {

        @Override
        public void mouseEntered(MouseEvent e) {
            ((JMenuItem) e.getSource()).setArmed(true);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            ((JMenuItem) e.getSource()).setArmed(false);
        }
    }; 
	
	private JMenuItem getTrayMenuItem(String text){
		JMenuItem item=new JMenuItem(text);
		item.addMouseListener(trayMenuItemListner);
		return item;
	}
	
	private void showView(String name){
		if (currentView!=null) currentView.saveSettings();
		ConverterView v=views.get(name);
		if (currentView!=v){
			currentView=v;
			setContentPane(currentView.getComponent());
			validate();
		}
	}
	
	private class ShowViewMenuItem extends JRadioButtonMenuItem{
		private static final long serialVersionUID = 6755623740676353377L;

		public ShowViewMenuItem(final String name,String text) {
			super(text);
			addActionListener(new ActionListener() {
				

				@Override
				public void actionPerformed(ActionEvent e) {
					showView(name);
				}
			});
		}
		
	}
	
	private JComponent getViewComponent(String name){
		return views.get(name).getComponent();
	}

}