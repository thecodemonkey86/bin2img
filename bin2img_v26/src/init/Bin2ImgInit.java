/*	This program is released under European Union Public License.
 *  for details please refer to the file LICENSE
 *   
 *  Disclaimer: the program comes "as is" and with absolutely no warranty 
 *  
 *  © 2010,2011 TCM
 */

package init;

import java.awt.EventQueue;
import java.io.IOException;

import plugin.PluginManager;
import converter.Bin2ImgModel;
import converter.ConverterSettings;
import converter.gui.ConverterGUI;

import gui.resource.ResourceManager;
import gui.util.lookandfeel.LAFMenuRestart;

import settings.Settings;
import singleInstance.SingleInstance;
import singleInstance.SingleInstanceHandler;
import util.exception.CancelException;
import core.io.Application;

public class Bin2ImgInit extends Application {
	
	private static Bin2ImgInit instance;
	
	public static Settings settings() {
		return instance.getSettings();
	}
	
	public static Bin2ImgInit getInstance() {
		return instance;
	}
	
	public static ResourceManager rm() {
		return instance.getResourceManager();
	}
	
	
	public String getVersion() {
		return "2.6 final";
	}
	
	public Bin2ImgInit() throws IOException {
		super("bin2img_settings");
		instance=this;
		Bin2ImgModel.getModel().setExecPath(execPath);
		Bin2ImgModel.getModel().setSettings(getSettings());
		try {
			PluginManager.load();
		} catch (CancelException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void setAdditionalDefaultSettings() {
		settings.set(ConverterSettings.SETTING_LAST_PATH_IMG, getExecPath());
		settings.set(ConverterSettings.SETTING_LAST_PATH_BIN, getExecPath());
		settings.set("outputImg", "output");
		settings.set("outputBin", "output");
	}

	public static void main(String[] args) throws IOException {
		new Bin2ImgInit();
		singleInstance(args);
		/*FilesChooser fc=new FilesChooser(null, "Test", true, "OK", "Cancel");
		fc.showOpenFilesDialog(null);
		List<ToImgInfo2> nfo= ImgInfoProvider.get(fc.getSelectedPaths(), null, 1024*1024);
		BasicToImg to=new BasicToImg();
		for (ToImgInfo2 i:nfo) {
			to.toImg(i);
		}*/
		//ScheduledScriptManager sm=new ScheduledScriptManager();
	}
	
	private static void singleInstance(final String[] args) {
		if (SingleInstance.isInstanceRunning(settings().getInt("port"))) {
			SingleInstance.sendArgsToInstance(args);
		} else {
			EventQueue.invokeLater(new Runnable() {

				@Override
				public void run() {
					LAFMenuRestart.setLookAndFeel(instance, ConverterSettings.SETTING_LAF);
					SingleInstanceHandler h=new ConverterGUI();
					SingleInstance.lock(h);
					h.passArguments(args);
				}
				
			});
		}
	}
		/*
		
		//new LinkCollectorGUI(null).setVisible(true);
		Bin2ImgModel.getInstance().init();
	
		if (args.length>0) {
			
			String userdir=System.getProperty("user.dir");
			int maxThreads=ConverterSettings.getSettings().getInt("ioThreads");
			ScheduledToBinConverter toBinConv=new ScheduledToBinConverter(maxThreads,Bin2ImgModel.getInstance().getOutputToBin(), false);
			ScheduledToImageConverter toImgConv=new ScheduledToImageConverter(Bin2ImgModel.getInstance().getOutputToImg(), maxThreads);
			File[] input;
			if (args[0].equals("-toImg")) {
				
				if (args[1].equals("-split")) {
					
					long split=Long.parseLong(args[2]);
					
					input=new File[args.length-3];
					int k=0;
					for (int i=3;i<args.length;i++) {
						
						input[k]=new File(userdir+File.separator+ args[i]);
						k++;
					}
					toImgConv.toImg(input,null,split);
				} else {
					input=new File[args.length-1];					

					int k=0;
					for (int i=1;i<args.length;i++) {
						
						input[k]=new File(userdir+File.separator+ args[i]);
						k++;
					}
					toImgConv.toImg(input);
				}
			} else if (args[0].equals("-toBin")) {
				
				input=new File[args.length-1];					

				int k=0;
				for (int i=1;i<args.length;i++) {
					
					input[k]=new File(userdir+File.separator+ args[i]);
					k++;
				}
				toBinConv.toBin(input, null);
			} else {
				singleInstance( args);
			}
		} else {
			singleInstance(args);
		}
	}
	
	
		singleInstance(args);
	}
	
	private static void singleInstance(final String[] args) {
		if (SingleInstance.isInstanceRunning(1234)) {
			SingleInstance.sendArgsToInstance(args);
		} else {
			EventQueue.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					try {
						UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					} catch (ClassNotFoundException e1) {
					} catch (InstantiationException e1) {
					} catch (IllegalAccessException e1) {
					} catch (UnsupportedLookAndFeelException e1) {
					}

					SingleInstanceHandler h=new ConverterGUI();
					SingleInstance.lock(h);
					h.passArguments(args);
				}
			});
			
		}
	}

	public String getTempPath() {
		return null;
	}*/
}