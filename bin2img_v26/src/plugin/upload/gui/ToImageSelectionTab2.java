package plugin.upload.gui;

import init.Bin2ImgInit;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import gui.filelist.FileList;
import gui.filetree.FileTree;
import gui.layout.PercentageLayout;
import gui.layout.percent2.APConstraint;
import gui.layout.percent2.AnchorPercentLayout;
import gui.resource.RMButton;
import gui.resource.ResourceManager;
import gui.settings.dialog.component.ComboBoxSettingsComponent;
import gui.settings.dialog.component.RadioButtonSettingsComponent;
import gui.settings.dialog.group.SettingsComponentGroup;
import gui.util.GuiUtil;


import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;


import net.util.ProxySettings;
import nio.converter.ToImgInfo2;
import nio.converter.ToImgInfoList;
import nio.converter.model.ImgInfoProvider;

import plugin.gui.IfBin2imgWindow;
import plugin.upload.UploaderPlugin;
import plugin.upload.convert.ScheduledScriptManager;
import plugin.upload.convert.UploadScript;
import plugin.upload.gui.AcceptTOS.AcceptResult;

import converter.ConverterSettings;
import converter.gui.ConvertRecursiveActionListener;
import converter.gui.SplitValuesComponent;

import jbiu.gui.GUIConfigLoader2;
import jbiu.gui.GUILoginManager;
import jbiu.gui.GUILoginObserver;
import jbiu.gui.GUIUploaderConfig;
import jbiu.gui.input.GalleryInputComponent;
import jbiu.init.JBIU;
import jbiu.io.login.LoginManager;
import jbiu.model.UploaderConfig;

import settings.Settings;
import settings.SettingsLoadable;
import util.exception.CancelException;

import static plugin.upload.UploaderPlugin.*;


public class ToImageSelectionTab2 extends JSplitPane implements Observer,SettingsLoadable,GUILoginObserver {

	private static final long serialVersionUID = 1L;
	private List<SettingsLoadable> settingComps;

	private ComboBoxSettingsComponent<GUIUploaderConfig> cbUpload;
	private RadioButtonSettingsComponent multiScript;
	
	private FileTree fileTree;
	private FileList fileList;

	private JTextField txtScriptName;
	
	private JSplitPane splBottom,spFile;
	private JButton bLogin,bConvertSelected,bConvertFolder;
	
	private GalleryInputComponent galComp;
	private SplitValuesComponent splitValuesComp;
	
	private boolean showSimulator=false;
	
	public ToImageSelectionTab2(IfBin2imgWindow parentWindow) {
		super(VERTICAL_SPLIT);
		settingComps=new LinkedList<>();
		fileList=new FileList(true,rm().getLocalizedString("fileListName"),rm().getLocalizedString("fileListSize"),rm().getLocalizedString("fileListDate"));

		fileTree=new FileTree(fileList);
		
		spFile=new JSplitPane(HORIZONTAL_SPLIT, fileTree,fileList);
		setLeftComponent(spFile);
		setRightComponent(getUploadOptionsComponent(parentWindow));
		loadSettings(getInstance().getSettings());
		Bin2ImgInit.settings().addObserver(this);
		GUILoginManager.getInstance().addObserver(this);
	}
	
