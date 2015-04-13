/*	This program is released under European Union Public License.
 *  for details please refer to the file LICENSE
 *   
 *  Disclaimer: the program comes "as is" and with absolutely no warranty 
 *  
 *  © 2010,2011 TCM
 */

package converter.gui;

import gui.filechooser.FilesChooser;
import gui.filechooser.FilesChooserFilter;
import gui.fileselection.PathSelectionButton;
import gui.fileselection.PathTextField;
import gui.layout.percent2.APConstraint;
import gui.layout.percent2.AnchorPercentLayout;

import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;

import util.exception.CancelException;

import model.WebsiteParserModel;

import common.util.Util;
import converter.ScriptCreatorModel;
import core.util.Lists;

import static gui.util.GuiUtil.*;
import static init.Bin2ImgInit.rm;
import static init.Bin2ImgInit.settings;

public class ScriptCreatorGUI extends JDialog implements ScriptCreatorObserver {
	private static final long serialVersionUID = 1L;

	private JSplitPane splitPane;

	private JLabel lblInfo;

	private WebsiteParserModel wpm;

	private ScriptCreatorModel model;
	private DefaultListModel<Path> availableFilesListModel; 
	private PathTextField txtPath;
		
	private DefaultListModel<String> linkListModel;
	private DefaultListModel<Path> fileListModel;
	
