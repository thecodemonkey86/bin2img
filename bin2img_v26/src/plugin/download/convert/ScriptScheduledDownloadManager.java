package plugin.download.convert;

import init.Bin2ImgInit;
import io.IDownloaderConfig;
import io.downloaders.FileDownloader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.util.Iterator;

import plugin.Script;
import plugin.ScriptNotification;
import plugin.download.DownloaderPlugin;
import util.exception.CancelException;

import net.util.Proxy;
import net.util.ProxySettings;
import net.util.TypeCheck;
import nio.converter.model.BasicToBin;


import model.DLInfo;
import model.JIDModel;
import model.ScheduledDownloadManager;
import model.transfer.Status;
import model.transfer.TransferEngine;
import model.transfer.linkcollector.ILinkCollectorEntry;

public class ScriptScheduledDownloadManager extends ScheduledDownloadManager {
	private Script script;
	private ScriptToBinConverter conv;
	
	protected ScriptScheduledDownloadManager(Script script, ScriptToBinConverter conv,int maxThreads, Proxy proxy) throws MalformedURLException, IOException, CancelException {
		super(maxThreads, proxy);
		this.script=script;
		this.conv=conv;
		Iterator<ILinkCollectorEntry> it=script.getEntries();
		
		while (it.hasNext()) {
			DownloadLinkInfo linkInfo = (DownloadLinkInfo) it.next();
			ScriptDLInfo dlInfo=new ScriptDLInfo(linkInfo, DownloaderPlugin.getSessionPath().resolve(script.getName()));
//			dlInfo.setFileName(linkInfo.getRelativePath());
			
			dlInfo.setFileSize(linkInfo.getSize());
			if (linkInfo.isFinished()) {
				super.filesCounter++;
				super.numberOfFiles++;
				bytesOfFinishedTransfers+=linkInfo.getSize();
			} else if (linkInfo.isDownloadOnlyFinished()){	
				if (Files.exists(dlInfo.getOutputFile())) {
					super.filesCounter++;
					super.numberOfFiles++;
					bytesOfFinishedTransfers+=linkInfo.getSize();
					BasicToBin converter=new BasicToBin();
					
					converter.toBin(dlInfo.getOutputFile());
				} else {
					linkInfo.reset();
					addDownload(dlInfo);
				}
				
			} else {
				addDownload(dlInfo);
			}
		}
		currentTotalBytes=bytesOfFinishedTransfers;
//		prevTotalBytes=currentTotalBytes;
	}

	public Script getScript() {
		return script;
	}
	
	@Override
	protected void startScheduler() throws IOException {
		TransferEngine.setProxy(ProxySettings.getProxySettings(Bin2ImgInit.settings()));
		setMaxThreads(DownloaderPlugin.getInstance().getSettings().getInt(DownloaderPlugin.SETTING_THREADS));
		script.setStartTime();
		super.startScheduler();
	}
	
	@Override
	public void addDownload(DLInfo dlInfo) throws IOException,CancelException {
		IDownloaderConfig cfg=JIDModel.getDownloaderConfigByUrl(dlInfo.getUrl());
		TransferEngine e=cfg!=null? cfg.getEngine(dlInfo):new FileDownloader(dlInfo);
		super.add(e,new ScriptDownloadThread(e));
	}
	
	
	private class ScriptDownloadThread extends DownloaderThread {

		public ScriptDownloadThread(TransferEngine d) {
			super(d);
		}
		
		@Override
		public void stop() {
			super.stop();
		}
		
		@Override
		protected boolean isValid() {
			return TypeCheck.checkPNGMagicNumberSuppressException(((ScriptDLInfo) transferInfo).getOutputFile());
		}
		
		@Override
		public void run() {
			stop=false;
			ScriptDLInfo info=(ScriptDLInfo) transferInfo;
			do {
				System.out.println("start dl");
				transferInfo.reset();
				runAction();
			} while ( transferInfo.getStatus()!=Status.finished &&!stop);
			if (!stop) {
				
				info.getLinkInfo().setDownloadOnlyFinished();
				try {
					if (filesCounter==numberOfFiles){
						script.setStatus(ScriptNotification.converting);
					}
					
					conv.toBin(info.getOutputFile(),info.getLinkInfo());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	
//	@Override
//	public long getTotalSpeed() {
//		System.out.println(currentTotalBytes +" "+prevTotalBytes);
//		return super.getTotalSpeed();
//	}

	public void setScriptThread(Object scriptThread) {
		conv.setScriptThread(scriptThread);
	}
	
}
