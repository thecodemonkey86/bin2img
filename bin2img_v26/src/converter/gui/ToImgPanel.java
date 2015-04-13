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
import gui.layout.percent2.APConstraint;
import gui.layout.percent2.AnchorPercentLayout;
import gui.resource.RMButton;
import gui.settings.GUISettingsManager;
import gui.settings.SettingsSplitPane;
import gui.util.GuiUtil;

import init.Bin2ImgInit;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import converter.Bin2ImgModel;
import converter.ConverterSettings;


import nio.converter.ConvertToImgObserver;
import nio.converter.ToImgInfo2;
import nio.converter.ToImgInfoList;
import nio.converter.model.ImgInfoProvider;
import nio.converter.model.ScheduledToImg2;


import settings.Settings;
import settings.SettingsLoadable;

import static init.Bin2ImgInit.rm;

public class ToImgPanel extends SettingsSplitPane implements Observer,ConvertToImgObserver,SettingsLoadable {
	private static final long serialVersionUID = 1L;

	private static final String IMG_PREFIX="toImg";
	private static final GUISettingsManager GSM = new GUISettingsManager();
	private FileTree fileTree;
	private FileList fileList;
	private ProgressPanel progressPanel; 
	private SplitValuesComponent splitValueComp;
	private ScheduledToImg2 toImg;
	
	public ToImgPanel() {
		super(IMG_PREFIX);
		Settings settings=Bin2ImgInit.settings();
		try{
			toImg=new ScheduledToImg2(settings.getInt("convertThreads")) {
				
				@Override
				protected void postConvertAction(ToImgInfo2 info) throws IOException {
				}
				
				@Override
				protected void cancelConvertAction(ToImgInfo2 info) throws IOException {
				}
			};
		} catch (IOException e) {
			e.printStackTrace();
		}
		toImg.setObserver(this);
		fileList=new FileList(true,rm().getLocalizedString("fileListName"),rm().getLocalizedString("fileListSize"),rm().getLocalizedString("fileListDate"));
		fileTree=new FileTree(fileList);
		
		SettingsSplitPane splitPaneFilesystem=new SettingsSplitPane(JSplitPane.HORIZONTAL_SPLIT,fileTree,fileList,IMG_PREFIX+"FileTree");
		
		JButton cmdFilesToImg=new RMButton(rm(),"convertSelected");
		cmdFilesToImg.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					binToImg(fileList.getSelectedFiles());
				} catch (IOException e1) {
					String err="Error";
					if (e1.getMessage()!=null) err+=": "+e1.getMessage();
					progressPanel.addToLog(err);
				}				
			}
		});
		
		JButton cmdFolderToImg=new RMButton(rm(),"convertFolder");
		cmdFolderToImg.addActionListener(new ConvertRecursiveActionListener() {
			
			@Override
			protected void process(Path[] input) {
				try {
					binToImg(input);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			@Override
			protected Path getSourcePath() {
				return fileTree.getSelectedDir();
			}
		});
		
		JButton cmdOpenSystemBrowserToImg=new RMButton(rm(),"browser");
		cmdOpenSystemBrowserToImg.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				GuiUtil.openBrowser(Bin2ImgModel.getModel().getOutputImg());			
			}
		});
		
		JButton cmdShowScriptCreator=new RMButton(rm(),"btnScriptCreator");
		cmdShowScriptCreator.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					new ScriptCreatorGUI(null).show(null);
				} catch (IOException e1) {
					e1.printStackTrace();
				}				
			}
		});
		
		progressPanel=new ProgressPanel();
		SettingsSplitPane splitPaneControl=new SettingsSplitPane(JSplitPane.HORIZONTAL_SPLIT, progressPanel,getOptionPanel(cmdFilesToImg, cmdFolderToImg, cmdOpenSystemBrowserToImg, cmdShowScriptCreator),IMG_PREFIX+"Option");
		
		GSM.addSettingsComponent(splitPaneControl);
		GSM.addSettingsComponent(splitPaneFilesystem);
		
		setOrientation(VERTICAL_SPLIT);
		setLeftComponent(splitPaneFilesystem);
		setRightComponent(splitPaneControl);
	}

	private void binToImg(Path[] input) throws IOException {
		if (input!=null){
			progressPanel.setValue(0, 0);
			long splitBytes=splitValueComp.getSplitValue();
			ToImgInfoList infos=ImgInfoProvider.get(fileTree.getSelectedDir(), input, null, splitBytes);
			toImg.setOutputDir(Bin2ImgModel.getModel().getOutputImg());
			toImg.toImg(infos);
			toImg.start();
		}
	}
	
	private Component getOptionPanel(JButton cmdFilesToImg, JButton cmdFolderToImg, JButton cmdOpenSystemBrowserToImg, JButton cmdShowScriptCreator) {
		JPanel pOptions=new JPanel(new AnchorPercentLayout());
		splitValueComp=new SplitValuesComponent();
		((JComponent) splitValueComp.getComponent()).setBorder(GuiUtil.DEFAULT_EMPTY_BORDER);
		GSM.addSettingsComponent(splitValueComp);
		pOptions.add(splitValueComp.getComponent(),APConstraint.constraint().left(0).heightPercent(25).right(0).top(0));
		
		pOptions.add(new JCheckBox(rm().getLocalizedString("createManually")),APConstraint.constraint().left(10).right(10).topPercent(25));
		
		pOptions.add(cmdFilesToImg,APConstraint.constraint().left(10).topPercent(50).heightPercent(20).rightPercent(51));
		pOptions.add(cmdFolderToImg,APConstraint.constraint().left(10).topPercent(75).heightPercent(20).rightPercent(51));
		pOptions.add(cmdOpenSystemBrowserToImg,APConstraint.constraint().leftPercent(51).topPercent(50).heightPercent(20).right(10));
		pOptions.add(cmdShowScriptCreator,APConstraint.constraint().leftPercent(51).topPercent(75).heightPercent(20).right(10));
		return pOptions;
	}
	
