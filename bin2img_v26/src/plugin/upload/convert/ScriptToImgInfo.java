package plugin.upload.convert;

import plugin.Script;

import nio.converter.ToImgInfo2;

public class ScriptToImgInfo extends ToImgInfo2{
	private Script script;
	
	public ScriptToImgInfo(ToImgInfo2 toImgInfo,Script script) {
		super(toImgInfo.getAbsoluteIn(),toImgInfo.getRelativeInputFileName(), toImgInfo.getSegmentLength(), toImgInfo.getPos(), toImgInfo.getRelativeOutput());
		this.script=script;
	}
	
	public Script getScript() {
		return script;
	}
	

	@Override
	public String toString() {
		StringBuilder sb=new StringBuilder("Convert>");
		sb.append(absoluteInput.toString());		
		sb.append('>');
		sb.append(segmentLength);
		sb.append('>');
		sb.append(pos);
		sb.append('>');
		sb.append(relativeOutput);
		return sb.toString();
	}

}
