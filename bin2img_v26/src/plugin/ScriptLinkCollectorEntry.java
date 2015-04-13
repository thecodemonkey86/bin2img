package plugin;

import java.nio.file.Path;
import java.nio.file.Paths;

import model.transfer.linkcollector.LinkCollectorEntry;

public class ScriptLinkCollectorEntry extends LinkCollectorEntry implements Comparable<ScriptLinkCollectorEntry>{

	public ScriptLinkCollectorEntry(String relativeFile) {
		super(1);
		this.setFile(Paths.get(relativeFile));
	}
	
	public ScriptLinkCollectorEntry(Path file) {
		super(1);
		this.setFile(file);
	}
	
	@Override
	public int compareTo(ScriptLinkCollectorEntry o) {
		return getFile().compareTo(o.getFile());
	}

	@Override
	public String toString() {
		return "Download>"+getFile()+">"+getLink(0)+">"+getSize()+"\n";
	}
	
}
