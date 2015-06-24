package net.flyingff.digitalspring.multicast.test;

import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

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
		dsr.setReceiveListener(new ReceiveListener() {
			@Override
			public void onData(byte[] data) {
				System.out.println("Data arrives(" + data.length + "):");
				try {
					JOptionPane.showConfirmDialog(null, "", "", JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE, new ImageIcon(ImageIO.read(new ByteArrayInputStream(data))));
				} catch (HeadlessException | IOException e) {
					e.printStackTrace();
				}
				//System.out.println(new String(data));
			}
		});
		DigitalSpringSender dss = new DigitalSpringSender(port);
		System.out.println(dss.detectClient());
		for(long lx : dss.getLastACKClient()){
			System.out.println(Long.toHexString(lx));
		}
		BufferedImage bi = new Robot().createScreenCapture(new Rectangle(0,0,1000,1000));
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ImageIO.write(bi, "png", bos);
		byte[] data = bos.toByteArray();
		System.out.println("image size = " + data.length);
		System.out.println(dss.send(data, 240));
		
		//System.exit(0);
	}
}
