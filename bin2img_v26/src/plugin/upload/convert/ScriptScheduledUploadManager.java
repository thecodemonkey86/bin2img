package plugin.upload.convert;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import converter.Bin2ImgModel;

import core.io.Application;

import plugin.ScriptIO;
import plugin.ScriptLinkCollectorEntry;
import plugin.ScriptNotification;
import plugin.ScriptTransferObserver;
import plugin.upload.UploaderPlugin;

import scheduler.SchedulerRunnable2;
import settings.Settings;
import util.exception.CancelException;

import model.transfer.Status;
import model.transfer.TransferEngine;
import model.transfer.linkcollector.ILinkCollectorEntry;
import model.transfer.linkcollector.LinkCollectorEntry;
import net.util.Proxy;
import nio.converter.ToImgInfo2;
import nio.converter.ToImgInfoList;
import nio.converter.model.ScheduledToImg2;
import jbiu.model.ScheduledUploadManager;
import jbiu.model.ULInfo;

public class ScriptScheduledUploadManager extends ScheduledUploadManager {

	private UploadScript script;
	private ScheduledToImg2 toImg;
	private static ScriptTransferObserver observer;
	private Object scriptThreadLock;
	private LinkCreator afterUploadAction;
	private Map<String, String> uploadTokens;
	
	public static void setObserver(ScriptTransferObserver observer) {
		ScriptScheduledUploadManager.observer = observer;
	}
	
	public void setScriptThreadLock(Object scriptThreadLock) {
		this.scriptThreadLock = scriptThreadLock;
	}
	
	public ScriptScheduledUploadManager(int maxThreads, Proxy proxy, UploadScript s,final Map<String, String> uploadTokens) throws IOException {
		super(maxThreads, proxy);
		this.script=s;
		this.uploadTokens=uploadTokens;
		bytesOfFinishedTransfers = s.getStartBytes();
		currentTotalBytes=bytesOfFinishedTransfers;
		
		toImg=new ScheduledToImg2(maxThreads) {
			
			
			@Override
			protected void postConvertAction(ToImgInfo2 info) throws IOException {
				ULInfo ulInfo= new ULInfo(outputDir.resolve( info.getRelativeOutput()),new ScriptLinkCollectorEntry( info.getRelativeOutput()));
				script.addTotalBytes(ulInfo.getFileSize());
				ulInfo.setFileTokens(uploadTokens);
				addUpload(ulInfo, script.getConfig());
			}
			
			@Override
			protected void cancelConvertAction(ToImgInfo2 info) throws IOException {
			}
			
			@Override
			public void allDone() {
				try {
					script.setConvertFinished(true);
					script.setStatus(ScriptNotification.running);
					ScriptScheduledUploadManager.this.startAll();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		};
		toImg.setObserver(observer);
		afterUploadAction=new HostLinkCreator(script.getConfig(),uploadTokens);
		script.setConvertFinished(true);
	}

	public void addUploads(ToImgInfoList input) throws IOException {
		if (input.size()>0 ) {
			script.setConvertFinished(false);
			for (ToImgInfo2 i:input) {
				toImg.toImg(new ScriptToImgInfo(i, script));
			}
		}
	}
	
	
	public synchronized void start() throws IOException {
		if (script.isConvertFinished()){
			script.setStatus(ScriptNotification.running);
			scheduler.enqueueAllStopped();
			startScheduler();
		} else {
			script.setStatus(ScriptNotification.converting);
			toImg.setOutputDir(UploaderPlugin.getSessionPath(script));
			toImg.start();
		}
	}
	
	public UploadScript getScriptInfo() {
		return script;
	}
	
	@Override
	public synchronized void stopAll() {
		super.stopAll();
		toImg.cancel();
		try {
			saveSession();
			
			script.setStartBytes(bytesOfFinishedTransfers);
		} catch (IOException e) {
			e.printStackTrace();
		}
		synchronized (scriptThreadLock) {
			scriptThreadLock.notify();
		}
	}
	
	private class ScriptUploadThread extends UploaderThread {

		public ScriptUploadThread(TransferEngine uploader) {
			super(uploader);
		}
		
		@Override
		protected void setLinkCollector() {
			lc=script;
		}
		
		@Override
		public void runAction() {
			if (script.getStatus()!=ScriptNotification.error){
				super.runAction();
				if (transferInfo.getStatus() ==Status.stopped){
					ScheduledScriptManager.getManager().stop(script);
				} else if (transferInfo.getError()!=null ){
					script.setStatus(ScriptNotification.error);
				}
				
			}
		}
	}
	
	@Override
	protected SchedulerRunnable2 createThread(TransferEngine d) {
		return new ScriptUploadThread(d);
	}

	@Override
	protected void startScheduler() throws IOException{
		saveSession();
		script.setStartTime();
		super.startScheduler();
	}
	
	@Override
	public void allDone() {
		if (script.getStatus()!=ScriptNotification.error){
			super.allDone();
			try {
				Settings settings= UploaderPlugin.getInstance().getSettings();
				Path out=Bin2ImgModel.getModel().getOutputImg().resolve(script.getName());
				Path sessionPath=UploaderPlugin.getSessionPath(script);
//				ScriptIO.writeScriptBase64(script);
				
				script.setStatus(ScriptNotification.finalizing);
				script.getStatus().updateStatus(observer, this, script);
				
				LinkCollectorEntry e=afterUploadAction.create(script);
			
				UploadScript.getScriptLinkCollector().addEntry(e);
				
				if (settings.getBoolean(UploaderPlugin.SETTING_KEEP_IMAGES)){
					Files.createDirectories(out);
					Iterator<ILinkCollectorEntry> it=script.getEntries();
					
					
					while (it.hasNext()){
						Path source=it.next().getFile();
						Path target=out.resolve(source);
						
						if (source.getNameCount()>1){
							Files.createDirectories(target.getParent());
						}
						
						Files.move(sessionPath.resolve(source), target, StandardCopyOption.REPLACE_EXISTING);
					}
					
				}
	
				if (settings.getBoolean(UploaderPlugin.SETTING_SAVE_SCRIPT_TO_DISK)){
					Path scriptOutPath=settings.getPath(UploaderPlugin.SETTING_SAVE_SCRIPT_PATH);
					Files.createDirectories(scriptOutPath);
					ScriptIO.writeScriptBase64(script,scriptOutPath.resolve(script.getName()));
				}
				
//				if (settings.getBoolean(UploaderPlugin.SETTING_SAVE_LINKS)){
//					Path dest=settings.getPath(UploaderPlugin.SETTING_SAVE_LINKS_PATH);
//					Files.createDirectories(dest.getParent());
//					Files.write(dest, (e.getLink(0)+"\n").getBytes(Application.UTF8), StandardOpenOption.APPEND,StandardOpenOption.CREATE,StandardOpenOption.WRITE);
//				}
				
				// delete remaining files and session path
				Files.walkFileTree(sessionPath, new FileVisitor<Path>() {
	
					@Override
					public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
						return FileVisitResult.CONTINUE;
					}
	
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						Files.delete(file);
						return FileVisitResult.CONTINUE;
					}
	
					@Override
					public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
						return FileVisitResult.TERMINATE;
					}
	
					@Override
					public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
						Files.delete(dir);
						return FileVisitResult.CONTINUE;
					}
				});
				script.setStatus(ScriptNotification.finished);
				script.getStatus().updateStatus(observer, this, script);
				
				synchronized (scriptThreadLock) {
					scriptThreadLock.notify();
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (CancelException e) {
				e.printStackTrace();
			}
		}
		
	}

