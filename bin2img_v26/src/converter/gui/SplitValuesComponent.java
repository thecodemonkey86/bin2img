package converter.gui;

import static plugin.upload.UploaderPlugin.SETTING_SPLIT_ENABLED;
import static plugin.upload.UploaderPlugin.SETTING_SPLIT_UNIT;
import static plugin.upload.UploaderPlugin.SETTING_SPLIT_VALUE;

import init.Bin2ImgInit;

import java.awt.Component;

import gui.layout.percent2.APConstraint;
import gui.layout.percent2.AnchorPercentLayout;
import gui.settings.dialog.component.CheckboxSettingsComponent;
import gui.settings.dialog.component.ComboBoxSettingsComponent;
import gui.settings.dialog.component.SettingsComponent;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import common.util.SplitUnit;
import converter.ConverterSettings;
import static init.Bin2ImgInit.rm;
import settings.Settings;

public class SplitValuesComponent extends SettingsComponent{

	private CheckboxSettingsComponent chkSplit;
	private ComboBoxSettingsComponent<SplitUnit> cbUnit;
	private ComboBoxSettingsComponent<String> cbSplitValue;
	private JPanel panel;
	
	public SplitValuesComponent() {
		
		chkSplit=new CheckboxSettingsComponent(SETTING_SPLIT_ENABLED, rm().getLocalizedString("optionSplit"));
		cbUnit=new ComboBoxSettingsComponent<SplitUnit>(SETTING_SPLIT_UNIT);
		cbSplitValue=new ComboBoxSettingsComponent<String>(SETTING_SPLIT_VALUE){
			@Override
			public void loadSettings(Settings s) {
				cb.setSelectedItem(s.getString(settingsName));
			}
			
			@Override
			public void saveSettings(Settings s) {
				s.set(settingsName, cb.getSelectedItem().toString());
			}
		};
		
		panel=new JPanel(new AnchorPercentLayout(cbUnit.getComponent().getMinimumSize()));
		
		((JComboBox<?>) cbSplitValue.getComponent()).setEditable(true);
		panel.add(chkSplit.getComponent(),APConstraint.constraint().left(0).top(0));
		panel.add(cbSplitValue.getComponent(),APConstraint.constraint().leftPercent(40).rightPercent(25).top(0));
		panel.add(cbUnit.getComponent(),APConstraint.constraint().leftPercent(76).right(0).top(0));
		
		DefaultComboBoxModel<SplitUnit> modelUnit=cbUnit.getModel();
//		modelUnit.addElement(new SplitUnit("Bytes", 1));
		modelUnit.addElement(new SplitUnit("KiB", 1024));
		modelUnit.addElement(new SplitUnit("MiB", 1048576));
		
		setSplitValues();
	}

	@Override
	public void loadSettings(Settings s) {
		chkSplit.loadSettings(s);
		cbSplitValue.loadSettings(s);
		cbUnit.loadSettings(s);
	}

	@Override
	public void saveSettings(Settings s) {
		chkSplit.saveSettings(s);
		cbSplitValue.saveSettings(s);
		cbUnit.saveSettings(s);
	}

	public long getSplitValue() {
		return chkSplit.isSelected()?(long)(Double.parseDouble(cbSplitValue.getSelectedValue().replace(',','.'))*cbUnit.getSelectedItem().getValue2()):-1L;
	}
	
	
	public void setSplitValues(){

		DefaultComboBoxModel<String> modelvalues=cbSplitValue.getModel();
		modelvalues.removeAllElements();
		String[] splitValues= Bin2ImgInit.settings().getStringArray(ConverterSettings.SETTING_SPLIT_VALUES);
		
		for (int i=0;i<splitValues.length;i++) {
			modelvalues.addElement(splitValues[i]);
		}
	}
	
	@Override
	public Component getComponent() {
		return panel;
	}
}
