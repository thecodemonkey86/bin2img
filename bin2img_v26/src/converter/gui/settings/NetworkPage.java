package converter.gui.settings;

import settings.Settings;
import init.Bin2ImgInit;
import gui.resource.ResourceManager;
import gui.settings.dialog.group.ProxySettingsGroup;
import gui.settings.dialog.group.TimeoutSettingsGroup;
import gui.settings.dialog.page.SettingsPage;

public class NetworkPage extends SettingsPage{

	private static final long serialVersionUID = 5204220343396872726L;

	public NetworkPage(String entry) {
		super(entry, Bin2ImgInit.getInstance());
	}

	@Override
	protected void createComponentGroups(Settings settings, ResourceManager rm) {
		setLayoutMgr(2);
		addSettingsComponentGroup(new ProxySettingsGroup(rm));
		addSettingsComponentGroup(new TimeoutSettingsGroup(rm));
	}
	

}
