/*	This program is released under European Union Public License.
 *  for details please refer to the file LICENSE
 *   
 *  Disclaimer: the program comes "as is" and with absolutely no warranty 
 *  
 *  © 2010,2011 TCM
 */

package plugin;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import model.transfer.linkcollector.ILinkCollectorEntry;
import model.transfer.linkcollector.LinkCollector;


public abstract class Script extends LinkCollector{
	protected long startBytes;
	protected long startTime;
	protected ScriptNotification status=ScriptNotification.stopped;
	protected int finished;
	protected long totalBytes;
	protected Path file;
	private int id;
	protected boolean autoResume;
	protected int numberOfFiles;
	
	public boolean autoResume() {
		return autoResume;
	}
	
	public void setAutoResume() {
		this.autoResume = true;
	}
	
	public Script(Path file) {
		this.file=file;
	}
	
	public void setStartBytes(long startBytes) {
		this.startBytes = startBytes;
	}
	
	public void addTotalBytes(long bytes) {
		totalBytes+=bytes;
	}

	public String getName() {
		return file.getFileName().toString();
	}
	
	public void setStartTime() {
		this.startTime = System.currentTimeMillis();
	}
	
	public void setStatus(ScriptNotification status) {
		if (this.status != ScriptNotification.finished)
			this.status = status;
	}

	public ScriptNotification getStatus() {
		return status;
	}
	
	public int getFileCounter() {
		return finished;
	}
	

	public long getTotalSize() {
		return totalBytes;
	}
	
	public long getStartTime() {
		return startTime;
	}
	
	public long getStartBytes() {
		return startBytes;
	}
	
	public Path getFile() {
		return file;
	}
	
	public void setFile(Path file) {
		this.file = file;
	}	
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	
	public int getNumberOfFiles() {
		return numberOfFiles;
	}
	
	@Override
	public int getEntryCount() {
		return numberOfFiles;
	}
	
	
	@Override
	public String toString() {
		StringBuilder sb=new StringBuilder();
		if (autoResume()) sb.append("AutoResume>1"); 
		List<ScriptLinkCollectorEntry> list=new LinkedList<>();
		for (ILinkCollectorEntry e:entries) {
			list.add((ScriptLinkCollectorEntry) e);
		}
		
		Collections.sort(list);
		for (ScriptLinkCollectorEntry li:list) {
			sb.append(li.toString());
		}
//		sb.append("FinishedBytes>"+startBytes);
//		sb.append("TotalSize>"+totalBytes);
		
		return sb.toString();
	}
}
