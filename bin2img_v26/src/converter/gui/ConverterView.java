package converter.gui;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import settings.Settings;
import settings.SettingsLoadable;

public abstract class ConverterView {
	protected JComponent comp;
	private AbstractButton btn;
	private String name;
	private Settings settings;
	
	public ConverterView(String name,AbstractButton btn,Settings settings) {
		this.name=name;
		this.btn=btn;
		this.settings=settings;
	}
	
	public AbstractButton getBtn() {
		return btn;
	}
	
	public JComponent getComponent() {
		if (comp==null) {
			createComp();
			loadSettings();
		}
		return comp;
	}

	public abstract void createComp();
	
	@Override
	public String toString() {
		return name;
	}
	
	public void updateUITree(){
		SwingUtilities.updateComponentTreeUI(comp);
		comp.updateUI();
	}

	public void saveSettings(){
		((SettingsLoadable)comp).saveSettings(settings);
	}
	
	public void loadSettings(){
		((SettingsLoadable)comp).loadSettings(settings);
	}
}
