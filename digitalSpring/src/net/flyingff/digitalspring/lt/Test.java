package net.flyingff.digitalspring.lt;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

public class Test {
	public static void main(String[] args) {
		byte[] data = new byte[512000];
		for(int i = 0; i < data.length;i++) {
			data[i] = (byte) (Math.random() * 256) ;
		}
		LTCoderFactory f = new LTCoderFactory();
		f.setDropLen(128);
		Encoder e = f.buildEnocder();
		Decoder d = f.buildDecoder();
		byte[] sendbuf = null;
		e.setData(data);
		int i = 0;
		d.init(data.length);
		long tmbegin = System.currentTimeMillis();
		long tm1, tm2, tm3;
		boolean succeed = false;
		while(System.currentTimeMillis() - tmbegin < 1000) {
			sendbuf = new byte[65536];
			tm1 = System.currentTimeMillis();
			int len = e.write(sendbuf, 0, sendbuf.length);
			if (Math.random() > 0.95) continue;
			tm2 = System.currentTimeMillis();
			if (d.update(sendbuf, 0, len)) {succeed = true; i++; break;}
			tm3 = System.currentTimeMillis();
			i++;
			System.out.println("Encoder: " +(tm2 - tm1)+" ms, Decoder: " + (tm3 - tm2) + " ms.");
		}
		System.out.println("Thoroughput = " + (((long)sendbuf.length * i * 1000 / (System.currentTimeMillis() - tmbegin)) >> 20) + "MB/s");
		byte[] recved = null;
		if (succeed)
			recved = d.finish();
		boolean equal = false;
		System.out.println("Succeed: " + succeed + ", result:" + (succeed && (equal = Arrays.equals(recved, data))));
		if (succeed && ! equal) {
			System.out.print("Recved: "); 
			atostr(recved);
			System.out.print("Origin: ");
			atostr(data);
			BufferedImage bi = atoimg(new byte[][]{recved, data});
			JOptionPane.showConfirmDialog(null, "", "", JOptionPane.CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, new ImageIcon(bi));
		}
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
