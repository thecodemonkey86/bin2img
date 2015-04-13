package plugin;


import init.Bin2ImgInit;

import java.io.IOException;
import java.util.Locale;

import plugin.download.DownloaderPlugin;
import plugin.gui.PluginGuiManager;
import plugin.upload.UploaderPlugin;
import util.exception.CancelException;

public class PluginManager {

	public static void load() throws IOException, CancelException {
		Locale l=Bin2ImgInit.getInstance().getSettings().getLocale("locale");
		DownloaderPlugin d= new DownloaderPlugin();
		d.getResourceManager().setLocale(l);
		UploaderPlugin u= new UploaderPlugin();
		u.getResourceManager().setLocale(l);
		PluginGuiManager.init(Bin2ImgInit.rm());
	}
	
	
}
