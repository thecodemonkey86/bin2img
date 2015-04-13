package nio.converter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

public class ByteInput implements SeekableByteChannel {
	private boolean open;
	private byte[] bytes;
	private int pos;
	
	public ByteInput(byte[] bytes) {
		open=true;
		pos=0;
		this.bytes=bytes;
	}
	
	@Override
	public boolean isOpen() {
		return open;
	}

	@Override
	public void close() throws IOException {
		open=false;
	}

	@Override
	public int read(ByteBuffer dst) throws IOException {
		int o=pos;
		int len=dst.limit();
		int l= Math.min(len,bytes.length-o);
		byte[] buffer=new byte[l];
		System.arraycopy(bytes, o, buffer, 0,l);
		dst.put(buffer);
		pos+=l;
		return l;
	}

	@Override
	public int write(ByteBuffer src) throws IOException {
		return 0;
	}

	@Override
	public long position() throws IOException {
		return pos;
	}

	@Override
	public SeekableByteChannel position(long newPosition) throws IOException {
		this.pos=(int) newPosition;
		return this;
	}

	@Override
	public long size() throws IOException {
		return bytes.length;
	}

	@Override
	public SeekableByteChannel truncate(long size) throws IOException {
		return null;
	}

}
