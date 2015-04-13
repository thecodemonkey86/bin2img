package nio.converter.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import nio.converter.ToImgInfo2;
import nio.converter.ToImgNewIO;

public class BasicToImg {
	protected ToImgNewIO toImgIo;
	protected Path outputDir;
	
	public BasicToImg() throws IOException {
		toImgIo=new ToImgNewIO();	
	}
	
	public void setOutputDir(Path outputDir) throws IOException {
		this.outputDir = outputDir;
		Files.createDirectories(outputDir);
	}
	
	public void toImg(ToImgInfo2 info) throws IOException {
		performToImg(info);
	}
	
	protected final void performToImg(ToImgInfo2 info) throws IOException {
		createOutDir(info);
		toImgIo.toImg(Files.newByteChannel(info.getAbsoluteIn()),info.getRelativeInputFileName(),info.getSegmentLength(),info.getPos(),outputDir.resolve(info.getRelativeOutput()));
	}
	

	private void createOutDir(ToImgInfo2 info) throws IOException{
		Path subdir=info.getRelativeOutput().getParent();
		if (subdir!=null){
			Path out=outputDir.resolve(subdir);
//			synchronized (outputDir) {
				if (!Files.exists(out)){
					Files.createDirectories(out);
				}
//			}
		}
	}
}
