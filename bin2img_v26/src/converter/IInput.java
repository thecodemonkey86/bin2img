package converter;

import java.io.IOException;

public interface IInput {
	public int read(byte[] buffer,int off,int len) throws IOException;

	public void seek(long pos) throws IOException;

	public int read(byte[] buffer)throws IOException;

	public long length()throws IOException;
}
