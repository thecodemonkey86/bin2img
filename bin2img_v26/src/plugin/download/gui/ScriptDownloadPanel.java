/*	This program is released under European Union Public License.
 *  for details please refer to the file LICENSE
 *   
 *  Disclaimer: the program comes "as is" and with absolutely no warranty 
 *  
 *  © 2010,2011 TCM
 */

package plugin.download.gui;

import plugin.Script;
import plugin.download.DownloaderPlugin;

import gui.BasicDownloadPanel;
import model.DownloadManager;
import model.transfer.TransferInfo;

public class ScriptDownloadPanel extends BasicDownloadPanel {

	private static final long serialVersionUID = 1L;
	private Script script;
	
	public ScriptDownloadPanel(DownloadManager dm,Script script) {
		super(dm);
		this.script=script;
		for (TransferInfo info :dm.getUnfinished())
			super.updateAddTransfer(info);
		
		loadSettings(DownloaderPlugin.settings());
	}

	public Script getScript() {
		return script;
	}
}