	void binToImg(final Path[] input,final String scriptName) throws IOException, CancelException {
	
		final long splitBytes=splitValuesComp.getSplitValue();
		
		Settings settings = JBIU.getInstance().getSettings();
		saveSettings(settings);
		if (input!=null) {
			GUIUploaderConfig gcfg=cbUpload.getSelectedItem();
			UploaderConfig cfg=gcfg.getUploaderConfig();
			if (cfg != null) {
				// FIXME mult host
				String settingAccept=cfg.getInternalName()+"_acceptTos";
				if (!settings.getBoolean(settingAccept)){
					try{
						AcceptResult res= new AcceptTOS((Window) getTopLevelAncestor()).showDialog(cfg);
						if (res==AcceptResult.ACCEPT_ALWAYS) {
							settings.set(settingAccept, true);
						}
					} catch (CancelException e) {
						return;
					}
				}
			}
			ScheduledScriptManager.getManager().setMaxThreads(settings.getInt(SETTING_MAX_SCRIPTS));
			Path sourceDir=fileTree.getSelectedDir();
			ToImgInfoList inputInfo=ImgInfoProvider.get(sourceDir, input, null, splitBytes);
			
			Map<String,String> tokens=null;
			if (gcfg.getInputComponentCount()>0) {
				if (!settings.getBoolean("autoParams")) {
					 tokens= new ManualUploadParams().getTokens(gcfg);
					
					for (String token:tokens.keySet()) {
						System.out.println(token);
					}
				} else {
					tokens=cfg.getDefaultFileTokens();
				}
			}
			if (gcfg.getGalleryInputComponentConfig()!=null){
				if (tokens!=null)
					tokens.putAll(galComp.getMultipleTokens());
				else
					tokens=galComp.getMultipleTokens();
			}
		
			if (gcfg instanceof MultiHostGUIUploaderConfig) {
				MultiHostGUIUploaderConfig mgcfg = (MultiHostGUIUploaderConfig) gcfg; 
				if (scriptName!=null) {				
					ScheduledScriptManager.getManager().addScript(inputInfo, UploadScript.multiHost(inputInfo, scriptName, mgcfg.getUploaderConfigs()), tokens);
				} else {
					for (ToImgInfo2 info:inputInfo) {
						UploadScript uploadScript = UploadScript.multiHost(inputInfo, info.getRelativeInputFileName()+".b2i", mgcfg.getUploaderConfigs());
						ScheduledScriptManager.getManager().addScript( ImgInfoProvider.single(info), uploadScript,tokens);
					}
				}
			} else {
				if (scriptName!=null) {				
					ScheduledScriptManager.getManager().addScript(inputInfo, UploadScript.singleHost(inputInfo, scriptName, gcfg.getUploaderConfig()), tokens);
				} else {
					for (ToImgInfo2 info:inputInfo) {
						UploadScript uploadScript = UploadScript.singleHost(inputInfo, info.getRelativeInputFileName()+".b2i", gcfg.getUploaderConfig());
						ScheduledScriptManager.getManager().addScript( ImgInfoProvider.single(info), uploadScript,tokens);
					}
				}
			}
			
			

		}
	}
	
	private JComponent getUploadOptionsComponent(final IfBin2imgWindow parentWindow) {
		PercentageLayout pl= new PercentageLayout();
		pl.setMinSize(new Dimension(0, 200));
		JPanel pOptions=new JPanel(pl);
		
		pOptions.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		SettingsComponentGroup grpOptions=new SettingsComponentGroup(rm().getLocalizedString("options"), UploaderPlugin.rm()) {
		
			private static final long serialVersionUID = -4374215117952913042L;

			@Override
			protected void createSettingsComponents(ResourceManager rm) {
				setLayout(new AnchorPercentLayout());
				splitValuesComp=new SplitValuesComponent();
				add(splitValuesComp.getComponent(),APConstraint.constraint().left(20).right(20).top(0));
				settingComps.add(splitValuesComp);
				
				txtScriptName=new JTextField();
				
				txtScriptName.setToolTipText(rm().getLocalizedString("tooltipScriptname"));
				multiScript=new RadioButtonSettingsComponent(SETTING_MULTIPLE_SCRIPTS);
				multiScript.setLayout(new AnchorPercentLayout());
				
				JRadioButton radSingle=new JRadioButton(rm().getLocalizedString("optSingleScript"));
				radSingle.setOpaque(false);
				JRadioButton radMulti=new JRadioButton(rm().getLocalizedString("optMultiScript"));
				radMulti.setOpaque(false);
				
				multiScript.addRadioButton(radSingle, APConstraint.constraint().left(20).top(0));
				multiScript.getComponent().add(txtScriptName, APConstraint.constraint().leftPercent(40).right(20).top(0));
				multiScript.addRadioButton(radMulti, APConstraint.constraint().left(20).bottom(0));
				addSettingsComponent(multiScript,APConstraint.linearV(50, 45));
			}
		};
		bConvertSelected=new RMButton(rm(),"convertSelected");
		bConvertFolder=new RMButton(rm(), "convertRecursively");
		JButton bOpenInput=new RMButton(rm(),"openInput");
		JButton bShowLinkCollector=new RMButton(rm(),"showLinkCollector");
		
		
		SettingsComponentGroup grpUpload=new SettingsComponentGroup("Upload",rm()) {

			private static final long serialVersionUID = -3673419306480569674L;

			@Override
			protected void createSettingsComponents(ResourceManager rm) {
				setLayout(new AnchorPercentLayout());
				cbUpload = new ComboBoxSettingsComponent<GUIUploaderConfig>(SETTING_HOST) {
					
					@Override
					public void saveSettings(Settings s) {
						UploaderConfig cfg=((GUIUploaderConfig) cb.getSelectedItem()).getUploaderConfig();
						if (cfg!=null)
							s.set(SETTING_HOST, cfg.getInternalName());
					}
					
					@Override
					public void loadSettings(Settings s) {
						String host=s.getString(SETTING_HOST);
						if (host != null) {
							for (GUIUploaderConfig  gcfg: GUIConfigLoader2.getConfigs()){
								if (gcfg.getInternalName()==null||gcfg.getInternalName().equals(host)) {
									cb.setSelectedItem(gcfg);
									return;
								}
							}
						}
					}
				};
				
				((JComboBox<?>) cbUpload.getComponent()).addItemListener(new ItemListener() {
					
					@Override
					public void itemStateChanged(ItemEvent e) {
						if (e.getStateChange() == ItemEvent.SELECTED) {
							GUIUploaderConfig gcfg = cbUpload.getSelectedItem();
							UploaderConfig cfg=gcfg.getUploaderConfig();
							
							galComp.setGalleryInputComponent(cbUpload.getSelectedItem());
							
							if (gcfg instanceof MultiHostGUIUploaderConfig) {
								bLogin.setEnabled(false);
								setUploadButtonsEnabled(true);
							} else if (cfg==null){
								bLogin.setEnabled(false);
								setUploadButtonsEnabled(false);
							} else if (cfg.getLoginInfo()==null){
								bLogin.setEnabled(false);
								setUploadButtonsEnabled(true);
							} else {
								bLogin.setEnabled(true);
								bLogin.setText(cfg.getLoginInfo().isLoggedIn()?rm().getLocalizedString("logout"):rm().getLocalizedString("login"));
								setUploadButtonsEnabled(true);
							}
						}						
					}
				});
				
			
				JButton bUploadOptions=new RMButton(rm(),"moreOptions");
				bUploadOptions.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							parentWindow.getSettingsDialog().showDialog("Upload");
						} catch (IOException e1) {
							e1.printStackTrace();
						} catch (CancelException ce) {
							ce.printStackTrace();
						}
					}
				});
				
				bLogin=new JButton(rm().getLocalizedString("login"));
				bLogin.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							UploaderConfig cfg=cbUpload.getSelectedItem().getUploaderConfig();
							if (!cfg.getLoginInfo().isLoggedIn()){
								LoginManager.performLogin(cfg,ProxySettings.getProxySettings(Bin2ImgInit.settings()));
								
							}else{
								LoginManager.performLogout(cfg);
								
								galComp.logout();
							}
						} catch (IOException e1) {
							e1.printStackTrace();
						} catch (CancelException ce) {
							ce.printStackTrace();
						}
						
					}
				});
				
				add(new JLabel(rm().getLocalizedString("host")),APConstraint.constraint().left(20).topPercent(5).height(24));
				addSettingsComponent(cbUpload, APConstraint.constraint().leftPercent(20).topPercent(5).rightPercent(35));
				add(bLogin,APConstraint.constraint().leftPercent(66).right(20).height(24).topPercent(5));
				
				
				galComp = new GalleryInputComponent();
				add(new JLabel(rm().getLocalizedString("gallery")),APConstraint.constraint().left(20).bottomPercent(5).height(24));
				add(galComp, APConstraint.constraint().leftPercent(20).bottomPercent(5).rightPercent(35));
				add(bUploadOptions,APConstraint.constraint().leftPercent(66).right(20).height(24).bottomPercent(5));
