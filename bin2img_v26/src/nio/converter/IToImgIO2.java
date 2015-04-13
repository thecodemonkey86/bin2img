package nio.converter;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;

public interface IToImgIO2 {
	void toImg(SeekableByteChannel in,String inputFilename,long segmentLength, long pos,Path out) throws IOException;
}
