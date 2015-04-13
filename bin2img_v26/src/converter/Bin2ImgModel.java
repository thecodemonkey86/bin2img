package converter;


import java.nio.file.Path;

import settings.Settings;


public class Bin2ImgModel {

	private static Bin2ImgModel instance=new Bin2ImgModel();
	
	private Settings settings;
	private Path execPath;
	
	
	public Settings getSettings() {
		return settings;
	}

	public void setSettings(Settings settings) {
		this.settings = settings;
	}
	
	public void setExecPath(Path execPath) {
		this.execPath = execPath;
	}
	
	public static Bin2ImgModel getModel() {
		return instance;
	}
	

	public Path getOutputBin() {
		return toAbsPath(ConverterSettings.SETTING_OUTPUT_BIN);
	}
	
	public Path getOutputImg() {
		return toAbsPath(ConverterSettings.SETTING_OUTPUT_IMG);
		
//		System.out.println(System.getProperty("user.dir")+ " "+ p.toAbsolutePath());
	}
	
	private Path toAbsPath(String settingName){
		Path p=settings.getPath(settingName);
		if (!p.isAbsolute()) p=execPath.resolve(p);
		return p;
	}

//	public void openBrowser(String path) throws IOException {
//		String browser=settings.getString("browser");
//		if (browser!=null && browser.length()>0) {
//			Util.runFileBrowser(browser, path);
//			return true;
//		} else {
//			return false;
//		}
		
		
//	}

	
	public Path getSessionPath(String subdir) {
		return execPath.resolve("session").resolve(subdir);
	}	
}