//				
				
				if (showSimulator){
					
					// Only for debugging
					cbUpload.getModel().addElement(GUIConfigLoader2.getSimulator());
				} else {
					cbUpload.getModel().addElement(GUIConfigLoader2.getNoneSelected(rm().getLocalizedString("selectHost")));
				}
//				cbUpload.getModel().addElement(new MultiHostGUIUploaderConfig());
				for (GUIUploaderConfig gcfg: GUIConfigLoader2.getConfigs()){
					cbUpload.getModel().addElement(gcfg);
				}
			}
		}; 
		
		addSettingsComponentGroup(pOptions,grpOptions,PercentageLayout.constraints(0, 0, 100, 55));
		addSettingsComponentGroup(pOptions, grpUpload,PercentageLayout.constraints(0, 55, 100, 45));
		
		JPanel pButtons=new JPanel(new GridLayout(4, 1,0,10));
		pButtons.setBorder(GuiUtil.DEFAULT_EMPTY_BORDER);
		

		
		bOpenInput.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				GuiUtil.openBrowser(fileTree.getSelectedDir());				
			}
		});
		
		bConvertSelected.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Path[] input=fileList.getSelectedFiles();
					binToImg(input,getScriptName(input));
				}catch (IOException ioex) {
					ioex.printStackTrace();
				} catch (CancelException ce) {
					ce.printStackTrace();
				}
			}
		});
		bConvertFolder.addActionListener(new ConvertRecursiveActionListener(){

			@Override
			protected void process(Path[] input) {
				try {
					binToImg(input,fileTree.getSelectedDir().getFileName().toString()+".b2i");
				} catch (IOException e) {
					e.printStackTrace();
				} catch (CancelException e) {
					e.printStackTrace();
				}
			}

			@Override
			protected Path getSourcePath() {
				return fileTree.getSelectedDir();
			}			
		});		
		bShowLinkCollector.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				LinkCollectorGUI.getInstance().displayLinks();
			}
		});
		
		pButtons.add(bConvertSelected);
		pButtons.add(bConvertFolder);
		pButtons.add(bOpenInput);
		pButtons.add(bShowLinkCollector);
		
		
		splBottom=new JSplitPane(HORIZONTAL_SPLIT,pOptions,pButtons);
		return splBottom;
	}

	private void setUploadButtonsEnabled(boolean b) {
		bConvertSelected.setEnabled(b);
		bConvertFolder.setEnabled(b);
	}
	
	
	private String getScriptName(Path[] files) {
		if (files==null) return null;
		String scriptName=null;
		if (multiScript.getSelectedIndex() == 0) {
			scriptName =txtScriptName.getText();
			
			if (scriptName.length()==0) {
				scriptName = files[0].getFileName().toString();
			}
			
			if (!scriptName.endsWith(".b2i")) scriptName+=".b2i";
		}
		return scriptName;
	}
	
	private void addSettingsComponentGroup(JPanel p,SettingsComponentGroup grp,Object args) {
		settingComps.add(grp);
		p.add(grp,args);
	}
	
	
	
	public void saveSettings(final Settings settings) {
		if (fileTree.getSelectedDir()!=null)
		settings.set(SETTING_LAST_PATH,fileTree.getSelectedDir().toString());
		settings.set(SETTING_FILELIST_COL_WIDTH, GuiUtil.getColumnWidths(fileList.getFileTable()));
		settings.set(SETTING_FILELIST_COL_POS, GuiUtil.getColumnPositions(fileList.getFileTable()));
		
		settings.set(SETTING_FILELIST_DIVIDER_LOCATION,spFile.getDividerLocation());
		settings.set(SETTING_BOTTOM_DIVIDER_LOCATION,splBottom.getDividerLocation());
		settings.set(SETTING_MAIN_DIVIDER_LOCATION,	getDividerLocation());
		
		for (SettingsLoadable settingCmp:settingComps) {
			settingCmp.saveSettings(getInstance().getSettings());
		}
		
		settings.set(SETTING_FILELIST_SORT_COL, fileList.getSortKey().getColumn());
		settings.set(SETTING_FILELIST_SORT_MODE, fileList.getSortKey().getSortOrder().name().toLowerCase());
		
		settings.set("fileTreeScrollX", fileTree.getScrollX());
		settings.set("fileTreeScrollY", fileTree.getScrollY());
	}
	
	public void refreshFileSystem() throws IOException {
		fileList.refresh();
	}
	

	@Override
	public void update(Observable o, Object arg) {
		if (o instanceof Settings && arg instanceof String) {
			String settingsName=(String)arg;
			
			if (settingsName.equals(ConverterSettings.SPLIT_VALUES)) {
				splitValuesComp.setSplitValues();
			}
		}
	}
	
	@Override
	public void loadSettings(final Settings settings) {
		GuiUtil.setColumnPosAndWidths(fileList.getFileTable(), settings.getIntArray(SETTING_FILELIST_COL_POS), settings.getIntArray(SETTING_FILELIST_COL_WIDTH));

		fileList.setSortKey(
				settings.getInt(SETTING_FILELIST_SORT_COL),
				settings.getString(SETTING_FILELIST_SORT_MODE)
		);
		
		Path p0=Paths.get(settings.getString(SETTING_LAST_PATH));
		if (!p0.isAbsolute()) p0=p0.toAbsolutePath();
		final Path p=p0;
//		EventQueue.invokeLater(new Runnable() {
//			
//			@Override
//			public void run() {
//				fileTree.expandFileTree(p);
//			}
//		});
		
		new SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground() throws Exception {
				fileTree.expandFileTree(p);
				fileTree.setScrollValues(settings.getInt0("fileTreeScrollX"), settings.getInt0("fileTreeScrollY"));
				
				return null;
			}
		}.execute();
		
		for (SettingsLoadable settingsCmp:settingComps) {
			settingsCmp.loadSettings(getInstance().getSettings());
		}
		
		spFile.setDividerLocation(settings.getInt(SETTING_FILELIST_DIVIDER_LOCATION));
		splBottom.setDividerLocation(settings.getInt(SETTING_BOTTOM_DIVIDER_LOCATION));
		setDividerLocation(settings.getInt(SETTING_MAIN_DIVIDER_LOCATION));
		
	}

	@Override
	public void login(UploaderConfig cfg) {
		if (cbUpload.getSelectedItem().getUploaderConfig().equals(cfg))
			bLogin.setText(rm().getLocalizedString("logout"));
	}

	@Override
	public void logout(UploaderConfig cfg) {
		if (cbUpload.getSelectedItem().getUploaderConfig().equals(cfg))
			bLogin.setText(rm().getLocalizedString("login"));
	}

	@Override
	public void loginFailed(UploaderConfig cfg) {
	}

}
