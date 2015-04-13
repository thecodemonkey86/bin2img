package nio.converter;

import java.nio.file.Path;

import util.StringUtil;

public class ToImgInfo2 {
	protected Path absoluteInput,relativeOutput;
	protected long segmentLength, pos;
	protected String relativeInputFileName;
	
	public ToImgInfo2(Path absoluteInput,String relativeInputFileName, long segmentLength, long pos,Path relativeOutput) {
		this.absoluteInput=absoluteInput;
		this.relativeOutput=relativeOutput;
		this.segmentLength=segmentLength;
		this.pos=pos;
		this.relativeInputFileName=StringUtil.replaceAll(relativeInputFileName, '\\', '/');
	}
	
	public String getRelativeInputFileName() {
		return relativeInputFileName;
	}
	
	public Path getAbsoluteIn() {
		return absoluteInput;
	}
	
	public Path getRelativeOutput() {
		return relativeOutput;
	}
	public long getPos() {
		return pos;
	}
	
	public long getSegmentLength() {
		return segmentLength;
	}
}
