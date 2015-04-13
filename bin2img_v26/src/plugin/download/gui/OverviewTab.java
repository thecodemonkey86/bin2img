/*	This program is released under European Union Public License.
 *  for details please refer to the file LICENSE
 *   
 *  Disclaimer: the program comes "as is" and with absolutely no warranty 
 *  
 *  © 2010,2011 TCM
 */

package plugin.download.gui;

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

import model.DownloadManager;
import model.transfer.TransferManager;
import model.transfer.TransferObserver;


import plugin.Script;
import plugin.ScriptNotification;
import plugin.ScriptTransferObserver;
import plugin.download.convert.DownloadScript;
import plugin.download.convert.ScriptManager;
import settings.Settings;
import settings.SettingsLoadable;

import static plugin.download.DownloaderPlugin.rm;
import static gui.util.GuiUtil.*;

public class OverviewTab extends JPanel implements ScriptTransferObserver,SettingsLoadable {

	private static final long serialVersionUID = 1L;

	private JTable scriptTable;
	private ObjectTableModel<DownloadScript> tableModel;
	private JScrollPane scrollDownloadTable;
	private ScriptProcessorTab tab;
	private JPopupMenu popup;
	
	private JMenuItem mStartStop,mDelete,mStartStopAll,mRemoveFinished,mOpenBrowser;
	
	
	
	public OverviewTab(ScriptProcessorTab tab) {
		this.tab=tab;
		setLayout(new BorderLayout());
		String progress=rm().getLocalizedString("colFinished");
		tableModel=new ObjectTableModel<DownloadScript>(rm().getLocalizedString("colScript"),
									rm().getLocalizedString("colStatus"),
									rm().getLocalizedString("colDownloaded"),
									progress,
									rm().getLocalizedString("colSize"),
									rm().getLocalizedString("colSpeed"),
									rm().getLocalizedString("colETA")
		){

			private static final long serialVersionUID = -3179397955174002911L;
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		}
									
									;
		scriptTable=new JTable(tableModel);
		scriptTable.setBackground(Color.WHITE);
		scriptTable.setFillsViewportHeight(true);
		scriptTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		scriptTable.getColumn(progress).setCellRenderer(new TableProgressBar());
		
		scrollDownloadTable = new JScrollPane(scriptTable);
		
		popup=new JPopupMenu();
		mStartStop=new JMenuItem(rm().getLocalizedString("scriptStartStop"));
		mDelete=new JMenuItem(rm().getLocalizedString("scriptDelete"));
		mStartStopAll=new JMenuItem(rm().getLocalizedString("scriptStartStopAll"));
		mRemoveFinished=new JMenuItem(rm().getLocalizedString("scriptRemoveFinished"));
		mOpenBrowser=new JMenuItem(rm().getLocalizedString("scriptBrowser"));
		popup.add(mStartStop);
		popup.add(mDelete);
		popup.add(mStartStopAll);
		popup.add(mRemoveFinished);
		popup.add(mOpenBrowser);
		
		mStartStop.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				startStopSelected();			
			}
		});
		
		mStartStopAll.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
						
			}
		});
		
		mOpenBrowser.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				/*try {
					
					Bin2ImgInit.getInstance().	
				} catch (IOException ioex) {
					JOptionPane.showMessageDialog(null, ioex.getMessage());
				}*/
			}
		});
		
		mDelete.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteSelectedScript();				
			}
		}); 
		
		scriptTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				showPopup(e);
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
				showPopup(e);
			}
			
			private void showPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					popup.show(scriptTable, e.getX(), e.getY());
				}
			}

		});
		mRemoveFinished.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				removeFinished();				
			}
		});
		//add(bPanel,BorderLayout.NORTH);
		
		
		
		add(scrollDownloadTable,BorderLayout.CENTER);
		ScriptManager.getManager().setObserver(this);
		
	}
	
