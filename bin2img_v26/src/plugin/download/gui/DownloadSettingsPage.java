package plugin.download.gui;

import java.awt.GridLayout;

import gui.resource.ResourceManager;
import gui.settings.dialog.group.ThreadsSettingsGroup;
import gui.settings.dialog.page.SettingsPage;
import init.JID2;
import plugin.download.DownloaderPlugin;
import settings.Settings;

public class DownloadSettingsPage extends SettingsPage {

	private static final long serialVersionUID = -1195762274616414971L;

	public DownloadSettingsPage(String entry) {
		super(entry,DownloaderPlugin.getInstance());
	}

	@Override
	protected void createComponentGroups(Settings s,ResourceManager rm) {
		setLayout(new GridLayout(2,1,0,20));
		addSettingsComponentGroup(new ThreadsSettingsGroup(rm.getLocalizedString("settingSimultanous"),DownloaderPlugin.SETTING_MAX_SCRIPTS, rm));
		addSettingsComponentGroup(new ThreadsSettingsGroup(rm.getLocalizedString("settingDlThreads"),JID2.SETTING_THREADS, rm));
	}

}
