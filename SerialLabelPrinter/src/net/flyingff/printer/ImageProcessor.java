package net.flyingff.printer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.function.BiConsumer;

import javax.imageio.ImageIO;

@Deprecated
public class ImageProcessor {
	private static final int PIC_WIDTH = 384, PIC_HEIGHT = 240;
	private int threashold = 128;
	private BiConsumer<BufferedImage, BufferedImage> output;
	private BufferedImage src = null, dest = new BufferedImage(384, 240, BufferedImage.TYPE_3BYTE_BGR);
	
	public ImageProcessor(BiConsumer<BufferedImage, BufferedImage> output) {
		this.output = Objects.requireNonNull(output);
	}
	public void loadImage(File f) {
		try {
			src = ImageIO.read(f);
			if(src != null) {
				if(src.getWidth() != 384 || src.getHeight() != 240) try {
					throw new RuntimeException(String.format("图片大小应当为%dx%d, 实际大小为%dx%d",
							PIC_WIDTH, PIC_HEIGHT, src.getWidth(), src.getHeight()));
				} finally {
					src = null;
				}
				process();
			} else throw new IOException("EMPTY");
		} catch (IOException e) {
			e.printStackTrace();
			src = null;
			throw new RuntimeException("打开图片出错！");
		}
	}
	public void setThreashold(int threashold) {
		if(threashold <= 0 || threashold >= 255) {
			throw new IllegalArgumentException("Threasold out of range: " + threashold);
		}
		this.threashold = threashold;
		if(src != null) {
			process();
		}
	}
	public int getThreashold() {
		return threashold;
	}
	
	private void process() {
		for(int i = 0; i < PIC_WIDTH; i++) {
			for(int j = 0; j < PIC_HEIGHT; j++) {
				int rgb = src.getRGB(i, j);
				int avg = ((rgb & 0xFF) + ((rgb >> 8) & 0xFF) + ((rgb >> 16) & 0xFF)) / 3;
				dest.setRGB(i, j, avg <= threashold ? 0x0 : 0xFFFFFF); 
			}
		}
		output.accept(src, dest);
	}
	
	public BufferedImage getProcessedImage() {
		return src == null ? null : dest;
	}
}
