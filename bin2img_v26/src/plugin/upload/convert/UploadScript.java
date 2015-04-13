package plugin.upload.convert;


import java.nio.file.Path;
import java.util.List;

import nio.converter.ToImgInfoList;

import model.transfer.linkcollector.ILinkCollectorEntry;

import plugin.Script;
import plugin.upload.UploaderPlugin;


import jbiu.model.UploadLinkCollector;
import jbiu.model.UploaderConfig;
import jbiu.model.template.LinkTemplateToken;

public abstract class UploadScript extends Script {
	
	public UploadScript(Path file, int numberOfFiles) {
		super(file);

		setNumberOfFiles(numberOfFiles);
	}
	
	private boolean convertFinished;
	
	
	public void setConvertFinished(boolean convertFinished) {
		this.convertFinished = convertFinished;
	}
	
	public boolean isConvertFinished() {
		return convertFinished;
	}
	
	private static UploadLinkCollector scriptLinkCollector=new UploadLinkCollector(){
		@Override
		protected List<LinkTemplateToken> getLinkTemplateTokens(ILinkCollectorEntry entry) {
			List<LinkTemplateToken>l= super.getLinkTemplateTokens(entry);
			l.add(new LinkTemplateToken("$SCRIPTNAME$", entry.getFile().getFileName().toString()));
			return l;
		}
	};
	
	@Override
	public void addEntry(ILinkCollectorEntry e) {
		super.addEntry(e);
		finished++;
	}

	public abstract UploaderConfig getConfig();
	
	public static UploadLinkCollector getScriptLinkCollector() {
		return scriptLinkCollector;
	}
	
	public void setNumberOfFiles(int numberOfFiles) {
		this.numberOfFiles = numberOfFiles;
	}
	
	public static UploadScript singleHost(ToImgInfoList input,String scriptName, UploaderConfig uploaderConfig) {
		return new SingleHostUploadScript(UploaderPlugin.getSessionPath().resolve(scriptName).resolve(scriptName), uploaderConfig, input.size());
	}
	
	public static UploadScript multiHost(ToImgInfoList input,String scriptName, UploaderConfig[] uploaderConfig) {
		return new MultiHostUploadScript(UploaderPlugin.getSessionPath().resolve(scriptName).resolve(scriptName), uploaderConfig, input.size());
	}

	public abstract void reloadConfig();
	
}
