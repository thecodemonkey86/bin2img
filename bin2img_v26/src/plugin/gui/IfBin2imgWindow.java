package plugin.gui;

import javax.swing.JMenu;

import converter.gui.settings.NewSettingsDialog;

public interface IfBin2imgWindow {
	public JMenu getViewMenu();
	public NewSettingsDialog getSettingsDialog();
}
