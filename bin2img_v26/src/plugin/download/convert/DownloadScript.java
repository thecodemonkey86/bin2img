package plugin.download.convert;

import java.nio.file.Path;

import model.transfer.linkcollector.ILinkCollectorEntry;

import plugin.Script;

public class DownloadScript extends Script{

	public DownloadScript(Path file) {
		super(file);
	}

	@Override
	public void addEntry(ILinkCollectorEntry e) {
		addTotalBytes(e.getSize());
		numberOfFiles++;
		super.addEntry(e);
	}

	
	public void setFinished(DownloadLinkInfo li) {
		li.setFinished();
		finished++;		
	}
}
