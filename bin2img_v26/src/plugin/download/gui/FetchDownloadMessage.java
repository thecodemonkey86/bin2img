package plugin.download.gui;

import gui.util.GuiUtil;

import java.awt.GridLayout;
import java.awt.Window;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;

import plugin.download.DownloaderPlugin;

public class FetchDownloadMessage extends JDialog {

	private static final long serialVersionUID = -6587390298868468158L;

	private JLabel msg;
	
	public FetchDownloadMessage(Window owner) {
		super(owner,"bin2img");
		msg=new JLabel();
		
		getContentPane().setLayout(new GridLayout(2, 1));
		getContentPane().add(new JLabel(DownloaderPlugin.rm().getLocalizedString("fetchingMeta")));
		getContentPane().add(msg);
		((JComponent) getContentPane()).setBorder(GuiUtil.DEFAULT_EMPTY_BORDER);
		//setSize(400, 100);
		GuiUtil.centerWindow(this, owner);
		//setPreferredSize(getSize());
	}
	
	public void showMessage(List<String> urls) {
		String s=urls.get(0);
		for(int i=1;i<urls.size();i++) {
			s+="<br/>"+urls.get(i);
		}
		msg.setText("<html>"+s+"</html>");
		pack();
		setVisible(true);
	}
	
	public void close(){
		dispose();
	}
}
