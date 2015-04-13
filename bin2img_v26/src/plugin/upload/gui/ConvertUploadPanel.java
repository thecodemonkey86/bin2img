package plugin.upload.gui;

import plugin.Script;

import jbiu.gui.BasicUploadPanel;
import jbiu.model.UploadManager;

public class ConvertUploadPanel extends BasicUploadPanel{

	private static final long serialVersionUID = 1L;

	private Script script;
	
	public ConvertUploadPanel(UploadManager um,Script script) {
		super(um);
		this.script=script;
	}
	
	public Script getScript() {
		return script;
	}

	@Override
	public void onFinish() {
		
	}
	
}
