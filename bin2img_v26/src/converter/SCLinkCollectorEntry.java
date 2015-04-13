package converter;

import plugin.ScriptLinkCollectorEntry;

public class SCLinkCollectorEntry extends ScriptLinkCollectorEntry {

	public SCLinkCollectorEntry(long size,String link) {
		super((String)null);
		setLink(0, link);
		setSize(size);
	}

	
}
