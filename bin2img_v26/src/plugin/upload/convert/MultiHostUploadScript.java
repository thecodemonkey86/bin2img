package plugin.upload.convert;

import java.nio.file.Path;

import jbiu.model.UploaderConfig;

public class MultiHostUploadScript extends UploadScript {

	private UploaderConfig[] uploaderConfig;
	private int counter;
	
	public MultiHostUploadScript(Path file, UploaderConfig[] uploaderConfig, int numberOfFiles) {
		super(file, numberOfFiles);
		this.uploaderConfig = uploaderConfig;
		counter = 0;
	}

	@Override
	public UploaderConfig getConfig() {
		return uploaderConfig[(counter++)%uploaderConfig.length];
	}

	@Override
	public void reloadConfig() {
		// TODO Auto-generated method stub
		
	}

}
