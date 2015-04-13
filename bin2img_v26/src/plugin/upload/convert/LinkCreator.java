package plugin.upload.convert;

import java.io.IOException;


import plugin.Script;
import plugin.ScriptLinkCollectorEntry;
import util.exception.CancelException;

public interface LinkCreator {
	public ScriptLinkCollectorEntry create(Script s) throws IOException, CancelException;
}
