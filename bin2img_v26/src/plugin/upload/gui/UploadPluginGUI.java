package plugin.upload.gui;

import gui.settings.GUISettingsManager;
import gui.settings.SettingsTabbedPane;
import gui.settings.dialog.page.MultiTabSettingsPage;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;

import converter.gui.settings.NewSettingsDialog;

import jbiu.gui.AccountManagerPanel;
import jbiu.gui.GUIConfigLoader2;
import jbiu.model.UploadManager;
import plugin.Script;
import plugin.gui.IPluginSettingsDialog;
import plugin.gui.IfBin2imgWindow;
import plugin.upload.UploaderPlugin;
import plugin.upload.convert.ScheduledScriptManager;
import settings.Settings;
import settings.SettingsLoadable;

import static plugin.upload.UploaderPlugin.*;

public class UploadPluginGUI extends JPanel  implements SettingsLoadable,IPluginSettingsDialog{

	private static final long serialVersionUID = -4654160706611111920L;

	private ToImageSelectionTab2 selectionTab;
	private OverviewTab overviewTab;
	private SettingsTabbedPane tabs;
	public static final GUISettingsManager GSM = new GUISettingsManager(); 
	
	public UploadPluginGUI(IfBin2imgWindow parentWindow) {
		LinkCollectorGUI.initLinkCollectorGUI();
		tabs=new SettingsTabbedPane("main");
		GSM.addSettingsComponent(tabs);
		tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		selectionTab = new ToImageSelectionTab2(parentWindow);
		overviewTab=new OverviewTab(this);
		setLayout(new BorderLayout());
		add(getIconToolbar(),BorderLayout.NORTH);
		add(tabs,BorderLayout.CENTER);
		tabs.addTab(rm().getLocalizedString("tabSelectFiles"), selectionTab);
		tabs.addTab(rm().getLocalizedString("tabOverview"), overviewTab);
	}

	public void addTab(Script info, UploadManager tm) {
		tabs.addTab(info.getName(), new ConvertUploadPanel(tm,info));
	}
	public void removeTab(Script info) {
		for (int i=0;i<tabs.getTabCount();i++) {
			if (tabs.getTitleAt(i).equals(info.getName())) {
				tabs.removeTabAt(i);
				return;
			}
		}
	}
	
	private JToolBar getIconToolbar() {
		JToolBar tb=new JToolBar();
		tb.setFloatable(false);
		JButton  bStartStopAll=new JButton(rm().getIcon(ICON_START_STOP));
		bStartStopAll.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				overviewTab.startStopAll();
			}
		});
		bStartStopAll.setToolTipText(rm().getLocalizedString("tooltipStartStop"));
		JButton  bEnqueue=new JButton(rm().getIcon(ICON_ENQUEUE));
		bEnqueue.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				overviewTab.startStopSelected();
			}
		});
		bEnqueue.setToolTipText(rm().getLocalizedString("tooltipEnqueue"));
		
		JButton bDelete=new JButton(rm().getIcon(ICON_DELETE));
		bDelete.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				overviewTab.removeSelectedScript();
			}
		});
		bDelete.setToolTipText(rm().getLocalizedString("tooltipDelete"));
		JButton bRemoveFinished=new JButton(rm().getIcon(ICON_REMOVE_FINISHED));
		bRemoveFinished.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				overviewTab.removeFinished();
			}
		});
		bRemoveFinished.setToolTipText(rm().getLocalizedString("tooltipRemoveFinished"));
		JButton bLinkCollector=new JButton(rm().getIcon(ICON_LINK_COLLECTOR));
		bLinkCollector.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				LinkCollectorGUI.getInstance().displayLinks();
			}
		});
		bLinkCollector.setToolTipText(rm().getLocalizedString("tooltipLinkCollector"));
		tb.add(bStartStopAll);
		tb.add(bEnqueue);		
		tb.add(bDelete);
		tb.add(bRemoveFinished);
		tb.add(bLinkCollector);		
		return tb;
	}
	
	public void saveSettings(Settings settings) {
		GSM.saveSettings(settings);
		selectionTab.saveSettings(settings);
	}

	@Override
	public void loadSettings(Settings s) {		
		loadSessions();		
		GSM.loadSettings(s);
	}

	public void loadSessions() {
		
		try {
			ScheduledScriptManager.getManager().loadSessions();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void addSettingsPages(NewSettingsDialog sd) {
		
		MultiTabSettingsPage multi=new MultiTabSettingsPage("Upload", UploaderPlugin.getInstance());
		multi.addPage(new GeneralUploadSettingsPage());
		multi.addPage(new AdvancedUploadSettingsPage(rm().getLocalizedString("settingTabAdvanced"), UploaderPlugin.getInstance()));
		multi.addPage(new AccountManagerPanel(rm().getLocalizedString("settingTabLogin"),GUIConfigLoader2.getConfigs()));
		//multi.addPage(new HostsSettingPage(rm().getLocalizedString("settingTabHosts"), UploaderPlugin.getInstance()));
		sd.addPage(multi);
	}
	
	
}
