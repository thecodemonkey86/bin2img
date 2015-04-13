/*	This program is released under European Union Public License.
 *  for details please refer to the file LICENSE
 *   
 *  Disclaimer: the program comes "as is" and with absolutely no warranty 
 *  
 *  © 2010,2011 TCM
 */

package common.util;

import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Calendar;

public final class Util {
	
	public static final String SPLIT_FILES_EXT=".00001.png";
	
	private static DecimalFormat d=new DecimalFormat("00000");
	public static final long[] BIT_MASK={65535L,4294901760L,281470681743360L,9223090561878065152L};
	
	private static Calendar cal=Calendar.getInstance();
	private static DateFormat df=DateFormat.getTimeInstance();
	
	public static String getDate() {
		cal.setTimeInMillis(System.currentTimeMillis());
		return df.format(cal.getTime());
	}
	
	
	
	public static Filter<Path> getPNGFilter() {
		return new Filter<Path>() {
			
			@Override
			public boolean accept(Path path) {
				return path.getFileName().toString().endsWith(".png");
			}
		};
	}

	public static Filter<Path> getScriptFilter() {
		return new Filter<Path>(){
			
			@Override
			public boolean accept(Path path) {
				return path.getFileName().toString().endsWith(".b2i");
			}
		};
	}
	
	public static String getClipboardString() {
		try {
			return Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor).toString();
		} catch (HeadlessException e) {
		} catch (UnsupportedFlavorException e) {
		} catch (IOException e) {
		}
		return null;
	}
	
	public static String formatFileNumbering(int i) {
		return d.format(i);
	}
	
	public static void runFileBrowser(String browser,String path) throws IOException {
		new ProcessBuilder(browser,path).start();
	}
	
	public static void runCommand(String command[]) throws IOException {
		new ProcessBuilder(command).start();
	}
	
	
	public static int getXCenterToComponent(Component relativeToComponent,int width) {
		return (relativeToComponent.getWidth()-width)/2+relativeToComponent.getX();
	}
	public static int getYCenterToComponent(Component relativeToComponent,int height) {
		return (relativeToComponent.getHeight()-height)/2+relativeToComponent.getY();
	}
	
}
