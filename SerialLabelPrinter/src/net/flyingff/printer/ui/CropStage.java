package net.flyingff.printer.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JSlider;

public class CropStage implements IProcessStage{
	private static final int PIC_WIDTH = 384, PIC_HEIGHT = 240;
	private int offsetX = 0, offsetY = 0, scale = 100;
	
	private BufferedImage dest = new BufferedImage(PIC_WIDTH, PIC_HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
	
	public CropStage(JSlider sliderOffsetX, JSlider sliderOffsetY, JSlider sliderScale) {
		sliderOffsetX.setMinimum(-100);
		sliderOffsetX.setValue(0);
		sliderOffsetX.setMaximum(100);
		sliderOffsetY.setMinimum(-100);
		sliderOffsetY.setValue(0);
		sliderOffsetY.setMaximum(100);
		sliderScale.setMinimum(-50);
		sliderScale.setMaximum(100);
		sliderScale.setValue(0);
		
		sliderOffsetX.addChangeListener(e->{
			offsetX = sliderOffsetX.getValue();
			if(updateListener != null) {
				updateListener.run();
			}
		});
		sliderOffsetY.addChangeListener(e->{
			offsetY = sliderOffsetY.getValue();
			if(updateListener != null) {
				updateListener.run();
			}
		});
		sliderScale.addChangeListener(e->{
			scale = sliderScale.getValue() + 100;
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
		Graphics g = dest.getGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, PIC_WIDTH, PIC_HEIGHT);
		float rateX = img.getWidth() / (float)PIC_WIDTH, rateY = img.getHeight() / (float)PIC_HEIGHT;
		float rate = Math.min(rateX, rateY) * (scale) / 100;
		
		
		
		int offsetX = this.offsetX * img.getWidth() / 100, offsetY = this.offsetY * img.getHeight() / 100,
				w = (int) (PIC_WIDTH * rate), h =  (int) (PIC_HEIGHT * rate);
		g.drawImage(img, 0, 0, PIC_WIDTH, PIC_HEIGHT, 
				offsetX, offsetY, 
				offsetX + w, offsetY + h, null);
		return dest;
	}

}
