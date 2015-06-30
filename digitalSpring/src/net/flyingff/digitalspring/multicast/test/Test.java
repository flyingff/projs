package net.flyingff.digitalspring.multicast.test;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import net.flyingff.digitalspring.multicast.DigitalSpringReceiver;
import net.flyingff.digitalspring.multicast.DigitalSpringSender;
import net.flyingff.digitalspring.multicast.ReceiveListener;

public class Test {
	public static void main(String[] args) throws Exception {
		int port = 10068;
		DigitalSpringReceiver dsr = new DigitalSpringReceiver(port);
		byte[][] imgdata = new byte[1][];
		boolean[] akk = new boolean[1];
		dsr.setReceiveListener(new ReceiveListener() {
			@Override
			public void onData(byte[] data) {
				System.out.println("Data arrives(" + data.length + "):");
				if(Arrays.equals(imgdata[0], data)){
					try {
						//JOptionPane.showConfirmDialog(null, "", "", JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE, new ImageIcon(ImageIO.read(new ByteArrayInputStream(data))));
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					System.out.println("not equal...");
					System.out.println(Arrays.toString(imgdata[0]));
					System.out.println(Arrays.toString(data));
					//System.exit(0);
				}
				akk[0] = true;
			}
		});
		DigitalSpringSender dss = new DigitalSpringSender(port);
		System.out.println(dss.detectClient());
		for(long lx : dss.getLastACKClient()){
			System.out.println(Long.toHexString(lx));
		}
		for(int i = 0; i < 10; i++){
			BufferedImage bi = new Robot().createScreenCapture(new Rectangle(0,0,1000,1000));
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ImageIO.write(bi, "png", bos);
			imgdata[0] = bos.toByteArray();
			System.out.println("image size = " + imgdata[0].length);
			akk[0] = false;
			System.out.println(dss.send(imgdata[0], 12400));
			while(!akk[0]) Thread.sleep(10);
		}
		Thread.sleep(2000);
		System.exit(0);
	}
}
