package net.flyingff.printer.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JSlider;

public class GrayifyStage implements IProcessStage{
	private BufferedImage funcImage = new BufferedImage(256, 256, BufferedImage.TYPE_BYTE_GRAY);
	private JLabel lblImage;
	private int light = 0;
	
	public GrayifyStage(JSlider sliderGray, JLabel displayer) {
		Graphics g = funcImage.getGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, 255, 255);
		
		(lblImage = displayer).setIcon(new ImageIcon(funcImage));
		
		sliderGray.setMaximum(128);
		sliderGray.setMinimum(-128);
		sliderGray.setValue(0);
		sliderGray.addChangeListener(x->{
			light = sliderGray.getValue() * 2;
			if(updateListener != null) {
				updateListener.run();
			}
		});
	}
	
	private Runnable updateListener;
	@Override
	public void setUpdateRequiredListener(Runnable listener) {
		this.updateListener = listener;
	}

	@Override
	public BufferedImage process(BufferedImage img) {
		int w = img.getWidth(), h = img.getHeight();
		BufferedImage dest = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
		int[] count = new int[256];
		for(int i = 0; i < w; i++) {
			for(int j = 0; j < h; j++) {
				int val = img.getRGB(i, j);
				int gray = ((val & 0xFF) + ((val >> 8) & 0xFF) + ((val >> 16) & 0xFF)) / 3 + light;
				if(gray > 255) gray = 255;
				if(gray < 0) gray = 0;
				
				dest.setRGB(i, j, (gray << 16) | (gray << 8) | gray);
				count[gray] ++;
			}
		}
		
		Graphics g = funcImage.getGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, 256, 256);
		g.setColor(Color.black);
		for(int i = 0; i < 256; i++) {
			g.drawLine(i, 255, i, (255 - (count[i] >> 8)));
		}
		lblImage.repaint();
		
		return dest;
	}

}