	public void loadSession(int convertFinished) throws IOException {
		toImg.loadSession(convertFinished);
	}
	
	static final char NEWLINE='\n';
	
	public void createSession() throws IOException {
		Path p=UploaderPlugin.getSessionPath().resolve(script.getName());
		Files.createDirectories(p);
		BufferedOutputStream out=new BufferedOutputStream(Files.newOutputStream(p.resolve("upload.session"),Application.DEFAULT_WRITE_OPTIONS));
		toImg.saveSession(out);
		out.close();
	}
	
	private void saveSession() throws IOException {
		Path p=UploaderPlugin.getSessionPath();
		BufferedOutputStream out=new BufferedOutputStream(Files.newOutputStream(p.resolve(script.getName()).resolve("upload.session"),Application.DEFAULT_WRITE_OPTIONS));
		
		Collection<SchedulerRunnable2> unfinished=scheduler.getUnfinished();
		
		Iterator<ILinkCollectorEntry> itFinished= script.getEntries();
		write(out,"Script>");
		write(out, script.getName());
		write(out,NEWLINE);
		
		if (script.getConfig().getInternalName()!=null){
			write(out, "Host>"+script.getConfig().getInternalName());
			write(out,NEWLINE);
		}
		while (itFinished.hasNext()) {
			ScriptLinkCollectorEntry e=(ScriptLinkCollectorEntry) itFinished.next();
			write(out, "Finished>");
			write(out, e.getFile().toString());
			write(out,'>');
			write(out,e.getLink(0));
			write(out,NEWLINE);
		}
		
		for (SchedulerRunnable2 thread:unfinished) {
			ScriptUploadThread t=(ScriptUploadThread) thread;
			ULInfo ulInfo= (ULInfo) t.getTransferInfo();
			write(out, "Upload>");
			write(out, ulInfo.getFileName());
			write(out,NEWLINE);
			
		}
		if (uploadTokens!=null){
			for (String token:uploadTokens.keySet()) {
				write(out,"#Token>");
				write(out,token);
				write(out,'>');
				write(out,uploadTokens.get(token));
				write(out,NEWLINE);
			}
		}
		toImg.saveSession(out);
		out.close();
	}
	
	private static void write(OutputStream out,String s) throws IOException{
		out.write(s.getBytes(Application.UTF8));
	}
	
	private static void write(OutputStream out,char c) throws IOException {
		out.write(c);
	}
	
}
