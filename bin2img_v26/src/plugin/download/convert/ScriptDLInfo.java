/*	This program is released under European Union Public License.
 *  for details please refer to the file LICENSE
 *   
 *  Disclaimer: the program comes "as is" and with absolutely no warranty 
 *  
 *  © 2010,2011 TCM
 */

package plugin.download.convert;

import java.net.MalformedURLException;
import java.nio.file.Path;

import model.DLInfo;

public class ScriptDLInfo extends DLInfo {
	
	private DownloadLinkInfo linkInfo;
	
	protected ScriptDLInfo(DownloadLinkInfo linkInfo,Path output) throws MalformedURLException {
		super(linkInfo.getLink(0), output,linkInfo.getFile().toString());
		this.linkInfo=linkInfo;
	}
	
	
	public DownloadLinkInfo getLinkInfo() {
		return linkInfo;
	}
	
	@Override
	public Path getOutputFile() {
		if (fileName!=null){
			return output.resolve(fileName);
		} else {
			return null;
		}
	}
}
