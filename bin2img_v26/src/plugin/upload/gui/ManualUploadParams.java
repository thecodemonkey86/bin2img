package plugin.upload.gui;

import gui.layout.percent2.APConstraint;
import gui.layout.percent2.AnchorPercentLayout;
import gui.okcancelpanel.OKCancelPanel;
import gui.util.GuiUtil;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import util.exception.CancelException;

import jbiu.gui.GUIUploaderConfig;
import jbiu.gui.input.InputComponent;
import jbiu.gui.input.model.InputComponentConfig;

import static plugin.upload.UploaderPlugin.rm;

public class ManualUploadParams extends JDialog {
	private static final long serialVersionUID = 1L;

	private JPanel panel;
	private OKCancelPanel okCancelPanel;
	
	public ManualUploadParams() {
		super((Dialog) null, "Upload parameters", true);
		panel = new JPanel(new AnchorPercentLayout());
		
		okCancelPanel = new OKCancelPanel(this,rm().getLocalizedString("ok"), rm().getLocalizedString("cancel"));
		Container cp = getContentPane();
		((JComponent) cp).setBorder(GuiUtil.DEFAULT_EMPTY_BORDER);
		cp.setLayout(new BorderLayout());
		cp.add(new JScrollPane(panel), BorderLayout.CENTER);
		cp.add(okCancelPanel, BorderLayout.SOUTH);
		setSize(300,300);
		panel.setPreferredSize(getSize());
	}

	public Map<String, String> getTokens(GUIUploaderConfig cfg) throws IOException, CancelException {
		if (cfg.getInputComponentCount() > 0) {
			
			panel.removeAll();
			Dimension d=panel.getPreferredSize();
			int sumHeight=0;
			
			List<InputComponent> input = new LinkedList<>();

		
			Iterator<InputComponentConfig> itComp = cfg.getInputComponentsIterator();

			int i=0;
			while (itComp.hasNext()) {
				InputComponent comp = (InputComponent) itComp.next().createInputComponent();
				int h=comp.getMinimumSize().height;
				sumHeight+=h;
				panel.add(new JLabel(comp.getDescription()),APConstraint.constraint().left(0).widthPercent(30).top(i*h).height(h));
				panel.add(comp,APConstraint.constraint().leftPercent(30).widthPercent(70).top(i*h).height(h));
				input.add(comp);
				i++;
			}
			d.setSize(d.width, sumHeight);
			panel.setPreferredSize(d);
			setVisible(true);
			okCancelPanel.getRetVal();

			Map<String, String> tokens = new HashMap<String, String>();

			for (InputComponent ic : input) {
				tokens.put(ic.getTokenName(), ic.getTokenValue());
			}
			
			return tokens;
		}
		throw new IllegalArgumentException();
	}

}
