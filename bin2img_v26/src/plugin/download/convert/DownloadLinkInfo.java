package plugin.download.convert;

import java.nio.file.Path;

import plugin.ScriptLinkCollectorEntry;


public class DownloadLinkInfo extends ScriptLinkCollectorEntry {

	public DownloadLinkInfo(Path file, String link, long bytes) {
		super(file);
		setLink(0, link);
		setSize(bytes);
	}

	public DownloadLinkInfo(String relativePath, String link, long bytes) {
		super(relativePath);
		setLink(0, link);
		setSize(bytes);
	}

	private boolean finished, downloadOnlyFinished;
	
	public void setFinished() {
		downloadOnlyFinished=false;
		finished=true;
	}
	
	public void setDownloadOnlyFinished() {
		this.downloadOnlyFinished = true;
	}
	
	public boolean isDownloadOnlyFinished() {
		return downloadOnlyFinished;
	}
	
	public boolean isFinished() {
		return finished;
	}
	
	@Override
	public String toString() {
		String status="Download>";
		if (isFinished()) {
			status="Finished>";
		} else if (isDownloadOnlyFinished()) {
			status="ToConvert>";
		}
		return status+getFile()+">"+getLink(0)+">"+getSize()+"\n";
	}

	public void reset() {
		downloadOnlyFinished=false;
		finished=false;
	}
	
}
