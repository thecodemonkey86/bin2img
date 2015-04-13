package nio.converter.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import nio.converter.ToBinNewIO;

public class BasicToBin {
	protected ToBinNewIO io;
	protected Path outputDir;
	
	public BasicToBin() {
		io=new ToBinNewIO();
	}
	
	public void setOutputDir(Path outputDir) throws IOException {
		this.outputDir = outputDir;
		Files.createDirectories(outputDir);
	}
	
	public Path toBin(Path in) throws IOException {
		return perform(in);
	}
	
	protected Path perform(Path in) throws IOException {
		return io.toBin(in, outputDir);
	}
}
