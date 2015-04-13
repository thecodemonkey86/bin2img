package plugin.upload.gui;

import gui.util.GuiUtil;
import gui.util.MultiLineJLabel;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import jbiu.gui.ReadTosActionListener;
import jbiu.model.UploaderConfig;

import util.exception.CancelException;

import static plugin.upload.UploaderPlugin.rm;

public class AcceptTOS extends JDialog{

	private static final long serialVersionUID = -2596250438537957361L;

	public static enum AcceptResult{ACCEPT,ACCEPT_ALWAYS,CANCEL};	
	
	private AcceptResult result;
	private ReadTosActionListener readAl;
	
	public AcceptTOS(Window owner) {
		super(owner, "bin2img",ModalityType.APPLICATION_MODAL);
		JPanel cp=(JPanel) getContentPane();
		cp.setBorder(GuiUtil.DEFAULT_EMPTY_BORDER);
		MultiLineJLabel msg=new MultiLineJLabel(rm().getLocalizedString("askTos"));
		cp.setLayout(new BorderLayout(10,10));
		cp.add(msg,BorderLayout.CENTER);
		JButton btnAcceptAlways=new ResultButton(AcceptResult.ACCEPT_ALWAYS, "btnAcceptAlways");
		JButton btnAccept=new ResultButton(AcceptResult.ACCEPT,"btnAccept");
		JButton btnRead=new JButton(rm().getLocalizedString("btnReadToS"));
		JButton btnCancel=new ResultButton(AcceptResult.CANCEL,"cancel");
		JPanel pButtons=new JPanel(new GridLayout(2,2,10,10));
		pButtons.setBorder(GuiUtil.DEFAULT_EMPTY_BORDER);
		pButtons.add(btnRead);
		pButtons.add(btnAccept);
		pButtons.add(btnAcceptAlways);
		pButtons.add(btnCancel);
		cp.add(pButtons,BorderLayout.SOUTH);
		readAl=new ReadTosActionListener();
		btnRead.addActionListener(readAl);
		setSize(600, 280);
	}
	
	public AcceptResult showDialog(UploaderConfig cfg) throws CancelException {
		readAl.setCfg(cfg);
		setVisible(true);
		if (result==AcceptResult.CANCEL) throw new CancelException();
		return result;
	}
	
	private class ResultButton extends JButton {
		private static final long serialVersionUID = -681898221375486073L;

		public ResultButton(final AcceptResult res, String localizedStringName) {
			super(rm().getLocalizedString(localizedStringName));
			addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					dispose();
					result=res;
				}
			});
		}
	}
}
