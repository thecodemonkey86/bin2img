package plugin.upload.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import plugin.upload.UploaderPlugin;

import core.io.Application;

import jbiu.model.template.LinkTemplate;
import settings.Settings;
import gui.layout.percent2.APConstraint;
import gui.layout.percent2.AnchorPercentLayout;
import gui.resource.ResourceManager;
import gui.settings.dialog.component.CheckboxSettingsComponent;
import gui.settings.dialog.component.ComboBoxSettingsComponent;
import gui.settings.dialog.group.SettingsComponentGroup;
import gui.settings.dialog.page.SettingsPage;


public class AdvancedUploadSettingsPage extends SettingsPage {
	private static final long serialVersionUID = -3554353821453061266L;

	public AdvancedUploadSettingsPage(String entry, Application app) {
		super(entry, app);
	}

	@Override
	protected void createComponentGroups(Settings s,ResourceManager rm) {
		setLayoutMgr(2);
		addSettingsComponentGroup(new SettingsComponentGroup(rm.getLocalizedString("settingTitleParameter"), rm) {
			private static final long serialVersionUID = -2634932936987273280L;
			@Override
			protected void createSettingsComponents(ResourceManager rm) {
				setLayout(new AnchorPercentLayout());
				addSettingsComponent(new CheckboxSettingsComponent("autoParams",rm.getLocalizedString("settingAutoParams")),APConstraint.constraint());
			}
		});
		addSettingsComponentGroup(new ModifiersSettingsGroup(rm));
	}
	
	
	private class ModifiersSettingsGroup extends SettingsComponentGroup{
		private static final long serialVersionUID = -7445307480640535318L;

		public ModifiersSettingsGroup(ResourceManager rm) {
			super(rm.getLocalizedString("settingTitleTemplates"), rm);
		}

		@Override
		protected void createSettingsComponents(ResourceManager rm) {
			setLayout(new AnchorPercentLayout());
			final ComboBoxSettingsComponent<LinkTemplate> cb=new ComboBoxSettingsComponent<LinkTemplate>("modifiers"){
				@Override
				public void saveSettings(Settings s) {
					DefaultComboBoxModel<LinkTemplate> cbModel= getModel();
					String[] modifiers=new String[cbModel.getSize()];
					for (int i=0;i<cbModel.getSize();i++) {
						LinkTemplate m=cbModel.getElementAt(i);
						modifiers[i] =m.getValue1()+"#"+m.getValue2(); 
					}
					s.set(settingsName, modifiers);
				}

				@Override
				public void loadSettings(Settings s) {
					DefaultComboBoxModel<LinkTemplate> cbModel= getModel();
					String[] modifiers=s.getStringArray(settingsName);
					for (String m:modifiers) {
						String[] t=m.split("#");
						cbModel.addElement(new LinkTemplate(t[0], t[1]));
					}
				}
			};
			
			
			JButton bAdd=new JButton(rm.getLocalizedString("add"));
			bAdd.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					String display=getDisplay(null);
					String value= getValue(null);
					if (display!=null && value!=null)
						cb.getModel().addElement(new LinkTemplate(display, value));		
				}
			});
			JButton bEdit=new JButton(rm.getLocalizedString("edit"));
			bEdit.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					LinkTemplate m=cb.getSelectedItem();
					String display=getDisplay(m.getValue1());
					
					if (display!=null) {
						String value= getValue(m.getValue2());
						
						if (value!=null){
							m.setValue1(display);
							m.setValue2(value);
							cb.getComponent().repaint();
						}
					}
				}
			});
			JButton bRemove=new JButton(rm.getLocalizedString("remove"));
			bRemove.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					cb.removeSelectedItem();
				}
			});
			JPanel pButton=new JPanel(new GridLayout(1, 3));
			pButton.add(bAdd);
			pButton.add(bRemove);
			pButton.add(bEdit);
			
			JPanel p=new JPanel(new GridLayout(2, 1,0,20));
			addSettingsComponent(cb, p,null);
			p.add(pButton);
			
			add(p,APConstraint.constraint().left(20).right(20).height(68));
		}
		
		private String getDisplay(String defaultValue) {
			return JOptionPane.showInputDialog(UploaderPlugin.rm().getLocalizedString("desc"),defaultValue);
		}
		
		private String getValue(String defaultValue) {
			return JOptionPane.showInputDialog(UploaderPlugin.rm().getLocalizedString("linkPattern"),defaultValue);
		}
		
	}

}
