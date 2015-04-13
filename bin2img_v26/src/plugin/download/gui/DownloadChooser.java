package plugin.download.gui;

import init.Bin2ImgInit;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import gui.filelist.FileList;
import gui.filetree.FileTree;
import gui.layout.AnchorLayout;
import gui.layout.AnchorLayoutConstraints;
import static gui.layout.AnchorParam.*;
import gui.okcancelpanel.OKCancelPanel;
import gui.settings.SettingsSplitPane;
import gui.settings.SettingsTabbedPane;
import gui.util.GuiUtil;
import gui.util.PasteButton;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import plugin.download.DownloaderPlugin;

import common.util.Util;

import settings.Settings;
import settings.SettingsLoadable;
import util.exception.CancelException;

import static plugin.download.DownloaderPlugin.rm;

public class DownloadChooser extends JDialog implements SettingsLoadable{

	private static final long serialVersionUID = 1L;

	private FileList filelist;
	private FileTree filetree;
	private OKCancelPanel okCancelPanel;
	private SettingsTabbedPane tabs;
	private SettingsSplitPane splitFiletree;
	private JTextArea txtUrl;
	public static final int TAB_URL=0,TAB_FILE=1;
	
	public DownloadChooser() {
		super((Dialog)null,rm().getLocalizedString("scriptNewDownload"),true );
		JPanel p = new JPanel();
		p.setLayout(new AnchorLayout());
		txtUrl=new JTextArea();
		txtUrl.setBorder(BorderFactory.createLoweredBevelBorder());
		p.add(new JLabel("URL"),AnchorLayoutConstraints.get(left(10),
															right(10),
															top(10),
															height(24)
														));
		p.add(new JScrollPane(txtUrl),AnchorLayoutConstraints.get(left(10),
												 right(160),
												 top(34),
												 bottom(24)
												));
		p.add(new PasteButton(txtUrl, rm().getLocalizedString("paste")),AnchorLayoutConstraints.get(
				width(150),
				 right(10),
				 top(30),
				 height(30)
				));
		okCancelPanel = new OKCancelPanel(this, rm().getLocalizedString("ok"), rm().getLocalizedString("cancel"));
		filelist = new FileList(true, Bin2ImgInit.rm().getLocalizedString("fileListName"),
				Bin2ImgInit.rm().getLocalizedString("fileListSize"), Bin2ImgInit.rm().getLocalizedString("fileListDate"));
		filelist.addFileFilter(Util.getScriptFilter());
		filelist.addDoubleClickActionListener(okCancelPanel.getOkAction());
		filetree = new FileTree(filelist);
		splitFiletree=new SettingsSplitPane(SettingsSplitPane.HORIZONTAL_SPLIT, filetree, filelist,"fileTreeDlChooser");
		tabs = new SettingsTabbedPane("dlChooser");
		tabs.addTab("URL", p);
		tabs.addTab(rm().getLocalizedString("dlChooserTabLocal"), splitFiletree);
		
		Container cp=getContentPane();
		cp.setLayout(new BorderLayout());
		cp.add(tabs,BorderLayout.CENTER);
		cp.add(okCancelPanel,BorderLayout.SOUTH);
		setSize(600, 350);
	}
	
	public void showDialog() throws CancelException {
		loadSettings(DownloaderPlugin.settings());		
		String link= GuiUtil.getClipboardString();
		if (link!=null){
			if (link.startsWith("b2i://")||link.startsWith("http://")){
				txtUrl.setText(link);
			}
			
		}
		setVisible(true);
		okCancelPanel.getRetVal();
		saveSettings(DownloaderPlugin.settings());
	}
	
	public Path[] getSelectedFiles() {
		if (tabs.getSelectedIndex()==TAB_FILE)
			return filelist.getSelectedFiles();
		else
			return null;
	}
	
	public int getOption() {
		return tabs.getSelectedIndex();
	}
	
	public String getUrl() {
		if (tabs.getSelectedIndex()==TAB_URL)
			return txtUrl.getText();
		else
			return null;
	}

	@Override
	public void loadSettings(Settings s) {
		splitFiletree.loadSettings(s);
		tabs.loadSettings(s);
		filetree.expandFileTree(s.getPath(DownloaderPlugin.SETTING_LAST_PATH_DL_CHOOSER));
	}

	@Override
	public void saveSettings(Settings s) {
		splitFiletree.saveSettings(s);
		tabs.saveSettings(s);
		s.set(DownloaderPlugin.SETTING_LAST_PATH_DL_CHOOSER, filetree.getSelectedDir());
	}

	public List<String> getUrls() {
		if (tabs.getSelectedIndex()==TAB_URL){
			String[] u=txtUrl.getText().split("\n");
			for(int i=0;i<u.length;i++) {
				u[i] = u[i].trim();
			}
			return Arrays.asList(u);
		}else{
			return null;
		}
	}
}
