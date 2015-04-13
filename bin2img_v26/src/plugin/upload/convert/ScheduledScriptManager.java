package plugin.upload.convert;

import init.Bin2ImgInit;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jbiu.model.ConfigModel;
import jbiu.model.ULInfo;

import core.io.Application;


import plugin.ScriptLinkCollectorEntry;
import plugin.ScriptNotification;
import plugin.ScriptTransferObserver;
import plugin.upload.UploaderPlugin;
import scheduler.Scheduler2;
import scheduler.SchedulerClient;
import scheduler.SchedulerRunnable2;
import settings.Settings;

import net.util.ProxySettings;
import nio.converter.ToImgInfo2;
import nio.converter.ToImgInfoList;
import model.transfer.TransferInfo;
import model.transfer.TransferManager;
import model.transfer.TransferObserver;

public class ScheduledScriptManager implements TransferObserver,SchedulerClient{
	private static ScheduledScriptManager instance=new ScheduledScriptManager() ;
	private Scheduler2 scheduler;
	private ScriptTransferObserver observer;
	
	public static ScheduledScriptManager getManager() {
		return instance;
	}
	
	public void setObserver(ScriptTransferObserver observer) {
		this.observer = observer;
		ScriptScheduledUploadManager.setObserver(observer);
	}
	
	public ScheduledScriptManager() {
		scheduler = new Scheduler2(this,1);
	}

	public void setMaxThreads(int max) {
		scheduler.setMaxThreads(max);
	}
	
	public void deleteUpload(UploadScript s) throws IOException {
		stop(s);
		scheduler.remove(s.getId());
		Path dir=UploaderPlugin.getSessionPath(s);
		
		Files.walkFileTree(dir, new FileVisitor<Path>() {

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.deleteIfExists(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				Files.deleteIfExists(dir);
				return FileVisitResult.CONTINUE;
			}
		});
		s.setStatus(ScriptNotification.remove);
		s.getStatus().updateStatus(observer, null, s);
	}

	public void addScript( ToImgInfoList input,UploadScript s, Map<String, String> uploadTokens ) throws IOException {
		addScriptInternal(input, s, null, uploadTokens,true,true);
	}
	
	protected void addScriptInternal( ToImgInfoList input,UploadScript s,  List<ULInfo> convertFinished, Map<String, String> uploadTokens,boolean start, boolean createNew) throws IOException {
		observer.updateScriptAdded(s);
		Settings settings=UploaderPlugin.getInstance().getSettings();
		
		ScriptScheduledUploadManager up=new ScriptScheduledUploadManager(settings.getInt("maxThreads"),  ProxySettings.getProxySettings(Bin2ImgInit.settings()), s, uploadTokens);
		up.addObserver(this);
		up.addUploads(input);
		observer.updateSetTransferManager(s, up);
		
		
		if (convertFinished!=null) {
			for (ULInfo ulInfo:convertFinished){
				up.addUpload(ulInfo, s.getConfig());
			}
			up.loadSession(convertFinished.size());
		}
		
		up.setScriptThreadLock(scheduler);
		if (createNew){
			up.createSession();
		}
		
		if (start) {
			int id=scheduler.addTask(new ScriptThread(s, up));
			s.setId(id);
			s.setStatus(ScriptNotification.queued);
			s.getStatus().updateStatus(observer, up, s);
			
			if (!scheduler.isRunning()) scheduler.start();
		} else {
			int id=scheduler.addTaskStopped(new ScriptThread(s, up));
			s.setId(id);
			s.setStatus(ScriptNotification.stopped);
			s.getStatus().updateStatus(observer, up, s);
		}
		
		
	}
	
	
	protected void stop(UploadScript s) {
		s.setStatus(ScriptNotification.stopped);
		scheduler.stop(s.getId());
	}
	
	
	private class ScriptThread extends SchedulerRunnable2  {

		private UploadScript s;
		private ScriptScheduledUploadManager up;
		
		public ScriptThread(UploadScript s, ScriptScheduledUploadManager up) {
			this.s=s;
			this.up=up;
			up.setScriptThreadLock(this);
		}
		
