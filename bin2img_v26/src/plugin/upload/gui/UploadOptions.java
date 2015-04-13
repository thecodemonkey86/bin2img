package plugin.upload.gui;

import gui.layout.AnchorLayout;
import gui.layout.AnchorLayoutConstraints;
import gui.okcancelpanel.OKCancelPanel;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

import jbiu.init.JBIU;

import settings.Settings;
import settings.SettingsLoadable;
import util.exception.CancelException;

import static gui.layout.AnchorParam.*;

public class UploadOptions extends JDialog implements SettingsLoadable{

	private static final long serialVersionUID = 1L;

	private JButton bLoginData;
	private JCheckBox chkImageHost,chkAutoParams,chkSaveToDisk;
	private OKCancelPanel okCancel;
	
	public UploadOptions() {
		super((Dialog)null, "Upload options", true);
		Container cp=getContentPane();
		
		JPanel pOptions=new JPanel();
		pOptions.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		JPanel pParams=new JPanel();
		pParams.setBorder(BorderFactory.createTitledBorder("Upload parameters (description, title, TOS, ...)"));
		
		chkAutoParams=new JCheckBox("Automatic");
		JPanel loginPanel=new JPanel();
		loginPanel.setBorder(BorderFactory.createTitledBorder("Login data"));
		loginPanel.setLayout(new AnchorLayout());
		bLoginData=new JButton("Edit login data");

		loginPanel.add(bLoginData,AnchorLayoutConstraints.get(height(24),width(150)));
		
		pParams.setLayout(new AnchorLayout());
		pParams.add(chkAutoParams,AnchorLayoutConstraints.get(height(24)));
		
		
		pOptions.setLayout(new GridLayout(3, 1,10,10));
		
		JPanel pDownloadScript = new JPanel();
		pDownloadScript.setBorder(BorderFactory.createTitledBorder("Download script"));
		pDownloadScript.setLayout(new AnchorLayout());
		chkSaveToDisk=new JCheckBox("Save to local disk");
		chkImageHost=new JCheckBox("Create download link (image host)");
		chkImageHost.setToolTipText("Upload b2i file to image hosting site");
		
		pDownloadScript.add(chkImageHost,AnchorLayoutConstraints.get(height(24),top(10)));
		pDownloadScript.add(chkSaveToDisk,AnchorLayoutConstraints.get(height(24),bottom(10)));
		
		cp.setLayout(new BorderLayout());
		pOptions.add(pParams);
		pOptions.add(pDownloadScript);
		pOptions.add(loginPanel);
		
		okCancel=new OKCancelPanel(this, "OK", "Cancel");
		
		cp.add(pOptions,BorderLayout.CENTER);
		cp.add(okCancel,BorderLayout.SOUTH);
		
		
//		setResizable(false);
		setSize(400, 400);
		loadSettings(JBIU.getInstance().getSettings());
	}
	
	public void showDialog() throws CancelException {
		setVisible(true);
		okCancel.getRetVal();
		saveSettings(JBIU.getInstance().getSettings());
	}

	
	@Override
	public void loadSettings(Settings s) {
		chkAutoParams.setSelected(s.getBoolean("autoParams"));
//		chkImageHost.setSelected(s.getBoolean("submitToArchive"));
	}

	@Override
	public void saveSettings(Settings s) {
		s.set("autoParams", chkAutoParams.isSelected());
	}
}
