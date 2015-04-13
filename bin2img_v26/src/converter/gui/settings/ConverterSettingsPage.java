package converter.gui.settings;

import init.Bin2ImgInit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import settings.Settings;

import converter.ConverterSettings;

import gui.layout.percent2.APConstraint;
import gui.layout.percent2.AnchorPercentLayout;
import gui.resource.ResourceManager;
import gui.settings.dialog.component.ListSettingsComponent;
import gui.settings.dialog.group.SettingsComponentGroup;
import gui.settings.dialog.group.ThreadsSettingsGroup;
import gui.settings.dialog.page.SettingsPage;

public class ConverterSettingsPage extends SettingsPage {

	private static final long serialVersionUID = -527115958833479459L;

	public ConverterSettingsPage(String entry) {
		super(entry,Bin2ImgInit.getInstance());
	}

	@Override
	protected void createComponentGroups(Settings s,ResourceManager rm) {
		setLayout(new AnchorPercentLayout());
		addSettingsComponentGroup(new FileSplitSettingsGrp(rm.getLocalizedString("settingTitleSplit")),APConstraint.linearV(70, 0));
		addSettingsComponentGroup(new ThreadsSettingsGroup(rm.getLocalizedString("settingConvertThreads"), ConverterSettings.SETTING_CONVERT_THREADS, rm),APConstraint.linearV(25, 75));
	}

	private class FileSplitSettingsGrp extends SettingsComponentGroup {

		private static final long serialVersionUID = -7150533977581925457L;

		public FileSplitSettingsGrp(String title) {
			super(title,Bin2ImgInit.rm());
		}

		@Override
		protected void createSettingsComponents(final ResourceManager rm) {
			final ListSettingsComponent c=new ListSettingsComponent(ConverterSettings.SPLIT_VALUES);
			setLayout(new AnchorPercentLayout());
			addSettingsComponent(c,	APConstraint.constraint().left(20).right(80).top(20).bottom(20));
			JButton bAdd=new JButton("+");
			JButton bRem=new JButton("-");
			JButton bUp=new JButton("↑");
			JButton bDwn=new JButton("↓");
			add(bDwn,APConstraint.constraint().right(20).width(60).height(40).bottom(20));
			add(bUp,APConstraint.constraint().right(20).width(60).height(40).bottom(60));
			add(bRem,APConstraint.constraint().right(20).width(60).height(40).bottom(100));
			add(bAdd,APConstraint.constraint().right(20).width(60).height(40).bottom(140));
			bAdd.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					String v=JOptionPane.showInputDialog("Bytes");
					if (v!=null)
						c.addValue(v);				
				}
			});
			bRem.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					c.removeSelected();				
				}
			});
			bUp.addActionListener(new ActionListener() {
							
				@Override
				public void actionPerformed(ActionEvent e) {
					c.valueUp();				
				}
			});
			bDwn.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					c.valueDown();				
				}
			});
		}
		
	}
}