//	private void addSettingsComponent(JPanel p,SettingsComponent c,Object layoutConstraint){
//		p.add(c.getComponent(),layoutConstraint);
//		settingComps.add(c);
//	}
	
	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void updateStartConvert(ToImgInfo2 info) {
		progressPanel.addToLog(rm().getLocalizedString("converting") +" "+info.getRelativeInputFileName() + " [Offset = "+info.getPos()+"]");		
	}

	@Override
	public void updateFinishConvert(ToImgInfo2 info, int count, int numberOfFiles) {
		progressPanel.setValue(count, numberOfFiles);
		progressPanel.addToLog("Finished "+info.getRelativeInputFileName() + " [Offset = "+info.getPos()+"]");
	}

	@Override
	public void updateStart(int numberOfFiles) {
		progressPanel.setValue(0, numberOfFiles);
	}
	
	@Override
	public void saveSettings(Settings s) {
		super.saveSettings(s);
		GSM.saveSettings(s);
		s.set(ConverterSettings.SETTING_LAST_PATH_IMG, fileTree.getSelectedDir());
		s.set(ConverterSettings.SETTING_FILELIST_COL_WIDTH, GuiUtil.getColumnWidths(fileList.getFileTable()));
		s.set(ConverterSettings.SETTING_FILELIST_COL_POS, GuiUtil.getColumnPositions(fileList.getFileTable()));
		s.set(IMG_PREFIX+ConverterSettings.SETTING_FILELIST_SORT_COLUMN, fileList.getSortKey().getColumn());
		s.set(IMG_PREFIX+ConverterSettings.SETTING_FILELIST_SORT_MODE, fileList.getSortKey().getSortOrder().name().toLowerCase());
		s.set(IMG_PREFIX+"FileTreeScrollX", fileTree.getScrollX());
		s.set(IMG_PREFIX+"FileTreeScrollY", fileTree.getScrollY());
	}
	
	@Override
	public void loadSettings(Settings s) {
		super.loadSettings(s);
		GSM.loadSettings(s);
		fileTree.expandFileTree(s.getPath(ConverterSettings.SETTING_LAST_PATH_IMG));
		fileTree.setScrollValues(s.getInt0(IMG_PREFIX+"FileTreeScrollX"), s.getInt0(IMG_PREFIX+"FileTreeScrollY"));
		GuiUtil.setColumnPosAndWidths(fileList.getFileTable(), s.getIntArray(ConverterSettings.SETTING_FILELIST_COL_POS), s.getIntArray(ConverterSettings.SETTING_FILELIST_COL_WIDTH));
		fileList.setSortKey(
				s.getInt(IMG_PREFIX+ConverterSettings.SETTING_FILELIST_SORT_COLUMN),
				s.getString(IMG_PREFIX+ConverterSettings.SETTING_FILELIST_SORT_MODE)
		);
	}

	@Override
	public void updateFinished(int numberOfFiles) {
		progressPanel.setValue(numberOfFiles, numberOfFiles);
		progressPanel.addToLog("Finished all");
		
	}
	
}
