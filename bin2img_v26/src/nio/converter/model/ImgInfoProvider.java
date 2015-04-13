package nio.converter.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import common.util.Util;

import nio.converter.ToImgInfo2;
import nio.converter.ToImgInfoList;

public class ImgInfoProvider {
	public static ToImgInfoList get(Path inputSourceDir, Path[] input,String subDir,final long splitBytes) throws IOException {
		ToImgInfoList toImgInfos=new ToImgInfoList();
		if (splitBytes>0) {
			for (Path f:input) {
				long l=Files.size(f);
				long pos=0;
				int counter=0;
				boolean finished=false;
				while (!finished) {
					counter++;
					String s=Util.formatFileNumbering(counter);
					String postFix="." +s+".png";
					
					if (l>splitBytes) {
						l-=splitBytes;
						toImgInfos.add(getToImgInfo(inputSourceDir,f,  splitBytes, pos, postFix));
						pos+=splitBytes;
					} else {
						toImgInfos.add(getToImgInfo(inputSourceDir,f,l, pos,postFix));
						finished=true;
					}
				}
				
			}
		} else {
			for (Path f:input) {
				toImgInfos.add(getToImgInfo(inputSourceDir,f, ".png"));
			}
		}
		
		return toImgInfos;
	}
	
	private static ToImgInfo2 getToImgInfo(Path sourceDir, Path in,long segmentLength, long pos,String postFix) {
		Path relIn=sourceDir.relativize(in);
		return new ToImgInfo2(in,relIn.toString(), segmentLength, pos, Paths.get(relIn.toString()+postFix));
	}
	
	private static ToImgInfo2 getToImgInfo(Path sourceDir, Path in,String postFix) throws IOException {
		Path relIn=sourceDir.relativize(in);
		return new ToImgInfo2(in,sourceDir.relativize(in).toString(), Files.size(in), 0, Paths.get(relIn.toString()+postFix));
	}
	
	public static ToImgInfoList single(ToImgInfo2 info) {
		ToImgInfoList l=new ToImgInfoList();
		l.add(info);
		return l;
	}
}
