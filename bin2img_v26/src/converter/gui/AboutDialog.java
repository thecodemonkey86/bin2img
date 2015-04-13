/*	This program is released under European Union Public License.
 *  for details please refer to the file LICENSE
 *   
 *  Disclaimer: the program comes "as is" and with absolutely no warranty 
 *  
 *  © 2010-2012 TCM
 */

package converter.gui;

import gui.okcancelpanel.ClosePanel;
import init.Bin2ImgInit;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import static init.Bin2ImgInit.rm;

public class AboutDialog extends JDialog {
	
	private static final long serialVersionUID = 1L;

	private JScrollPane scrollLic;
		
	private JTextArea licenseText;
	private JPanel contentPane,aboutPanel;
	private ClosePanel closePanel;
	
	
	public AboutDialog() {
		
		setTitle(rm().getLocalizedString("menuAbout"));
		setSize(640,480);
		setModal(true);
		contentPane=new JPanel();
		setContentPane(contentPane);
		licenseText=new JTextArea();
		scrollLic=new JScrollPane(licenseText);
		
		aboutPanel=new JPanel();
		aboutPanel.setLayout(new GridLayout(8,1));
		
		final String thecodemonkeyDotDe="http://www.thecodemonkey.de";
		
		JButton bLink=new JButton("<html><b>"+thecodemonkeyDotDe+"</b></html>");
		
		JLabel labels[]=new JLabel[] {new JLabel(rm().getLocalizedString("about1")),
								new JLabel(rm().getLocalizedString("about2")),
								new JLabel(rm().getLocalizedString("about3")),
								new JLabel(rm().getLocalizedString("about4")+ " "+Bin2ImgInit.getInstance().getVersion()),
								new JLabel(rm().getLocalizedString("about5")),
								new JLabel(rm().getLocalizedString("about6")),
								
								
		};
		
		for (JLabel l:labels) {
			l.setHorizontalAlignment(JLabel.CENTER);
			aboutPanel.add(l);
		}
		
		bLink.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Desktop.getDesktop().browse(new URI(thecodemonkeyDotDe));
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (URISyntaxException e1) {
					e1.printStackTrace();
				}
			}
		});
		aboutPanel.add(bLink);
		
		closePanel=new ClosePanel(this, rm().getLocalizedString("close"),true);
		
		contentPane.setLayout(new BorderLayout());
		JPanel pAbout=new JPanel(new BorderLayout());
		pAbout.add(aboutPanel,BorderLayout.CENTER);
		
		JLabel icon=new JLabel(new ImageIcon(Bin2ImgInit.getInstance().getExecPath().resolve("bin2img_48x48.png").toString()));
		pAbout.add(icon,BorderLayout.SOUTH);
		icon.setPreferredSize(new Dimension(getWidth(), 80));
		contentPane.add(pAbout,BorderLayout.CENTER);
		contentPane.add(closePanel,BorderLayout.SOUTH);
		
		/*try {
			BufferedReader br=new BufferedReader(new FileReader(Bin2ImgModel.getModel().getExecPath()+"COPYING"));
			StringBuilder sb=new StringBuilder();
			String s=br.readLine();
			
			while (s!=null) {
				sb.append(s);
				sb.append('\n');
				s=br.readLine();
			}
			licenseText.setText(sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		licenseText.setEditable(false);
		licenseText.setCaretPosition(0);
		scrollLic.getVerticalScrollBar().setValue(0);
		scrollLic.getHorizontalScrollBar().setValue(0);
		
		validate();
	}
}
