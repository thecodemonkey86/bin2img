package plugin.upload.convert;

import java.nio.file.Path;

import jbiu.model.ConfigModel;
import jbiu.model.UploaderConfig;

public class SingleHostUploadScript extends UploadScript {
	private UploaderConfig cfg;
	
	public SingleHostUploadScript(Path file, UploaderConfig uploaderConfig, int numberOfFiles) {
		super(file, numberOfFiles);
		this.cfg = uploaderConfig;
	}

	@Override
	public UploaderConfig getConfig() {
		return cfg;
	}
	
	public void setConfig(UploaderConfig cfg) {
		this.cfg = cfg;
	}

	@Override
	public void reloadConfig() {
		cfg = ConfigModel.getUploaderConfig(cfg.getInternalName());
	}

}
