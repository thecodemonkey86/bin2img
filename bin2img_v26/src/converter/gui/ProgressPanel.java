/*	This program is released under European Union Public License.
 *  for details please refer to the file LICENSE
 *   
 *  Disclaimer: the program comes "as is" and with absolutely no warranty 
 *  
 *  © 2010,2011 TCM
 */

package converter.gui;

import gui.util.GuiUtil;
import gui.util.logpanel.LogPanel;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import static init.Bin2ImgInit.rm;

public class ProgressPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private JProgressBar progress;
	private LogPanel log;
	
	public ProgressPanel() {
		super(new BorderLayout(10,10));
		progress=new JProgressBar(SwingConstants.HORIZONTAL);
		progress.setMinimum(0);
		progress.setMaximum(100);
		progress.setString("0 % (0/0)");
		progress.setStringPainted(true);
		log=new LogPanel();
		Border b = BorderFactory.createCompoundBorder(
				 GuiUtil.DEFAULT_EMPTY_BORDER,
				BorderFactory.createTitledBorder(rm().getLocalizedString("progress"))
				
			);
		
		setBorder(BorderFactory.createCompoundBorder(b,GuiUtil.DEFAULT_EMPTY_BORDER));
		
		add(progress,BorderLayout.SOUTH);
		add(log,BorderLayout.CENTER);
	}

	public void addToLog(String string) {
		log.addToLog(string);
	}

	public void setValue(int finished,int count) {
		int p;
		if (count==0)
			p=0;
		else
			p=finished*100/count;
		progress.setValue(p);
		progress.setString(p +" % ("+finished+"/"+count+")");
	}

}
