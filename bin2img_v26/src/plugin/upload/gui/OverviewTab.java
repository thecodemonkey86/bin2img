/*	This program is released under European Union Public License.
 *  for details please refer to the file LICENSE
 *   
 *  Disclaimer: the program comes "as is" and with absolutely no warranty 
 *  
 *  © 2010,2011 TCM
 */

package plugin.upload.gui;

import gui.table.TableProgressBar;
import gui.util.tablemodel.ObjectTableModel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import nio.converter.ToImgInfo2;

import jbiu.model.UploadManager;


import model.transfer.TransferManager;
import model.transfer.TransferObserver;

import plugin.Script;
import plugin.ScriptNotification;
import plugin.ScriptTransferObserver;
import plugin.upload.convert.ScheduledScriptManager;
import plugin.upload.convert.ScriptToImgInfo;
import plugin.upload.convert.UploadScript;

import static gui.util.GuiUtil.*;
import static jbiu.init.JBIU.rm;


public class OverviewTab extends JPanel implements ScriptTransferObserver {

	private static final long serialVersionUID = 6056027757323748217L;

	private JTable scriptTable;
	private ObjectTableModel<Script>  tableModel;
	private JScrollPane scrollDownloadTable;
	private JPopupMenu popup; 
	private UploadPluginGUI parent;
	
	public OverviewTab(UploadPluginGUI parent) {
		this.parent=parent;
		setLayout(new BorderLayout());
		String progress=rm().getLocalizedString("colPercent");
		tableModel=new ObjectTableModel<Script>(rm().getLocalizedString("colScript"),
									rm().getLocalizedString("colStatus"),
									rm().getLocalizedString("colUploaded"),
									progress,
									rm().getLocalizedString("colSize"),
									rm().getLocalizedString("colSpeed"),
									rm().getLocalizedString("colETA")
									){

			private static final long serialVersionUID = 8922870815032308640L;
			
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		scriptTable=new JTable(tableModel);
		scriptTable.setBackground(Color.WHITE);
		scriptTable.setFillsViewportHeight(true);
		scriptTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		popup=new JPopupMenu();
		JMenuItem mDel=new JMenuItem("Delete");
		mDel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				removeSelectedScript();
			}
		});
		JMenuItem mStartStop=new JMenuItem("Start/Stop");
		mStartStop.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				startStopSelected();
			}
		});
		popup.add(mDel);
		popup.add(mStartStop);
		scriptTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger())
					popup.show(e.getComponent(), e.getX(), e.getY());
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger())
					popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
		
		scriptTable.getColumn(progress).setCellRenderer(new TableProgressBar());
		
		scrollDownloadTable = new JScrollPane(scriptTable);
		add(scrollDownloadTable,BorderLayout.CENTER);
		ScheduledScriptManager.getManager().setObserver(this);
	}
	
	
	@Override
	public void updateScriptAdded(Script info) {
		
		tableModel.addRow(info,
		  info.getName(),
		  info.getStatus().toString(),
		  formatBytes(0.0),
		  0,
		  ((UploadScript)info).isConvertFinished()?formatBytes(info.getTotalSize()):"",
		  "",
		  ""
		  
		);
		
	}

	@Override
	public void updateTransferred(Script info,TransferManager<TransferObserver>tm, long speed) {
		long bytes=tm.getTransferredBytes();
		int progress=(int) (bytes*100/info.getTotalSize());
		long time=(System.currentTimeMillis()-info.getStartTime()) / 1000+1;
		long bytesSession=bytes-info.getStartBytes();
		System.out.println(bytesSession + " "+time + " "+bytes );
		tableModel.setData(info,
				null,
				 info.getStatus().toString(),
				formatBytes(bytes),
				progress,
				formatBytes(info.getTotalSize()),
				formatSpeed(speed),
				formatETA(bytesSession/time, info.getTotalSize()-info.getStartBytes(), bytesSession)
				);
	}

	@Override
	public void updateProgress(Script info) {
		
	}

	@Override
	public void updateStopped(Script info, TransferManager<TransferObserver> tm) {
		long bytes=info.getStartBytes();
		Integer progress=null;
		
		if (((UploadScript) info).isConvertFinished()){
			progress=(int) (bytes*100/info.getTotalSize());
		} 
		
		
		tableModel.setData(info,
				null,
				 info.getStatus().toString(),
				formatBytes(bytes),
				progress,
				null,
				"",
				""
				);
		
		/*long bytes=info.getStartBytes();
		int progress=info.getFileCounter()*100/info.getNumberOfFiles();
		tableModel.setData(info,
				null,
				info.getStatus(),
				formatBytes(bytes),
				  progress,
				  formatBytes(info.getTotalSize()),
				  "",
				  ""
				);*/
	}

	@Override
	public void updateFinalizing(Script info, TransferManager<TransferObserver> tm) {
		showFinished(info);
		
	}
	
	private void showFinished(Script info){
		tableModel.setData(info,
				null,
				info.getStatus().toString(),
				formatBytes(info.getTotalSize()),
				100,
				null,
				"",
				""
				);
	}
	
	@Override
	public void updateFinished(Script info) {
		showFinished(info);
		parent.removeTab(info);
	}

