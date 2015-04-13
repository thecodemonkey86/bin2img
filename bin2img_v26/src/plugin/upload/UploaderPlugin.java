package plugin.upload;

import init.Bin2ImgInit;

import java.io.IOException;
import java.nio.file.Path;

import plugin.upload.convert.UploadScript;
import util.exception.CancelException;

import converter.Bin2ImgModel;

import jbiu.init.JBIU;
import jbiu.io.login.LoginManager;
import jbiu.model.ConfigModel;

public class UploaderPlugin extends JBIU {
	
	public static final String
		SETTING_SPLIT_UNIT="splitUnit",
		SETTING_SPLIT_VALUE="splitValue",
		SETTING_HOST="host",
		SETTING_FILELIST_COL_WIDTH="fileListColWidth",
		SETTING_FILELIST_COL_POS="fileListColPos",
		SETTING_FILELIST_SORT_COL="fileListSortCol",
		SETTING_FILELIST_SORT_MODE="fileListSortMode",
		SETTING_AUTO_PARAMS="autoParams",
		SETTING_FILELIST_DIVIDER_LOCATION="fileListDividerLocation",
		SETTING_MAIN_DIVIDER_LOCATION="mainDividerLocation",
		SETTING_BOTTOM_DIVIDER_LOCATION="bottomDividerLocation",
		SETTING_SPLIT_ENABLED="splitEnabled",
		SETTING_MULTIPLE_SCRIPTS="multipleScripts",
		SETTING_KEEP_IMAGES="keepImages",
		SETTING_COMPRESS_SCRIPT="compressScript",
		SETTING_MAX_SCRIPTS="maxSimultScripts",
		SETTING_LAST_PATH="lastPath",
		SETTING_SAVE_SCRIPT_TO_DISK="saveToLocalDisk",
		SETTING_SAVE_LINKS="saveLinks",
		SETTING_SAVE_SCRIPT_PATH="scriptOutputPath",
		SETTING_SAVE_LINKS_PATH="saveLinksPath"
	;
	
	public static final String
		ICON_START_STOP="startStopIcon",
		ICON_ENQUEUE="enqueueIcon",
		ICON_DELETE="deleteIcon",
		ICON_REMOVE_FINISHED="removeFinishedIcon",
		ICON_LINK_COLLECTOR="linkCollectorIcon"
		;
	
	public UploaderPlugin() throws IOException {
		super("plugin_cfg/upload/","jbiu_settings");
	}
	
	public static Path getSessionPath(){
		return Bin2ImgModel.getModel().getSessionPath("upload");
	}
	
	public static Path getSessionPath(UploadScript s){
		return Bin2ImgModel.getModel().getSessionPath("upload").resolve(s.getName());
	}
	
	public void logoutAll(){
		try {
			LoginManager.performLogout(ConfigModel.getAllConfigs());
		} catch (CancelException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void setAdditionalDefaultSettings() {
		settings.set(SETTING_LAST_PATH, Bin2ImgInit.getInstance().getExecPath());
	}
//		settings.set(SETTING_AUTO_PARAMS, false);
//		settings.set(SETTING_SPLIT_UNIT, 0);
//		settings.set(SETTING_SPLIT_VALUE, 0);
//		settings.set(SETTING_SPLIT_ENABLED, false);
//		settings.set(SETTING_FILELIST_COL_WIDTH,new int[]{75,75,75});		
//	}
}
