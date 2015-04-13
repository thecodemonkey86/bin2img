package converter;

import java.nio.file.Path;

import model.transfer.linkcollector.ILinkCollectorEntry;

import plugin.Script;

public class SCDownloadScript extends Script {

	public SCDownloadScript(Path file) {
		super(file);
	}

	@Override
	public void addEntry(ILinkCollectorEntry e) {
		super.addEntry(e);
		addTotalBytes(e.getSize());
	}
	
	@Override
	public String toString() {
		return super.toString()+"TotalSize>"+getTotalSize();
	}
}
