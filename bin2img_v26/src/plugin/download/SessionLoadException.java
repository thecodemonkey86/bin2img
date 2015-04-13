package plugin.download;

import plugin.download.convert.DownloadScript;

public class SessionLoadException extends Exception{
	private static final long serialVersionUID = 2345790418870739043L;

	private DownloadScript s;
	
	public SessionLoadException(DownloadScript s ) {
		this.s=s;
	}
	
	public DownloadScript getScript() {
		return s;
	}
}
