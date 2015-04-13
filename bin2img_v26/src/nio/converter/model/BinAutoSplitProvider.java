package nio.converter.model;

import static common.util.Util.SPLIT_FILES_EXT;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TreeSet;

import common.util.Util;

public class BinAutoSplitProvider {
	public static Path[] getAllSplitInputFiles(Path[] input) {
		final TreeSet<Path> files=new TreeSet<Path>();
		for (Path in:input) {
			String s=in.getFileName().toString();
			files.add(in);
			
			if (s.length()>SPLIT_FILES_EXT.length() && s.substring(s.length()- SPLIT_FILES_EXT.length()).matches("\\.[\\d]{5}\\.png")) {
				int i=1;
				String prefix=s.substring(0, s.length()-SPLIT_FILES_EXT.length());
				Path f=getNumbered(in,prefix, i++);;
				while (Files.exists(f)) {
					files.add(f);
					f=getNumbered(in,prefix, i++);
				};
			} 
		}
		Path[] result=new Path[files.size()];
		files.toArray(result);
		return result;
	}
	
	private static Path getNumbered(Path in,String prefix, int counter) {
		return in.getParent().resolve(prefix+"."+Util.formatFileNumbering(counter)+".png");
	}
}
