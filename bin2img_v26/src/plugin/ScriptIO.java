/*	This program is released under European Union Public License.
 *  for details please refer to the file LICENSE
 *   
 *  Disclaimer: the program comes "as is" and with absolutely no warranty 
 *  
 *  © 2010,2011 TCM
 */

package plugin;


import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import core.io.Application;
import plugin.Script;
import util.base64.GzipPlusBase64Coder;
import nio.converter.ByteInput;


public class ScriptIO {

	
	public static ByteInput writeScriptToBytes(Script s) {
		ByteInput inp = new ByteInput(s.toString().getBytes());
		return inp;
	}
	
	public static void writeScript(Script s) throws IOException {
		writeScript(s, s.getFile());
	}

	public static void writeScript(Script s,Path dest) throws IOException {
		 
		BufferedOutputStream out=new BufferedOutputStream(Files.newOutputStream(dest, Application.DEFAULT_WRITE_OPTIONS));
		out.write(s.toString().getBytes(Application.UTF8));
		out.close();
	}
	
	public static void writeScriptBase64(Script s) throws IOException {
		writeScriptBase64(s, s.getFile());
	}

	public static void writeScriptBase64(Script s,Path dest) throws IOException {
		BufferedOutputStream out=new BufferedOutputStream(Files.newOutputStream(dest, Application.DEFAULT_WRITE_OPTIONS));
		out.write("Encoding>Gzip+Base64\nVersion>2.0\n".getBytes(Application.UTF8));
		out.write(GzipPlusBase64Coder.toGzipPlusBase64(s.toString().getBytes(Application.UTF8)).getBytes(Application.UTF8));
		out.close();
	}
	
	
}
