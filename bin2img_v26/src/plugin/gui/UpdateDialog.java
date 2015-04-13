package plugin.gui;

import java.awt.BorderLayout;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import init.Bin2ImgInit;
import gui.okcancelpanel.ClosePanel;
import gui.util.GuiUtil;
import gui.util.logpanel.LogPanel;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import jbiu.gui.GUIConfigLoader2;
import jbiu.model.ConfigModel;

import model.JIDModel;

public class UpdateDialog extends JDialog {

	private static final long serialVersionUID = 2989409113459628503L;
	
	private LogPanel lp;
	private ClosePanel close;
	
	public UpdateDialog() {
		super(null,"Update",ModalityType.APPLICATION_MODAL);
		lp=new LogPanel();
		close=new ClosePanel(this, Bin2ImgInit.rm().getLocalizedString("close"));
		JPanel cp=(JPanel)getContentPane();
		cp.setLayout(new BorderLayout());
		cp.setBorder(GuiUtil.DEFAULT_EMPTY_BORDER);
		cp.add(lp,BorderLayout.CENTER);
		cp.add(close,BorderLayout.SOUTH);
		close.getCloseButton().setEnabled(false);
		setSize(400, 200);
		
		new SwingWorker<IOException, String>() {
			
			@Override
			protected IOException doInBackground() throws Exception {
				try{
					update("pluginUpdateCheck","Downloader");
					if (JIDModel.downloadUpdates()){
						update("pluginUpdateAvailable",null);
					}
					update("pluginUpdateCheck","Uploader");
					boolean b=ConfigModel.checkForUpdates();
					b=b|GUIConfigLoader2.downloadUpdates(false);
					if (b){
						update("pluginUpdateAvailable",null);
					}
				} catch (IOException e){
					return e;
				}
				
				return null;
			}
			
			private void update(String resName,String type){
				process(Arrays.asList(Bin2ImgInit.rm().getLocalizedString(resName)+(type!=null?" "+type:"")));
			}
			@Override
			protected void process(List<String> chunks) {
				lp.addToLog(chunks.get(0));
			}
			
			@Override
			protected void done() {
				try {
					IOException e=get();
					
					if (e!=null){
						lp.addToLog(Bin2ImgInit.rm().getLocalizedString("statusError"));
					} else {
						lp.addToLog(Bin2ImgInit.rm().getLocalizedString("finishedAll"));
					}
					close.getCloseButton().setEnabled(true);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		}.execute();
		
		setVisible(true);
	}

}