//	private ScriptInfo getSelectedScriptId() {
//		return tableModel.getValue(scriptTable.getSelectedRow());
//	}
	

	public void startStopSelected() {
		int row=scriptTable.getSelectedRow();
		if (row>-1)
			ScriptManager.getManager().startStop(tableModel.getValue(row));
	}
	
	public void deleteSelectedScript() {
		int[] selected=scriptTable.getSelectedRows();
		Script[] scripts=new Script[selected.length];
		
		for (int i=0;i<selected.length;i++){
			scripts[i]=tableModel.getValue(selected[i]);
		}
		
		for (int i=0;i<selected.length;i++){
			Script s=scripts[i];
			try {
				ScriptManager.getManager().delete(s);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
//		int scriptId=getSelectedScriptId();
//		if (scriptId>-1)
//			DownloaderScriptManager.getManager().delete(scriptId);		
	}
	@Override
	public void saveSettings(Settings settings) {
		int[] columnWidth=new int[scriptTable.getColumnCount()];
		
		for (int i=0;i<columnWidth.length;i++) {
			columnWidth[i]=scriptTable.getColumnModel().getColumn(i).getPreferredWidth();
		}
		settings.set("overviewColumnWidth", columnWidth);
		int[] columnPos=new int[scriptTable.getColumnCount()];

		for (int i=0;i<scriptTable.getColumnCount();i++) {
			columnPos[i]=tableModel.findColumn(scriptTable.getColumnModel().getColumn(i).getHeaderValue().toString());
		}
		settings.set("overviewColumnPos",columnPos);
	}	
	
	@Override
	public void loadSettings(Settings settings) {
		int[] colPos=settings.getIntArray("overviewColumnPos");
		if (colPos!=null) {
		for (int i=0;i<colPos.length;i++) {
			
			int p=colPos[i];
			
			int k=scriptTable.getColumnModel().getColumnIndex(tableModel.getColumnName(p));
			if (k!=i ) {
				scriptTable.getColumnModel().moveColumn(k,i);
			}

		}
		}
		int[] colWidth=settings.getIntArray("overviewColumnWidth");
		if (colWidth!=null ) {
			for (int i=0;i<colWidth.length;i++) {
			
				int w=colWidth[i];
				if (w>0) {
					scriptTable.getColumnModel().getColumn(i).setPreferredWidth(w);
				}
			}
		
		}
	}
	
	public void updateUITree() {
		SwingUtilities.updateComponentTreeUI(this);
		SwingUtilities.updateComponentTreeUI(popup);
	}


	@Override
	public void updateScriptAdded(Script info) {
//		String sessionFile= ((DownloadScript) info).getSessionFile().getFileName().toString();
		long bytes=info.getStartBytes();
		tableModel.addRow((DownloadScript)info,
				info.getName(),
				  info.getStatus().toString(),
				  formatBytes(bytes),
				  info.getFileCounter()*100/info.getNumberOfFiles(),
				  formatBytes(info.getTotalSize()),
				  "",
				  formatETA(0, info.getTotalSize(),bytes)
				  
				);
		System.out.println("row added");
	}

	@Override
	public void updateTransferred(Script info, TransferManager<TransferObserver> tm,long speed) {
		long bytes=tm.getTransferredBytes();
		long time=(System.currentTimeMillis()-info.getStartTime()) / 1000+1;
		long bytesSession=bytes-info.getStartBytes();
		tableModel.setData((DownloadScript)info,
				null,
				 info.getStatus().toString(),
				formatBytes(bytes),
				info.getFileCounter()*100/info.getNumberOfFiles(),
				formatBytes(info.getTotalSize()),
				formatSpeed(speed),
				formatETA(bytesSession/time, info.getTotalSize()-info.getStartBytes(), bytesSession)
				);
	}

	@Override
	public void updateProgress(Script info) {
		tableModel.setData((DownloadScript)info,
				null,
				null,
				null,
				info.getFileCounter()*100/info.getNumberOfFiles(),
				null,
				null,
				null
				);
	}

	
	@Override
	public void updateFinalizing(Script info, TransferManager<TransferObserver> tm) {
		setFinishStatus(info);
	}
	
	private void setFinishStatus(Script info){
		tableModel.setData((DownloadScript)info,
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
		setFinishStatus(info);
		tab.removeTab((DownloadScript) info);
	}

	@Override
	public void updateStopped(Script info,TransferManager<TransferObserver> tm) {
		long bytes=tm.getTransferredBytes();
		tableModel.setData((DownloadScript)info,
				null,
				 info.getStatus().toString(),
				formatBytes(bytes),
				info.getFileCounter()*100/info.getNumberOfFiles(),
				formatBytes(info.getTotalSize()),
				"",
				""
				);
	}

//	@Override
//	public void updateConverting(Script info) {
//		
//	}

	@Override
	public void updateRemove(Script info, TransferManager<TransferObserver> tm) {
		tableModel.removeRowByValue((DownloadScript)info);
		this.tab.removeTab((DownloadScript) info);
	}

	@Override
	public void updateQueued(Script info) {
		tableModel.setData((DownloadScript)info,
				null,
				info.getStatus(),
				null,
				null,
				null,
				null,
				null
				);
	}

	


	@Override
	public void updateFinishConvert(ToImgInfo2 info, int count, int numberOfFiles) {
	}

	@Override
	public void updateStartConvert(ToImgInfo2 info) {
		
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
	public void updateStart(int numberOfFiles) {
		
	}

	@Override
	public void updateSetTransferManager(final Script info, final TransferManager<TransferObserver> tm) {
//		EventQueue.invokeLater(new Runnable() {
//			
//			@Override
//			public void run() {
//				tab.addTab(info, (DownloadManager) tm);
//			}
//		});
		tab.addTab(info, (DownloadManager) tm);
		
	}

	@Override
	public void updateFinishedAllScripts() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateFinished(int numberOfFiles) {
		// TODO Auto-generated method stub
		
	}

	

}
