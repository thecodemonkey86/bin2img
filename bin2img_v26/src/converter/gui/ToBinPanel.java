/*	This program is released under European Union Public License.
 *  for details please refer to the file LICENSE
 *   
 *  Disclaimer: the program comes "as is" and with absolutely no warranty 
 *  
 *  © 2010,2011 TCM
 */

package converter.gui;

import gui.filelist.FileList;
import gui.filetree.FileTree;
import gui.settings.GUISettingsManager;
import gui.settings.SettingsSplitPane;
import gui.util.GuiUtil;

import init.Bin2ImgInit;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Path;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import converter.Bin2ImgModel;
import converter.ConverterSettings;

import settings.Settings;

import nio.converter.ConvertToBinObserver;
import nio.converter.model.BinAutoSplitProvider;
import nio.converter.model.ScheduledToBin2;

import static init.Bin2ImgInit.rm;

public class ToBinPanel extends SettingsSplitPane implements ConvertToBinObserver {
	private static final long serialVersionUID = 1L;
	private static final String BIN_PREFIX="toBin";
	
	private FileTree fileTree;
	private FileList fileList;
	private ProgressPanel progressPanel; 
	private static final GUISettingsManager GSM = new GUISettingsManager();
	private ScheduledToBin2 toBin;
	
	public ToBinPanel() {
		super(BIN_PREFIX);
		Settings settings=Bin2ImgInit.settings();
		
		int maxThreads=settings.getInt(ConverterSettings.SETTING_CONVERT_THREADS);
		toBin=new ScheduledToBin2(maxThreads);
		toBin.setObserver(this);
		fileList=new FileList(true,rm().getLocalizedString("fileListName"),rm().getLocalizedString("fileListSize"),rm().getLocalizedString("fileListDate"));
		fileTree=new FileTree(fileList);
		
		SettingsSplitPane splitPaneFilesystem=new SettingsSplitPane(JSplitPane.HORIZONTAL_SPLIT, fileTree,fileList,BIN_PREFIX+"FileTree");
		GSM.addSettingsComponent(splitPaneFilesystem);
		
		JButton cmdFilesToBin=new JButton(rm().getLocalizedString("convertSelected"));
		cmdFilesToBin.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					toBin.setOutputDir(Bin2ImgModel.getModel().getOutputBin());
					toBin.toBin(BinAutoSplitProvider.getAllSplitInputFiles(fileList.getSelectedFiles()));
					toBin.start();
				} catch (IOException ioex) {
					ioex.printStackTrace();
				}
			}
		});
		
		JButton cmdFolderToBin=new JButton(rm().getLocalizedString("convertFolder"));
		cmdFolderToBin.addActionListener(new ConvertRecursiveActionListener() {
			
			@Override
			protected void process(Path[] input) {
				if (input!=null){
					try {
						toBin.setOutputDir(Bin2ImgModel.getModel().getOutputBin());
						toBin.toBin(input);
						toBin.start();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
			@Override
			protected Path getSourcePath() {
				return fileTree.getSelectedDir();
			}
		});
		
		JButton cmdOpenSystemBrowserToBin=new JButton(rm().getLocalizedString("browser"));
		cmdOpenSystemBrowserToBin.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				GuiUtil.openBrowser(Bin2ImgModel.getModel().getOutputBin());			
			}
		});
		
		JPanel optionPanel=new JPanel();
		optionPanel.setLayout(new GridLayout( 3,1,10,10));
		optionPanel.setBorder(GuiUtil.DEFAULT_EMPTY_BORDER);
		optionPanel.add(cmdFilesToBin);
		optionPanel.add(cmdFolderToBin);
		optionPanel.add(cmdOpenSystemBrowserToBin);
		progressPanel=new ProgressPanel();
		
		SettingsSplitPane splitPaneControl=new SettingsSplitPane(JSplitPane.HORIZONTAL_SPLIT, progressPanel,optionPanel,BIN_PREFIX+ "Option");
		GSM.addSettingsComponent(splitPaneControl);

		setOrientation(VERTICAL_SPLIT);
		setLeftComponent(splitPaneFilesystem);
		setRightComponent(splitPaneControl); 
	}

	@Override
	public void updateStartConvert(Path input) {
		progressPanel.addToLog(rm().getLocalizedString("reading")+ " "+input);
	}

	@Override
	public void updateFinishConvert(Path output, int count, int numberOfFiles) {
		progressPanel.addToLog(rm().getLocalizedString("writing")+" "+output);
		progressPanel.setValue(count, numberOfFiles);
		if (count==numberOfFiles){
			progressPanel.addToLog(rm().getLocalizedString("finishedAll"));
		}
	}

	@Override
	public void updateStart(int numberOfFiles) {
		progressPanel.setValue(0, numberOfFiles);
	}
	
	@Override
	public void saveSettings(Settings s) {
		super.saveSettings(s);
		GSM.saveSettings(s);
		s.set(ConverterSettings.SETTING_LAST_PATH_BIN, fileTree.getSelectedDir());
		s.set(BIN_PREFIX+ConverterSettings.SETTING_FILELIST_COL_WIDTH, GuiUtil.getColumnWidths(fileList.getFileTable()));
		s.set(BIN_PREFIX+ConverterSettings.SETTING_FILELIST_COL_POS, GuiUtil.getColumnPositions(fileList.getFileTable()));
		s.set(BIN_PREFIX+ConverterSettings.SETTING_FILELIST_SORT_COLUMN, fileList.getSortKey().getColumn());
		s.set(BIN_PREFIX+ConverterSettings.SETTING_FILELIST_SORT_MODE, fileList.getSortKey().getSortOrder().name().toLowerCase());
		s.set(BIN_PREFIX+"FileTreeScrollX", fileTree.getScrollX());
		s.set(BIN_PREFIX+"FileTreeScrollY", fileTree.getScrollY());
	}
	
	@Override
	public void loadSettings(Settings s) {
		super.loadSettings(s);
		GSM.loadSettings(s);
		fileTree.expandFileTree(s.getPath(ConverterSettings.SETTING_LAST_PATH_BIN));
		fileTree.setScrollValues(s.getInt0(BIN_PREFIX+"FileTreeScrollX"), s.getInt0(BIN_PREFIX+"FileTreeScrollY"));
		GuiUtil.setColumnPosAndWidths(
				fileList.getFileTable(), 
				s.getIntArray(BIN_PREFIX+ConverterSettings.SETTING_FILELIST_COL_POS), 
				s.getIntArray(BIN_PREFIX+ConverterSettings.SETTING_FILELIST_COL_WIDTH)
		);
		fileList.setSortKey(
				s.getInt(BIN_PREFIX+ConverterSettings.SETTING_FILELIST_SORT_COLUMN),
				s.getString(BIN_PREFIX+ConverterSettings.SETTING_FILELIST_SORT_MODE)
		);
		
	}

}
