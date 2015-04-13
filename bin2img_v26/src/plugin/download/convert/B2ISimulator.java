package plugin.download.convert;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import util.exception.CancelException;
import model.DLInfo;
import model.transfer.EngineConfig;
import model.transfer.Status;
import init.Bin2ImgInit;
import io.downloaders.Downloader;

public class B2ISimulator extends Downloader{

	public B2ISimulator(DLInfo info, EngineConfig cfg) {
		super(info, cfg);
	}

	private long b=0;
	boolean c;

	public void startTransfer() throws IOException, CancelException {
		c=false;
		b=0;
		int bps=16384;
		byte [] buffer=new byte[bps];
		Path p=Bin2ImgInit.getInstance().getExecPath().resolveSibling("test/nio_test").resolve(dlInfo.getUrl());
		System.out.println(Files.exists(p));
		BufferedInputStream in=new BufferedInputStream(Files.newInputStream(p,StandardOpenOption.READ)); 
		BufferedOutputStream out=new BufferedOutputStream(Files.newOutputStream(dlInfo.getOutputFile(),StandardOpenOption.TRUNCATE_EXISTING,StandardOpenOption.CREATE,StandardOpenOption.WRITE));
		
		int r=in.read(buffer);
		while (r>-1) {
			if (dlInfo.getStatus()==Status.stopped) {
				throw new CancelException();
			}
			
			out.write(buffer,0,r);
			r=in.read(buffer);
			b+=bps;
			
			try {
				
				Thread.sleep(30);
			} catch (InterruptedException e) {
			}
			if (c){
				b=0;
				throw new CancelException(); 
			}
		}
		out.close();
	}

	@Override
	public void updateCurrentBytes() {
		dlInfo.updateBytes(b);
	}
	
	@Override
	public void cancel() {
		
		c=true;
	}
	
}
