package plugin.upload.gui;

import jbiu.gui.GUIUploaderConfig;
import jbiu.init.JBIU;
import jbiu.model.ConfigModel;
import jbiu.model.UploaderConfig;

public class MultiHostGUIUploaderConfig extends GUIUploaderConfig {
	private String label;
	
	public MultiHostGUIUploaderConfig() {
		super(null);
		this.label = JBIU.rm().getLocalizedString("distribute");
	}
	
	@Override
	public String toString() {
		return label;
	}
	
	@Override
	public final UploaderConfig getUploaderConfig() {
		return null;
	}
	
	public UploaderConfig[] getUploaderConfigs() {
		String[] hostNames = JBIU.getInstance().getSettings().getStringArray("multiHosts");
		UploaderConfig[] hosts = new UploaderConfig[hostNames.length];
		
		for(int i=0;i<hostNames.length;i++)
			hosts[i]=ConfigModel.getUploaderConfig(hostNames[i]);
		return hosts;
	}

}
