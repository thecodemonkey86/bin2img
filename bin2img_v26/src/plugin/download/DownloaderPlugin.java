/*	This program is released under European Union Public License.
 *  for details please refer to the file LICENSE
 *   
 *  Disclaimer: the program comes "as is" and with absolutely no warranty 
 *  
 *  © 2010,2011 TCM
 */

package plugin.download;

import init.Bin2ImgInit;
import init.JID2;

import java.io.IOException;
import java.nio.file.Path;

import converter.Bin2ImgModel;

import plugin.download.convert.ScriptManager;
import util.exception.CancelException;

public class DownloaderPlugin extends JID2{
	
	public static final String
		SETTING_MAX_SCRIPTS="maxSimultScripts",
		SETTING_LAST_PATH_DL_CHOOSER="lastPathDownloadChooser"
	;
	
	public DownloaderPlugin() throws IOException, CancelException {
		super("plugin_cfg/download/", "jid_settings");
	}
	
	@Override
	public void saveSettings() throws IOException {
		ScriptManager.getManager().stopAll();
		super.saveSettings();
	}
	
//	public void saveSessions() throws IOException {
//		ScriptManager.getManager().stopAll();
//	}
	
	public static Path getSessionPath(){
		return Bin2ImgModel.getModel().getSessionPath("download");
	}
	
	@Override
	protected void setAdditionalDefaultSettings() {
		settings.set(SETTING_LAST_PATH_DL_CHOOSER, Bin2ImgInit.getInstance().getExecPath());
		settings.set("maxTries", -1);
	}
}
