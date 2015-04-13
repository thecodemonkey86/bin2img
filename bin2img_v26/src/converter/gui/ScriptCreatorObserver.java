package converter.gui;

import java.nio.file.Path;

public interface ScriptCreatorObserver {
	public void updateRemoveFile(int index);
	public void updateRemoveLink(int index);
	public void updateAddFiles(Path... files);
	public void updateAddLinks(String...links);
	public void updateLinkUp(int index);
	public void updateLinkDown(int index);
}
