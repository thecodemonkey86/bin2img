package plugin.download.convert;

import java.io.IOException;
import java.nio.file.Path;

import plugin.ScriptNotification;
import plugin.ScriptTransferObserver;


import nio.converter.model.ScheduledToBin2;

public class ScriptToBinConverter extends ScheduledToBin2 {
	private static ScriptTransferObserver observer;
	private DownloadScript info;
	private Object scriptThread;
	
	public static void setObserver(ScriptTransferObserver observer) {
		ScriptToBinConverter.observer = observer;
	}
	
	public void setScriptThread(Object scriptThread) {
		this.scriptThread = scriptThread;
	}
	
	public ScriptToBinConverter(int threads,DownloadScript info) throws IOException{
		super(threads);
		this.info=info;
	}
	
//	public ScriptToBinConverter(int threads,DownloadScript info,Path outputDir) throws IOException {
//		super(threads,outputDir);
//		this.info=info;
//	}
	
	@Deprecated 
	@Override
	public Path toBin(Path file) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Deprecated 
	@Override
	public void toBin(Path[] files) throws IOException {
		throw new UnsupportedOperationException();
	}
	
	public void toBin(Path file,final DownloadLinkInfo li) throws IOException {
		scheduler.addTask(new ConvertThread(file){
		@Override
		public void run() {
			try {
				perform(file);
				info.setFinished(li);
				observer.updateProgress(info);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		});
		if (!scheduler.isRunning()) scheduler.start(); 
	}
	
	@Override
	public void allDone() {
		if (info.getFileCounter()==info.getEntryCount()) {

			info.setStatus(ScriptNotification.finished);
			ScriptNotification.finished.updateStatus(observer, null, info);
			synchronized (scriptThread) {
				scriptThread.notify();
			}

		}
	}
	

}
