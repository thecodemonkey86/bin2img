package plugin.upload.gui;

import gui.okcancelpanel.ClosePanel;
import gui.settings.SettingCheckBox;
import gui.util.GuiUtil;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import plugin.upload.UploaderPlugin;
import plugin.upload.convert.UploadScript;

import jbiu.gui.LinkCollectorPanel;
import jbiu.model.JBIUModel;
import jbiu.model.template.LinkTemplate;
import static plugin.upload.UploaderPlugin.rm;

public class LinkCollectorGUI extends JFrame implements Runnable{
	private static final long serialVersionUID = 1L;

	private LinkCollectorPanel lcp;
	private List<String> linkNames;
	
	private static LinkCollectorGUI instance;
	
	public static void initLinkCollectorGUI(){
		if (instance==null) instance=new LinkCollectorGUI(); 
	}
	
	public static LinkCollectorGUI getInstance() {
		return instance;
	}
	
	private LinkCollectorGUI() {
		super("Download links");
		linkNames=new LinkedList<>();
		linkNames.add("Download");
		setSize(640, 480);
		lcp=new LinkCollectorPanel();
		ClosePanel closePanel=new ClosePanel(this, rm().getLocalizedString("close"),true);
		Container cp=getContentPane();
		cp.setLayout(new BorderLayout());
		lcp.setBorder(GuiUtil.DEFAULT_EMPTY_BORDER);
		cp.add(lcp,BorderLayout.CENTER);
		cp.add(closePanel,BorderLayout.SOUTH);
		((FlowLayout) closePanel.getLayout()).setHgap(20);
		final SettingCheckBox chkClear=new SettingCheckBox( rm().getLocalizedString("clearOnClose"),"clearLinksOnClose");
//		SettingCheckBox chkAppend=new SettingCheckBox("Append links to file","appendLinksToFile");
//		
//		JPanel pBottom=new JPanel(new BorderLayout(10,10));
//		pBottom.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Options"), GuiUtil.DEFAULT_EMPTY_BORDER));
//		JPanel pSettings=new JPanel(new AnchorPercentLayout());
//		pSettings.setBorder(GuiUtil.DEFAULT_EMPTY_BORDER);
//		pSettings.add(chkClear,APConstraint.constraint().left(0).right(0).top(0));
//		pSettings.add(chkAppend,APConstraint.constraint().left(0).rightPercent(50).top(0));
//		PathSettingsComponent pathAppend=new PathSettingsComponent(settingsName, pathDescr)
//		pSettings.add(chkClear);
		UploadPluginGUI.GSM.addSettingsComponent(chkClear);
		closePanel.add(chkClear);
		closePanel.getCloseButton().addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (chkClear.isSelected()) {
					lcp.clear();
				}
				LinkTemplate tpl= lcp.getModifier(0);
				UploaderPlugin.getInstance().getSettings().set("lastLinkTemplate", tpl.getValue2());
			}
		});
	}
	
	public void displayLinks() {
		EventQueue.invokeLater(this);
	}

	@Override
	public void run() {
		lcp.displayLinks(UploadScript.getScriptLinkCollector(),linkNames.iterator(),1,JBIUModel.getModifierByValue(UploaderPlugin.getInstance().getSettings().getString("lastLinkTemplate") ));
		SwingUtilities.updateComponentTreeUI(getContentPane());
		setVisible(true);
	}
}