		@Override
		public synchronized void run() {
			if (scheduler.isRunning()) {
				try {
					updateStatus(ScriptNotification.running);
					up.setProxy(ProxySettings.getProxySettings(Bin2ImgInit.settings()));
					up.start();
					wait();
					System.out.println("test");
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public void stop() {
			updateStatus(ScriptNotification.stopped);
			stop=true;
			System.out.println("stop " +s.getName());
			up.stopAll();
		}

		private void updateStatus(ScriptNotification status){
			s.setStatus(status);
			status.updateStatus(observer, up, s);
		}

		@Override
		public void enqueue() {
			updateStatus(ScriptNotification.queued);
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
	public void updateTotalSpeed(TransferManager<TransferObserver> tm,long speed) {
		ScriptScheduledUploadManager up=(ScriptScheduledUploadManager) tm;
		observer.updateTransferred(up.getScriptInfo(),up, speed);
	}


	@Override
	public void updateRemoveAll() {
		
		
	}

	@Override
	public void allDone() {
		observer.updateFinishedAllScripts();
	}

	@Override
	public void updateEnqueue(TransferInfo info) {
		
	}

	@Override
	public void updateFinishedAll() {
		
	}
	
	public void startStopAll() throws IOException{
		if (scheduler.isRunning()) {
			stopAll();
		} else {			
			Collection<SchedulerRunnable2> stopped= scheduler.getStopped();
			for (SchedulerRunnable2 thread:stopped) {
				ScriptThread t=(ScriptThread) thread;
				t.s.setStatus(ScriptNotification.queued);
				t.s.reloadConfig();
				ScriptNotification.queued.updateStatus(observer, t.up, t.s);
			}
			scheduler.enqueueAllStopped();
			scheduler.start();
		}
	}
	
	public void stopAll() throws IOException {
		scheduler.stopAll();
		for (SchedulerRunnable2 r: scheduler.getStopped()) {
			ScriptThread t= (ScriptThread)r;
			UploadScript s=t.s;
			s.setStatus(ScriptNotification.stopped);
			ScriptNotification.stopped.updateStatus(observer, t.up, s);
		}

	}
	
	public void startStop(UploadScript s) throws IOException {
		if (s.getStatus()!=ScriptNotification.finished) {
			if (s.getStatus()!=ScriptNotification.stopped)
				stop(s);
			else
				start(s);
		}
		
	}
	
/*	public void changeHost(UploadScript s) {
		if (!scheduler.isRunning(s.getId())) {
			ScriptThread t= (ScriptThread) scheduler.getStoppedById(s.getId());
			t.up.
		}
	}*/
	
	private void start(UploadScript s) {
		
		ScriptThread t= (ScriptThread) scheduler.enqueueStopped(s.getId());
		s.setStatus(ScriptNotification.queued);
		s.getStatus().updateStatus(observer, t.up, s);
		
		if (!scheduler.isRunning()) scheduler.start();
	}
	
	public void loadSessions() throws IOException {
		Path sp=UploaderPlugin.getSessionPath();
		if (Files.exists(sp)) {
			DirectoryStream<Path> stream=Files.newDirectoryStream(sp);
			Iterator<Path> sessionDirStream=stream.iterator();
			HashMap<String, String> tokens=new HashMap<>();
			try{
				
				while (sessionDirStream.hasNext()){
					Path sessPath=sessionDirStream.next();
					if (Files.isDirectory(sessPath)){
						Path sessFile=sessPath.resolve("upload.session");
						if (Files.exists(sessFile)) {
							List<String> lines = Files.readAllLines(sessFile, Application.UTF8);
							
							ULInfo currentUlInfo = null;
//							String currentTokenName = null;
							ToImgInfoList toConvert= new ToImgInfoList();
							List<ULInfo> toUpload=new LinkedList<>();
							List<ScriptLinkCollectorEntry> finished=new LinkedList<>();
							String[] t;
							String hostName=null;
							
							long totalBytes=0L;
							String scriptName=null;
							
							for (String line:lines) {
								line=line.trim();
								
								if (line.length()>0) {
									int k=line.indexOf('>');
									if (k==-1) throw new IOException();
									String prefix=line.substring(0,k);
									
									switch (prefix){
										case "Upload":
											t=line.split(">");
											currentUlInfo=new ULInfo(sessPath.resolve(t[1]), new ScriptLinkCollectorEntry(t[1]));
											toUpload.add(currentUlInfo);
											totalBytes+=currentUlInfo.getFileSize();
											break;
										case "Convert":
											t=line.split(">");
											toConvert.add(new ToImgInfo2(
													sessFile.resolve(t[1]),//in
													t[1],											
													Long.parseLong(t[2]),
													Long.parseLong(t[3]), 
													Paths.get(t[4])));
											break;
										case "#Token":
											int k2=line.indexOf('>',k+1);
											String tokenName=line.substring(k+1,k2);
											String tokenValue=line.substring(k2+1);
											tokens.put(tokenName, tokenValue);
											
											break;
										case "Finished":
											t=line.split(">");
											ScriptLinkCollectorEntry e=new ScriptLinkCollectorEntry(t[1]);
											e.setSize(Files.size(sessPath.resolve(t[1])));
											finished.add(e);
											e.setLink(0, t[2]);
											break;
										case "Host":
											hostName=line.substring(k+1);
											break;
										case "Script":
											scriptName=line.substring(k+1);
											break;
									}
								}
							}
							int numberOfFiles=toConvert.size()+toUpload.size()+finished.size();
							if (scriptName==null) continue;
							
							UploadScript s=new SingleHostUploadScript(UploaderPlugin.getSessionPath().resolve(scriptName).resolve(scriptName),hostName!=null?ConfigModel.getUploaderConfig(hostName):ConfigModel.getSimulator(), numberOfFiles); 
							long startBytes=0L;
							for (ScriptLinkCollectorEntry e:finished){
								s.addEntry(e);
								totalBytes+=e.getSize();
								startBytes+=e.getSize();		
							}		
							
							for (ULInfo info:toUpload){
								for (String token:tokens.keySet()){
									info.setFileToken(token, tokens.get(token));
								}
							}
							
							s.addTotalBytes(totalBytes);
							s.setStartBytes(startBytes);
							addScriptInternal(toConvert,s,toUpload, tokens,false,false);
							
						}
					}
					
				}			
			} finally{
				stream.close();				
			}
		}
	}

	@Override
	public void updateStopAllTransfers() {
		
	}
}
