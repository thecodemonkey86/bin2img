package plugin.download.convert;


import init.Bin2ImgInit;

import java.io.IOException;

import converter.Bin2ImgModel;
import net.util.ProxySettings;

import plugin.Script;
import plugin.ScriptIO;
import plugin.ScriptNotification;
import plugin.ScriptTransferObserver;
import plugin.download.DownloaderPlugin;
import plugin.download.script.DownloadScriptIO;
import scheduler.Scheduler2;
import scheduler.SchedulerClient;
import scheduler.SchedulerRunnable2;
import util.exception.CancelException;

import model.transfer.TransferInfo;
import model.transfer.TransferManager;
import model.transfer.TransferObserver;;

public class ScriptManager implements TransferObserver,SchedulerClient{
	private static ScriptManager instance=new ScriptManager() ;
	private Scheduler2 scheduler;
	private ScriptTransferObserver observer;
	
	public static ScriptManager getManager() {
		return instance;
	}
	
	public void setObserver(ScriptTransferObserver observer) {
		this.observer = observer;
	}
	
	public ScriptManager() {
		scheduler = new Scheduler2(this,1);
	}

	public void addScript(DownloadScript s) throws IOException, CancelException {
		observer.updateScriptAdded(s);
		int maxThreads=DownloaderPlugin.settings().getInt(DownloaderPlugin.SETTING_THREADS);
		//maxThreads=1;
		
		ScriptToBinConverter conv=new ScriptToBinConverter(maxThreads, s);
		conv.setOutputDir(Bin2ImgModel.getModel().getOutputBin());
		ScriptToBinConverter.setObserver(observer);
		ScriptScheduledDownloadManager dm=new ScriptScheduledDownloadManager(s, conv, maxThreads,ProxySettings.getProxySettings(Bin2ImgInit.settings()));
		
		dm.addObserver(this);
		
		int id=scheduler.addTaskStopped(new ScriptThread(s, dm));
		s.setId(id);
		observer.updateSetTransferManager(s, dm);
	}
	
	public ScriptThread stop(Script s) {
		return (ScriptThread) scheduler.stop(s.getId());
	}
	
	
	private class ScriptThread extends SchedulerRunnable2  {

		private Script s;
		private ScriptScheduledDownloadManager dm;
		
		public ScriptThread(Script s, ScriptScheduledDownloadManager dm) {
			this.s=s;
			this.dm=dm;
			dm.setScriptThread(this);
		}
		
		@Override
		public synchronized void run() {
			
			try {
				updateStatus(ScriptNotification.running);
				dm.startAll();
				wait();
				DownloadScriptIO.clearSession(s);
				System.out.println("test");
			} catch (InterruptedException | IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void stop() {
			updateStatus(ScriptNotification.stopped);
			dm.stopAll();
			try {
				ScriptIO.writeScript(s);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}

		@Override
		public void enqueue() {
			updateStatus(ScriptNotification.queued);
		}

		private void updateStatus(ScriptNotification status){
			s.setStatus(status);
			status.updateStatus(observer, dm, s);
		}

		@Override
		public boolean errorOccoured() {
			return false;
		}
		
	}

	@Override
	public void updateAddTransfer(TransferInfo info) {
		
		
	}

	@Override
	public void updateRemoveTransfer(TransferInfo info) {
		
		
	}

	@Override
	public void updateStopTransfer(TransferInfo info) {
		
		
	}

	@Override
	public void updateTransferFinished(TransferInfo info) {
		
		
	}

	@Override
	public void updateSpeed(TransferInfo info) {
		
		
	}

	@Override
	public void updateTotalSpeed(TransferManager<TransferObserver> tm, long speed) {
		ScriptScheduledDownloadManager dm=(ScriptScheduledDownloadManager) tm;
		observer.updateTransferred(dm.getScript(),dm, speed);
	}


	@Override
	public void updateRemoveAll() {
		
		
	}

	@Override
	public void allDone() {
		
	}

	public void stopAll() {
		scheduler.stopAll();
	}

	public void delete(Script s) throws IOException {
		ScriptThread t;
		if (s.getStatus()!=ScriptNotification.stopped){
			t=stop(s);
		}else {
			t=(ScriptThread) scheduler.getStoppedById(s.getId());
		}
		if (t!=null) {
			scheduler.remove(s.getId());
			DownloadScriptIO.clearSession(s);
			s.setStatus(ScriptNotification.remove);
			ScriptNotification.remove.updateStatus(observer, t.dm, s);
		}
	}

	public void start(DownloadScript s) {
		
		ScriptThread t= (ScriptThread) scheduler.enqueueStopped(s.getId());
		s.setStatus(ScriptNotification.queued);
		s.getStatus().updateStatus(observer, t.dm, s);
		if (!scheduler.isRunning()) scheduler.start();
	}

	public void startStop(DownloadScript s) {
		if (s.getStatus()!=ScriptNotification.finished) {
			if (s.getStatus()!=ScriptNotification.stopped)
				stop(s);
			else
				start(s);
		}
		
	}
	
//	public void removeFinished() {
//		
//	}

	public void startStopAll(){
		if (scheduler.isRunning()) {
			stopAll();
		} else {
			scheduler.enqueueAllStopped();
			scheduler.start();
		}
	}

	@Override
	public void updateEnqueue(TransferInfo info) {
		
	}

	@Override
	public void updateFinishedAll() {
		
	}

	@Override
	public void updateStopAllTransfers() {
		
	}
}
