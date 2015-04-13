package plugin.upload.gui;


import jbiu.init.JBIU;
import plugin.upload.UploaderPlugin;
import settings.Settings;
import gui.layout.percent2.APConstraint;
import gui.layout.percent2.AnchorPercentLayout;
import gui.resource.ResourceManager;
import gui.settings.dialog.component.CheckboxSettingsComponent;
import gui.settings.dialog.component.PathSettingsComponent;
import gui.settings.dialog.group.CheckboxSettingsGroup;
import gui.settings.dialog.group.SettingsComponentGroup;
import gui.settings.dialog.group.ThreadsSettingsGroup;
import gui.settings.dialog.page.SettingsPage;

public class GeneralUploadSettingsPage extends SettingsPage {
	private static final long serialVersionUID = 7394645428646205357L;

	public GeneralUploadSettingsPage() {
		super(UploaderPlugin.rm().getLocalizedString("settingTabGeneral"),UploaderPlugin.getInstance());
	}

	@Override
	protected void createComponentGroups(Settings s,ResourceManager rm) {
		setLayout(new AnchorPercentLayout());
		addSettingsComponentGroup(new ThreadsSettingsGroup(rm.getLocalizedString("settingSimultScripts"),UploaderPlugin.SETTING_MAX_SCRIPTS, rm),APConstraint.constraint().left(0).rightPercent(50).top(0).heightPercent(31));
		addSettingsComponentGroup(new ThreadsSettingsGroup(rm.getLocalizedString("settingThreadsPerScript"),JBIU.SETTING_THREADS, rm),APConstraint.constraint().leftPercent(50).right(0).top(0).heightPercent(31));
		addSettingsComponentGroup(new CheckboxSettingsGroup(rm.getLocalizedString("settingTitleImages"),rm.getLocalizedString("settingKeepImages"), UploaderPlugin.SETTING_KEEP_IMAGES, rm),APConstraint.linearV(20,34));
		addSettingsComponentGroup(new SettingsComponentGroup(rm.getLocalizedString("settingTitleDLScripts"), rm) {
			private static final long serialVersionUID = 4097024618653453195L;

			@Override
			protected void createSettingsComponents(ResourceManager rm) {
				setLayout(new AnchorPercentLayout());
				addSettingsComponent(new CheckboxSettingsComponent(UploaderPlugin.SETTING_SAVE_SCRIPT_TO_DISK,rm.getLocalizedString("settingSaveDLScript")),
						APConstraint.constraint().left(10).topPercent(15)
				);				
				addSettingsComponent(new PathSettingsComponent(UploaderPlugin.SETTING_SAVE_SCRIPT_PATH, null),
						APConstraint.constraint().leftPercent(50).right(10).topPercent(15));
				addSettingsComponent(new CheckboxSettingsComponent("uploadScript", rm.getLocalizedString("settingCreateLink")),
						APConstraint.constraint().left(10).topPercent(40)
				);
				
				/*addSettingsComponent(new CheckboxSettingsComponent(UploaderPlugin.SETTING_SAVE_LINKS,rm.getLocalizedString("settingSaveLinks")),
						APConstraint.constraint().left(10).topPercent(65)
				);				
				addSettingsComponent(new FileSettingsComponent(UploaderPlugin.SETTING_SAVE_LINKS_PATH, null),
						APConstraint.constraint().leftPercent(50).right(10).topPercent(65));
				*/
//				addSettingsComponent(new CheckboxSettingsComponent(UploaderPlugin.SETTING_COMPRESS_SCRIPT, "Compress + encode Base64"),
//						APConstraint.constraint().left(10).topPercent(65)
//				);
			}
			
		},APConstraint.linearV(43,57));
	}

}
