package plugin.download.script;

import init.Bin2ImgInit;
import io.IDownloaderConfig;
import io.downloaders.FileDownloader;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import model.DLInfo;
import model.JIDModel;
import model.transfer.TransferEngine;
import net.util.ProxySettings;
import nio.converter.model.BasicToBin;
import plugin.Script;
import plugin.ScriptIO;
import plugin.download.DownloaderPlugin;
import plugin.download.SessionLoadException;
import plugin.download.convert.DownloadLinkInfo;
import plugin.download.convert.DownloadScript;
import util.base64.Base64Decoder;
import util.base64.GzipPlusBase64Coder;
import util.exception.CancelException;
import converter.Bin2ImgModel;
import core.io.Application;

public class DownloadScriptIO {
	public static DownloadScript read(String url) throws IOException, SessionLoadException, CancelException{
		if (url.startsWith("b2i://")){
			int end=url.length();
			if (url.endsWith("/"))end--;
			url=new String(Base64Decoder.decode(url.substring(6,end)),Application.UTF8);
			if (!url.startsWith("https://")) url="http://"+url;
		}

		Path sp=Bin2ImgModel.getModel().getSessionPath("temp");
		DLInfo di=new DLInfo(url,sp ){
			@Override
			public Path getOutputFile() {
				if (fileName!=null){
					return output.resolve(fileName);
				} else {
					return null;
				}
			}
		};
		IDownloaderConfig cfg=JIDModel.getDownloaderConfigByUrl(url);
		TransferEngine.setProxy(ProxySettings.getProxySettings(Bin2ImgInit.settings()));
		if (cfg!=null)
			cfg.getEngine(di).startTransfer();
		else
			new FileDownloader(di).startTransfer();
		
		Path downloaded = di.getOutputFile();
		BasicToBin tobin=new BasicToBin();
		tobin.setOutputDir(sp);
		Path converted = tobin.toBin(downloaded);
		DownloadScript ds=read(converted);
		Files.delete(downloaded);
		Files.delete(converted);
		createSession(ds);
		return ds;
	}
	
	private static DownloadScript readInfo(Path f) throws IOException, SessionLoadException{
		
		DownloadScript si = new DownloadScript(f);
		
		if (!Files.exists(f)){
			throw new SessionLoadException(si);
		} else {
		
			List<String> l=Files.readAllLines(f, Application.UTF8 );
			
			String hdr=l.get(0).trim();
			if (hdr.equals("Encoding>Gzip+Base64")){
				String base64=l.get(l.size()-1);//single line, last list entry
				byte[] bytes=GzipPlusBase64Coder.fromGzipPlusBase64(base64);
				String decompressed=new String(bytes,Application.UTF8);
				l = Arrays.asList(decompressed.split("\n"));
			}
			
			Iterator<String> it=l.iterator();
			long startBytes=0L;
			long totalBytes=0L;
			

			while (it.hasNext()) {
				String line=it.next().trim();
				
				if (line.length()>0){
				
					String[] s=line.split(">");
					
					if (s[0].equals("Download")) {
						long size;
						String rp;
						String url;
	
						if (s.length==4){
							rp=s[1];
							url=s[2];
							size=Long.parseLong(s[3]);
						}else{ 
							throw new SessionLoadException(si);
						}
						si.addEntry(new DownloadLinkInfo(rp, url,size));
					} else if (s[0].equals("Finished")) {
						if (s.length<4) throw new SessionLoadException(si);
						long size=Long.parseLong(s[3]);
						startBytes+=size;
						DownloadLinkInfo li=new DownloadLinkInfo(s[1], s[2],size);
						si.setFinished(li);
						si.addEntry(li);
					} else if (s[0].equals("ToConvert")) {
						if (s.length<4) throw new SessionLoadException(si);
						DownloadLinkInfo li=new DownloadLinkInfo(s[1], s[2],Long.parseLong(s[3]));
						li.setDownloadOnlyFinished();
						si.addEntry(li);
					} else if (s[0].equals("AutoResume") && s[1].equals("1")) {
						si.setAutoResume();
					} else {
						throw new SessionLoadException(si);
					}
				}
			}
			if (totalBytes>0L) si.addTotalBytes(totalBytes);
			si.setStartBytes(startBytes);
		}
		return si;
	}
	
	public static void clearSession(Script s) throws IOException {
		Path path=DownloaderPlugin.getSessionPath().resolve(s.getName());
		
		Files.walkFileTree(path, new FileVisitor<Path>() {

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file );
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				try{
				Files.delete(dir);
				} catch(Exception e){
					e.printStackTrace();
				
				}
				return FileVisitResult.CONTINUE;
			}
		});
		
	}
	
	private static void createSession(DownloadScript s) throws IOException {
		String name=s.getName();
		Path sessionPath=DownloaderPlugin.getSessionPath().resolve(name);
		if (!Files.exists(sessionPath)) Files.createDirectories(sessionPath); 
		s.setFile(sessionPath.resolve(name));
		ScriptIO.writeScript(s);
	}
	
	public static List<DownloadScript> loadDownloadSessions() throws IOException {
		LinkedList<DownloadScript> scripts=new LinkedList<>();
		if (Files.exists(DownloaderPlugin.getSessionPath())) {
			
			DirectoryStream<Path> stream=Files.newDirectoryStream(
					DownloaderPlugin.getSessionPath(),
					new DirectoryStream.Filter<Path>() {
						@Override
						public boolean accept(Path f) {
							return Files.isDirectory(f);
						}
			});
			
			
			
			for (Path p:stream) {
				String fileName=p.getFileName().toString();
				Path sessionFile=p.resolve(fileName);
					try {
						DownloadScript s=readInfo(sessionFile);
						scripts.add(s);
					} catch (SessionLoadException e) {
						e.printStackTrace();
						DownloadScriptIO.clearSession(e.getScript());
					}
			}
			
			
		}
		return scripts;
	}
	
	public static DownloadScript read(Path f) throws IOException, SessionLoadException{
		DownloadScript si=readInfo(f);
		createSession(si) ;
		return si;
	}
}
