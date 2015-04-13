/*	This program is released under European Union Public License.
 *  for details please refer to the file LICENSE
 *   
 *  Disclaimer: the program comes "as is" and with absolutely no warranty 
 *  
 *  © 2010,2011 TCM
 */

package nio.converter;

import static common.util.Util.BIT_MASK;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;

import javax.imageio.ImageIO;


public class ToImgNewIO implements IToImgIO2 {

	
	@Override
	public void toImg(SeekableByteChannel in,String inputFilename,long segmentLen, long pos,Path out) throws IOException {
		
		// header: 4 pixel segment size + 4 pixel offset pos + 1 pixel filename length + ceil(n / 3) pixel filename  
		// content: ceil(b / 3) pixel
		
		// Dimension d = ceil sqrt( pixel) 
		
		Point p=new Point(0,0);
		
		int d = (int)Math.ceil(
								Math.sqrt(
											9 + getPixel(inputFilename.length())+getPixel(segmentLen) 
										 )
							  );
		
		BufferedImage bi=new BufferedImage(d, d, BufferedImage.TYPE_INT_RGB);
		
		for (int i=0;i<4;i++) {
			write(bi, p, d, longToInt(segmentLen, i));
		}
		for (int i=0;i<4;i++) {
			write(bi, p, d, longToInt(pos, i));
		}
		write(bi, p, d, inputFilename.length());
		
		for (int i=0;i<inputFilename.length()-3;i+=3) {
			write(bi, p, d, toRGB(inputFilename.charAt(i), inputFilename.charAt(i+1), inputFilename.charAt(i+2)));
		}
		
		int fl=inputFilename.length()%3;
		
		if (fl==1) {
			write(bi, p, d, toRGB(inputFilename.charAt(inputFilename.length()-1),0,0));
		} else if (fl==2) {
			write(bi, p, d, toRGB(inputFilename.charAt(inputFilename.length()-2),inputFilename.charAt(inputFilename.length()-1),0));
		} else {
			write(bi, p, d, toRGB(inputFilename.charAt(inputFilename.length()-3),inputFilename.charAt(inputFilename.length()-2),inputFilename.charAt(inputFilename.length()-1)));
		}
		
		in.position(pos);
		byte[] buffer = new byte[16384];
		ByteBuffer bb=ByteBuffer.wrap(buffer);
		
		
		int cnt=0;
		int[] c=new int[3];
		long bytesRead=0;
		int r=-1;
//		updateGUI(rm().getLocalizedString("logReading") + ' '+inputFilename+" [Offset "+pos+"]");
		do {
			if (bytesRead==segmentLen) {
				if (c[0]!=0 || c[1]!=0 || c[2]!=0) {
					int rgb=toRGB(c[0], c[1], c[2]);
					write(bi,p,d, rgb);
				}
				
				break;
			} else {
				
				if (segmentLen- bytesRead<=bb.limit()) {
					
					bb.limit((int)(segmentLen- bytesRead));
				}
				r=in.read(bb);		
				bytesRead+=r;
				
				for (int i=0;i<r;i++) {
					
					c[cnt]=buffer[i];
					cnt++;
					
					if (cnt==3  ) {
						cnt=0;
						int rgb=toRGB(c[0], c[1], c[2]);
						c[0]=0;
						c[1]=0;
						c[2]=0;
						write(bi,p,d, rgb);
					}
				}
				bb.clear();
			}
		} while (true);
//		updateGUI(rm().getLocalizedString("logWriting")+" PNG "+out.getName());
		ImageIO.write(bi, "png", out.toFile());
		in.close();
//		updateGUI(rm().getLocalizedString("logFinishedWriting") +' '+out.getName());
	}

	private int getPixel(long i) {
		return (int)(i/3+i%3);
	}
	private int getPixel(int i) {
		return i/3+i%3;
	}
	private void write(BufferedImage bi,Point p,int d,int rgb) {
		bi.setRGB(p.x, p.y, rgb);
		p.x++;
		if (p.x==d) {
			p.x=0;
			p.y++;
		}
	}
	
	private int toRGB(byte red,byte green,byte blue) {
		int r=(red&0xff);
		int g=(green&0xff);
		int b=(blue&0xff);
		return r<<16|g<<8|b;
	}
	
	private int toRGB(char red,char green,char blue) {
		return toRGB((byte)red,(byte) green, (byte)blue);
	}
	
	private int toRGB(int red,int green,int blue) {
		return toRGB((byte)red,(byte) green, (byte)blue);
	}
	
	private int longToInt(long l,int arg) {
		if (arg==0) {
			return (int)(l & BIT_MASK[arg]);
		} else {
			return (int)((l & BIT_MASK[arg])>>>(arg<<4));
		}
	}

	
}
