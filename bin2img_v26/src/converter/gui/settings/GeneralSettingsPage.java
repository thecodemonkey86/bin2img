package converter.gui.settings;

import converter.ConverterSettings;
import settings.Settings;
import init.Bin2ImgInit;
import gui.layout.percent2.APConstraint;
import gui.layout.percent2.AnchorPercentLayout;
import gui.resource.ResourceManager;
import gui.settings.dialog.component.InstancePortComponent;
import gui.settings.dialog.component.PathSettingsComponent;
import gui.settings.dialog.group.LanguageSettingsGroup;
import gui.settings.dialog.group.SettingsComponentGroup;
import gui.settings.dialog.page.SettingsPage;


public class GeneralSettingsPage extends SettingsPage {
		
	private static final long serialVersionUID = -545033579793243342L;

	public GeneralSettingsPage(String entry) {
		super(entry,Bin2ImgInit.getInstance());
		
	}
		
//	@Override
//	protected void setLayoutMgr(int componentCount) {
//		setLayout(new PercentageLayout());
//	}
	

	@Override
	protected void createComponentGroups(Settings s,ResourceManager rm) {
		setLayoutMgr(3);
		addSettingsComponentGroup( new LanguageSettingsGroup(Bin2ImgInit.getInstance().getLocales(), rm));
		addSettingsComponentGroup(new SettingsComponentGroup(rm.getLocalizedString("settingTitleOutPaths"), rm) {
			
			private static final long serialVersionUID = -7848065718204605940L;

			@Override
			protected void createSettingsComponents(ResourceManager rm) {
				setLayout(new AnchorPercentLayout());
				addSettingsComponent(new PathSettingsComponent(ConverterSettings.SETTING_OUTPUT_IMG, "PNG"),APConstraint.constraint().left(10).right(10).topPercent(10));
				addSettingsComponent(new PathSettingsComponent(ConverterSettings.SETTING_OUTPUT_BIN, rm.getLocalizedString("settingBinaryPath")),APConstraint.constraint().left(10).right(10).bottomPercent(10));
			}
		});
		addSingleComponent(rm.getLocalizedString("settingTitleInstancePort"), new InstancePortComponent(rm.getLocalizedString("settingInstancePort")),rm,null,APConstraint.constraint().widthPercent(40));
	}
	

}
