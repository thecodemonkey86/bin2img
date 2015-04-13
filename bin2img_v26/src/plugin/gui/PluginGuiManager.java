package plugin.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import init.Bin2ImgInit;

import javax.swing.JMenuItem;

import gui.resource.ResourceManager;
import plugin.ScriptNotification;

public class PluginGuiManager {
	public static void init(ResourceManager rm) {
		ScriptNotification.stopped.setText(rm.getLocalizedString("statusStopped"));
		ScriptNotification.finished.setText(rm.getLocalizedString("statusFinished"));
		ScriptNotification.running.setText(rm.getLocalizedString("statusLoading"));
		ScriptNotification.converting.setText(rm.getLocalizedString("statusConverting"));
		ScriptNotification.queued.setText(rm.getLocalizedString("statusQueued"));
		ScriptNotification.remove.setText(rm.getLocalizedString("statusRemoving"));
		ScriptNotification.finalizing.setText(rm.getLocalizedString("statusFinalizing"));		
	}
	
	public static void addMenus(JMenuItem parent){
		JMenuItem it=new JMenuItem(Bin2ImgInit.rm().getLocalizedString("menuCheckForUpdates"));
		parent.add(it);
		it.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				new UpdateDialog();
			}
		});
		
	}
}