	public ScriptCreatorGUI(Window owner) {
		super(owner, rm().getLocalizedString("titleScriptCreator"));
		wpm = WebsiteParserModel.getWebsiteParserModel();
		model = new ScriptCreatorModel();
		linkListModel=new DefaultListModel<>();
		fileListModel=new DefaultListModel<>();
		setSize(640, 480);

		JButton cmdNew = new JButton(rm().getLocalizedString("scriptCreatorNew"));
		JButton cmdPaste = new JButton(rm().getLocalizedString("scriptCreatorClipboard"));
		JButton cmdSave = new JButton(rm().getLocalizedString("scriptCreatorSave"));
		JButton cmdAddAll = new JButton(rm().getLocalizedString("scriptCreatorAddAll"));
		JButton cmdAddSelected = new JButton(rm().getLocalizedString("scriptCreatorAddSelected"));
		
	

		cmdSave.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (save())
					dispose();
			}
		});


		cmdPaste.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Set<String> links;
					links = wpm.getLinks(Util.getClipboardString());

					for (String l : links) {
						model.addLink(l);
					}
				} catch (IOException ex) {

				} catch (CancelException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}
			}
		});

		addWindowListener(new WindowListener() {

			@Override
			public void windowOpened(WindowEvent e) {
			}

			@Override
			public void windowIconified(WindowEvent e) {
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
			}

			@Override
			public void windowClosing(WindowEvent e) {
				askSave();
				model.removeAll();
				dispose();
			}

			@Override
			public void windowClosed(WindowEvent e) {
			}

			@Override
			public void windowActivated(WindowEvent e) {
			}
		});
		JPanel pSourceDir=new JPanel(new AnchorPercentLayout());
		pSourceDir.setBorder(
			BorderFactory.createTitledBorder(rm().getLocalizedString("sourceDir"))
		);
		
		txtPath=new PathTextField();
		txtPath.setEditable(false);
		availableFilesListModel=new DefaultListModel<>();
		final JList<Path> availableFilesList=new JList<>(availableFilesListModel);
		availableFilesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		cmdAddSelected.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					model.addFiles(availableFilesList.getSelectedValuesList());
				} catch (IOException e1) {
					e1.printStackTrace();
				}	
			}
		});
		
		cmdAddAll.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Path[] files=new Path[availableFilesListModel.size()];
					for (int i=0;i<files.length;i++)
						files[i] =availableFilesListModel.get(i); 
					model.addFiles(files);
				} catch (IOException e1) {
					e1.printStackTrace();
				}	
			}
		});
		
		final PathSelectionButton selButton=new PathSelectionButton(
				txtPath,
				rm().getLocalizedString("selPath"),
				rm().getLocalizedString("ok"),
				rm().getLocalizedString("cancel")) 	{
			private static final long serialVersionUID = 2594088570754214373L;

			@Override
			protected void additionalAction() {
				selectSourceDir();
			}
		};
		
		cmdNew.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				askSave();
				model.removeAll();
				selButton.invoke(e);
			}
		});
		
		pSourceDir.add(txtPath,APConstraint.constraint().left(10).right(40));
		pSourceDir.add(selButton,APConstraint.constraint().width(30).right(10));
		
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(new JList<>(fileListModel)), new JScrollPane(new JList<>(linkListModel)));
		splitPane.setDividerLocation(200);

		lblInfo = new JLabel();
		updateCount();
		
		JPanel cp = (JPanel) getContentPane();
		cp.setLayout(new AnchorPercentLayout());
		JPanel pButtons = new JPanel();
		pButtons.setLayout(new GridLayout(1, 3));
		pButtons.add(cmdPaste);
		pButtons.add(cmdNew);
		pButtons.add(cmdSave);
		JPanel pInfo = new JPanel();
		pInfo.add(lblInfo);
		
		cp.setBorder(DEFAULT_EMPTY_BORDER);
		cp.add(pSourceDir, APConstraint.linearV(15, 0));
		cp.add(new JScrollPane(availableFilesList), APConstraint.constraint().left(0).rightPercent(30).topPercent(20).heightPercent(25));
		cp.add(
				splitPane, 
				
			 APConstraint.constraint().left(0).right(0).topPercent(50).bottom(60) );
		cp.add(cmdAddAll, APConstraint.constraint().right(0).heightPercent(10).widthPercent(30).topPercent(20));
		cp.add(cmdAddSelected, APConstraint.constraint().right(0).heightPercent(10).widthPercent(30).topPercent(35));
		cp.add(pInfo, APConstraint.constraint().left(0).right(0).height(30).bottom(30));
		cp.add(pButtons, APConstraint.constraint().left(0).right(0).height(30).bottom(0));
	}

	private void selectSourceDir() {
		Path sourceDir=txtPath.getPath();
//		model.setSourceDir(sourceDir);
		try {
			Files.walkFileTree(sourceDir, new PathSelectionFileVisitor());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public void show(List<Path> imageFiles) throws IOException {
		model.setObserver(this);
		model.removeAll();
		if (imageFiles != null) {
			model.addFiles(imageFiles);
		}
		setVisible(true);

	}

	private void askSave() {
		if (JOptionPane.showConfirmDialog(null, rm().getLocalizedString("scriptCreatorAskSave"), rm().getLocalizedString("titleScriptCreator"), JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
			save();
		}
	}

	/*private void selectFiles() {
		FilesChooser fc = new FilesChooser(getOwner(), rm().getLocalizedString("scriptCreatorSelect"), true, rm().getLocalizedString("ok"), rm().getLocalizedString("cancel"));
		fc.setChooseFilters(new FilesChooserFilter(Util.getPNGFilter(), rm().getLocalizedString("pngFilter")));
		try {
			fc.showOpenFilesDialog(settings().getPath("lastPathScriptPNG"));
			settings().set("lastPathScriptPNG", fc.getFileTreePath());
			Path[] files = fc.getSelectedPaths();
			if (files != null) {
				model.addFiles(files);
			}
		} catch (IOException e) {
		}

	}
*/
	private boolean save() {
		FilesChooser fc = new FilesChooser(getOwner(), rm().getLocalizedString("scriptCreatorSave"), false, rm().getLocalizedString("ok"), rm().getLocalizedString("cancel"));
		fc.setChooseFilters(new FilesChooserFilter(Util.getScriptFilter(), rm().getLocalizedString("b2iFilter"), ".b2i"));
		try {
			fc.showSavePathDialog(settings().getPath("lastPathScriptSave"));
			Path f = fc.getSelectedPath();
			settings().set("lastPathScriptSave", f);
			model.save(f);
			return true;
		} catch (CancelException ce) {

		} catch (IOException ioex){	
			JOptionPane.showMessageDialog(null, "Error: "+ioex.getLocalizedMessage());
			ioex.printStackTrace();
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, rm().getLocalizedString("scriptCreatorInvalidNumber"));
		}
		return false;
	}
/*
	@Override
	public void update(Observable o, Object arg) {
		linkPanel.invalidate();
		linkPanel.removeAll();
		filePanel.invalidate();
		filePanel.removeAll();
		
		int i=0;
		for (Path f : model.getImageFiles()) {
			final int index=i;
			JButton bRemove=new JButton("-");
			bRemove.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					model.removeFile(index);					
				}
			});
			int top=i*24;
			filePanel.add(bRemove,APConstraint.constraint().left(0).width(24).top(top));
			filePanel.add(new JTextField(f.toString()),APConstraint.constraint().left(24).top(top).height(24).right(0));
			i++;
		}
		
		i=0;
		for (String l : model.getLinks()) {
			final int index=i;
			JButton bRemove=new JButton("-");
			bRemove.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					model.removeLink(index);					
				}
			});
			int top=i*24;
			linkPanel.add(bRemove,APConstraint.constraint().left(0).width(24).top(top));
			linkPanel.add(new JTextField(l),APConstraint.constraint().left(24).top(i*24).height(24).right(0));
			i++;
		}
		splitPane.setPreferredSize(new Dimension(getWidth(), 24*Math.max(model.getNumberOfFiles(),model.getNumberOfLinks())));
		linkPanel.validate();
		filePanel.validate();
		
		String f = rm().getLocalizedString("file");

		if (model.getNumberOfFiles() > 1)
			f = rm().getLocalizedString("files");
		lblFiles.setText(model.getNumberOfFiles() + " " + f);
		lblSize.setText(formatBytes(model.getTotalSize()));
	}*/

	
	private class PathSelectionFileVisitor implements FileVisitor<Path> {
		
		public PathSelectionFileVisitor() {
			availableFilesListModel.clear();
		}
		
		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			if (file.getFileName().toString().endsWith(".png"))
				availableFilesListModel.addElement(file);
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
			return FileVisitResult.CONTINUE;
		}

		
	}


	@Override
	public void updateRemoveFile(int index) {
		fileListModel.remove(index);
		updateCount();
	}

	@Override
	public void updateRemoveLink(int index) {
		linkListModel.remove(index);
		updateCount();
	}

	@Override
	public void updateAddFiles(Path... files) {
		for (Path p:files){
			fileListModel.addElement(p);
		}
		updateCount();
	}
	
	private void updateCount() {
		String f;
		if (model.getNumberOfFiles() == 1)
			f = rm().getLocalizedString("file");
		else  
			f = rm().getLocalizedString("files");
		lblInfo.setText(model.getNumberOfFiles()+ " "  + f+ " | "+formatBytes(model.getTotalSize()) + " | "+model.getNumberOfLinks() + " links");
	}

	@Override
	public void updateAddLinks(String... links) {
		for (String l:links) {
			linkListModel.addElement(l);
		}
		updateCount();
	}

	@Override
	public void updateLinkUp(int index) {
		Lists.moveUp(linkListModel, index);
	}

	@Override
	public void updateLinkDown(int index) {
		Lists.moveDown(linkListModel, index);
	}
}
