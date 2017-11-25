package net.flyingff.printer.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JSlider;

import net.flyingff.printer.BSplineGenerator;

public class GrayifyStage implements IProcessStage{
	private BufferedImage funcImage = new BufferedImage(256, 256, BufferedImage.TYPE_BYTE_GRAY);
	private JLabel lblImage;
	private int light = 0;
	private float[][] curvePoints = new float[16][2];
	private BSplineGenerator generator = new BSplineGenerator();
	private int[] mapper = new int[256];
	
	public GrayifyStage(JSlider sliderGray, JLabel displayer) {
		Graphics g = funcImage.getGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, 255, 255);
		
		(lblImage = displayer).setIcon(new ImageIcon(funcImage));
		displayer.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				int x = e.getX(), y = 255 - e.getY();
				int area = x / 16;
				
				if(area <= 0) area = 0; 
				if(area >= 16) area = 15;
				if(y <= 0) y = 0;
				if(y >= 256) y = 255;
				
				curvePoints[area][1] = y;
				updateListener.run();
			}
		});
		
		sliderGray.setMaximum(128);
		sliderGray.setMinimum(-128);
		sliderGray.setValue(0);
		sliderGray.addChangeListener(x->{
			light = sliderGray.getValue() * 2;
			if(updateListener != null) {
				updateListener.run();
			}
		});
		
		for(int i = 0; i < 16; i++) {
			curvePoints[i][0] = i * 255 / 15;
			curvePoints[i][1] = i * 255 / 15;
		}
	}
	
	private Runnable updateListener;
	@Override
	public void setUpdateRequiredListener(Runnable listener) {
		this.updateListener = listener;
	}

	@Override
	public BufferedImage process(BufferedImage img) {
		int w = img.getWidth(), h = img.getHeight();
		
		Graphics g = funcImage.getGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, 256, 256);
		g.setColor(Color.black);
		
		generator.clearPoints();
		generator.addAllPoints(curvePoints);
		float[] last = null;
		for(float[] val : generator.build().sample(256)) {
			if(last != null) {
				int lastX = Math.round(last[0]), currX = Math.round(val[0]), len = currX - lastX + 1;
				for(int x = lastX; x <= currX; x++) {
					float k = (x - lastX) / len;
					mapper[x] = Math.round(last[1] * (1 - k) + val[1] * k);
				}
			}
			last = val;
		}
		{
			int x = 0, lastY = 0;
			for(int v : mapper) {
				if(x > 1) {
					g.drawLine(x - 1, 255 - lastY, x, 255 - v);
				}
				lastY = v;
				x++;
			}
		}
		for(float[] pt : curvePoints) {
			int x = Math.round(pt[0]), y = Math.round(pt[1]);
			g.drawRect(x - 3, (255 - y) - 3, 6, 6);
		}
		
		BufferedImage dest = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
		int[] count = new int[256];
		for(int i = 0; i < w; i++) {
			for(int j = 0; j < h; j++) {
				int val = img.getRGB(i, j);
				int gray = ((val & 0xFF) + ((val >> 8) & 0xFF) + ((val >> 16) & 0xFF)) / 3 + light;
				if(gray > 255) gray = 255;
				if(gray < 0) gray = 0;
				count[gray] ++;
				gray = mapper[gray];
				dest.setRGB(i, j, (gray << 16) | (gray << 8) | gray);
			}
		}
		
		for(int i = 0; i < 256; i++) {
			if(count[i] > 0) {
				g.drawLine(i, 255, i, (255 - (count[i] >> 8)));
			}
		}
		lblImage.repaint();
		return dest;
	}

}
