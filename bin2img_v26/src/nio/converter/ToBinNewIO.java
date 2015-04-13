package nio.converter;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import javax.imageio.ImageIO;

import core.util.IntByRef;

public class ToBinNewIO {
	public Path toBin(Path in,Path outputDir) throws IOException {
			System.out.println(in.getFileName().toString());
//			updateGUI(rm().getLocalizedString("logReading") + ' ' +in.getName());
			BufferedImage bi=ImageIO.read(in.toFile());
			if (bi==null) {
//				updateGUI(rm().getLocalizedString("logErrorInvalid" ));
				return null;
			}
			
			Point p=new Point(0,0);
			
			int i1=getInt(bi, p);
			nextPixel(p, bi);
			int i2=getInt(bi, p);
			nextPixel(p, bi);
			int i3=getInt(bi, p);
			nextPixel(p, bi);
			int i4=getInt(bi, p);
			nextPixel(p, bi);
			long len=intToLong(i1, i2, i3, i4);
			
			i1=getInt(bi, p);
			nextPixel(p, bi);
			i2=getInt(bi, p);
			nextPixel(p, bi);
			i3=getInt(bi, p);
			nextPixel(p, bi);
			i4=getInt(bi, p);
			nextPixel(p, bi);
			long pos=intToLong(i1, i2, i3, i4);
			
			StringBuilder sb=new StringBuilder();
			
			int fileNameLen=getInt(bi, p);
			nextPixel(p, bi);
			
			//boolean b=false;
			
			int rgb,i=0;
			while (true) {
				
				rgb=getRGB(bi, p);
				
				if (i%3==0) {
					sb.append((char)getRed(rgb));
					i++;
					if (i==fileNameLen) {
						nextPixel(p, bi);
						break;
					}
					//b=true;
				} else if (i%3==1) {
					sb.append((char)getGreen(rgb));
					i++;
					if (i==fileNameLen) {
						nextPixel(p, bi);
						break;
					}
					//b=true;
				} else {
					sb.append((char)getBlue(rgb));
					nextPixel(p, bi);
					i++;
					if (i==fileNameLen) {
						break;
					}
				}
				
				
			}
			Path out=outputDir.resolve(sb.toString());
			
			if (out.getNameCount() != outputDir.getNameCount()+1){
				Files.createDirectories(out.getParent());
			}
			
			SeekableByteChannel fos= Files.newByteChannel(out,StandardOpenOption.CREATE,StandardOpenOption.WRITE);
			
			if (pos>0) {
				fos.position(pos);
			}
			byte[] buffer=new byte[4096];
			ByteBuffer bb=ByteBuffer.wrap(buffer);
			IntByRef k=new IntByRef();
			k.val=0;
			i=0;
			//updateGUI(rm().getLocalizedString("logWriting") +' '+out.getName());
			
			while (true) {
				
				if (i>=len) {
					break;
				}
				rgb=getRGB(bi, p);
				writeBuffer(fos, bb, k);
				buffer[k.val]=(byte)getRed(rgb) ;
				k.val++;
				i++;
				
				if (i>=len) {
					break;
				}
				writeBuffer(fos, bb, k);
				buffer[k.val]=(byte)getGreen(rgb) ;
				k.val++;
				i++;
				
				if (i>=len) {
					break;
				}
				writeBuffer(fos, bb, k);
				buffer[k.val]=(byte)getBlue(rgb) ;
				k.val++;
				i++;
				nextPixel(p, bi);
			}
			
			if (len>=3) {
				bb.limit(k.val);
				fos.write(bb);
			} else {
				bb.limit((int) len);
				fos.write(bb);
			}
						
			fos.close();
//			updateGUI(rm().getLocalizedString("logFinishedWriting") +' '+out.getName());
			
			//updateGUI(rm().getLocalizedString("logError") +' '+e.getMessage());
			return out;
	}
	
	private void writeBuffer(SeekableByteChannel seek,ByteBuffer buffer, IntByRef counter) throws IOException {
		if (counter.val==buffer.limit()) {
			seek.write(buffer);
			counter.val=0;
			buffer.clear();
		}
	}
	
//	private void updateGUI(Object arg) {
//		setChanged();
//		notifyObservers(arg);
//	}
	
	private void nextPixel(Point p,BufferedImage bi) {
		p.x++;
		if (p.x==bi.getWidth()) {
			p.x=0;
			p.y++;
		}
	}
	
	private int getInt(BufferedImage bi,Point p) {
		return bi.getRGB(p.x, p.y)+(2<<23);
	}
	
	private int getRGB(BufferedImage bi,Point p) {
		return bi.getRGB(p.x,p.y);
	}
	
	private int getRed(int rgb) {
		return (rgb >> 16) & 0xFF;
	}

	private int getGreen(int rgb) {
		return (rgb >> 8) & 0xFF;
	}

	private int getBlue(int rgb) {
		return (rgb >> 0) & 0xFF;
	}
	
	private long intToLong(int i1,int i2,int i3,int i4) {
		return (long)i4<<48|(long)i3<<32|(long)i2<<16|(long)i1;
	}
}
