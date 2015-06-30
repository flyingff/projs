package net.flyingff.digitalspring.lt;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.imageio.ImageIO;

public class Test {
	public static void main(String[] args) {
		int sum = 0, cnt = 100, tmp;
		for(int i = 0; i < cnt; i++){
			tmp = test();
			if (tmp == -1) throw new RuntimeException();
			sum += tmp;
		}
		sum = sum / cnt;
		System.out.println("averagge usage: " + sum + "%");
	}
	private static int test(){
		//byte[] data = new byte[94037];
		//for(int i = 0; i < data.length;i++) {
		//	data[i] = (byte) (Math.random() * 256) ;
		//}
		byte[] data = null; 
		try {
			BufferedImage bi = new Robot().createScreenCapture(new Rectangle(0,0,1000,1000));
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ImageIO.write(bi, "jpeg", bos);
			data = bos.toByteArray();
		} catch(Exception e) {e.printStackTrace();}
		
		LTCoderFactory f = new LTCoderFactory();
		f.setDropLen(1024);
		Encoder e = f.buildEnocder();
		Decoder d = f.buildDecoder();
		e.setData(data);
		int i = 0;
		
		long tmbegin = System.currentTimeMillis(), curr = 0;
		long tm1, tm2, tm3;
		boolean succeed = false;
		while(System.currentTimeMillis() - tmbegin < 1000) {
			int PACKETLEN = 65536;
			ByteBuffer DATAPACKET = ByteBuffer.allocate(PACKETLEN);
			tm1 = System.currentTimeMillis();
			DATAPACKET.position(0);
			DATAPACKET.putLong(tmbegin).putInt(data.length);
			DATAPACKET.limit(e.write(DATAPACKET.array(), 12,PACKETLEN  - 12) + 12).position(0);
			//int len = e.write(sendbuf, 12,  - 12);
			//System.out.println(Arrays.toString(sendbuf));
			//if (Math.random() > 0.5) continue;
			tm2 = System.currentTimeMillis();
			DATAPACKET.position(0);
			if (curr != DATAPACKET.getLong()){
				DATAPACKET.position(0);
				curr = DATAPACKET.getLong();
				DATAPACKET.position(8);
				d.init(DATAPACKET.getInt());
			}
			if (d.update(DATAPACKET.array(), 12, DATAPACKET.limit() - 12)) {succeed = true; i++; break;}
			tm3 = System.currentTimeMillis();
			i++;
			System.out.println("Encoder: " +(tm2 - tm1)+" ms, Decoder: " + (tm3 - tm2) + " ms.");
		}
		//System.out.println("Thoroughput = " + (((long)sendbuf.length * i * 1000 / (System.currentTimeMillis() - tmbegin)) >> 20) + "MB/s");
		//System.out.println("packet used:" + (long)sendbuf.length * i * 100/ data.length + "%");
		byte[] recved = null;
		if (succeed)
			recved = d.finish();
		boolean equal = false;
		equal = Arrays.equals(recved, data);
		//System.out.println("Succeed: " + succeed + ", result:" + (succeed && ()));
		if (!succeed || !equal) {
			//System.out.print("Recved: "); 
			//atostr(recved);
			//System.out.print("Origin: ");
			//atostr(data);
			//BufferedImage bi = atoimg(new byte[][]{recved, data});
			//JOptionPane.showConfirmDialog(null, "", "", JOptionPane.CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, new ImageIcon(bi));
			return -1;
		}
		return (int) ((long)65536 * i * 100/ data.length);
	}
	public static void atostr(byte[] arr){
		for(byte x : arr)
			System.out.printf("%4d, ", x);
		System.out.println();
	}
	public static BufferedImage atoimg(byte[][] arr) {
		int r = 5;
		BufferedImage bi = new BufferedImage(arr[0].length * r, arr.length * r, BufferedImage.TYPE_BYTE_GRAY);
		Graphics g = bi.getGraphics();
		//bi.getData().getDataBuffer().
		for(int i = 0; i < arr.length; i++) {
			for(int j = 0; j <arr[0].length; j++) {
				int x = 0xff & (int)arr[i][j];
				g.setColor(new Color(x + (x << 8) + (x << 16)));
				g.fillRect(j * r, i * r, r, r);
			}
		}
		return bi;
	}
}