//	@Override
//	public void updateConverting(Script info) {
//		System.out.println("update converting");
//		tableModel.setData(info,
//				null,
//				info.getStatus(),
//				null,
//				0,
//				null,
//				null,
//				null
//				);
//	}

	@Override
	public void updateRemove(Script info, TransferManager<TransferObserver> tm) {
		tableModel.removeRowByValue(info);
		parent.removeTab(info);
	}

	@Override
	public void updateQueued(Script info) {
		long bytes=info.getStartBytes();
//		int progress=info.getFileCounter()*100/info.getNumberOfFiles();
		tableModel.setData(info,
				null,
				info.getStatus(),
				formatBytes(bytes),
				  null,
				  null,
				  "",
				  ""
				);
	}

	public void removeSelectedScript() {
		if (scriptTable.getSelectedRow()>-1){
			try {
				ScheduledScriptManager.getManager().deleteUpload((UploadScript) tableModel.getValue(scriptTable.getSelectedRow()));
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void startStopSelected() {
		if (scriptTable.getSelectedRow()>-1)
			try {
				ScheduledScriptManager.getManager().startStop((UploadScript) tableModel.getValue(scriptTable.getSelectedRow()));
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	public void startStopAll() {
		try {
			ScheduledScriptManager.getManager().startStopAll();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void updateStartConvert(ToImgInfo2 info) {
		ScriptToImgInfo si=(ScriptToImgInfo) info;
		tableModel.setData(si.getScript(),
				null,
				"converting",
				null,
				null,
				null,
				null,
				null
				);
	}

	@Override
	public void updateFinishConvert(final ToImgInfo2 info, final int count, final int numberOfFiles) {
//		EventQueue.invokeLater(new Runnable() {
//			
//			@Override
//			public void run() {
				ScriptToImgInfo si=(ScriptToImgInfo) info;
				tableModel.setData(si.getScript(),
						null,
						null,
						null,
						count*100/numberOfFiles,
						null,
						null,
						null
						);
//			}
//		});
		
	}

//	@Override
//	public void updateStarted(Script info, TransferManager<TransferObserver> tm) {
//		UploadPluginGUI.getInstance().addTab(info, (UploadManager) tm);
//		
//	}

//	public void stopSelectedScript() {
//		ScheduledScriptManager.getManager().stop((UploadScript) tableModel.getValue(scriptTable.getSelectedRow()));
//	}

	public void updateUITree() {
		SwingUtilities.updateComponentTreeUI(this);
		SwingUtilities.updateComponentTreeUI(popup);
	}

	@Override
	public void updateStart(int numberOfFiles) {
		
	}
	
	public void removeFinished() {
		scriptTable.invalidate();
		int i=0;
		while(i<tableModel.getRowCount()) {
			if ( tableModel.getValue(i).getStatus()==ScriptNotification.finished ) {
				tableModel.removeRow(i--);
			}
			i++;
		}
		scriptTable.validate();
	}

	@Override
	public void updateSetTransferManager(Script info, TransferManager<TransferObserver> tm) {
		parent.addTab(info, (UploadManager) tm);
	}

	@Override
	public void updateFinishedAllScripts() {
		LinkCollectorGUI.getInstance().displayLinks();
	}


	@Override
	public void updateFinished(int numberOfFiles) {
		// TODO Auto-generated method stub
		
	}
}
