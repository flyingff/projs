package net.flyingff.img.dither;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class DitherProcessor {
	private static final ErrorSpreadDitherFunction FLOYD_STEINBERG_8 = new ErrorSpreadDitherFunction() {
		private Map<Point, Integer> spreadMap = new HashMap<>();
		{
			spreadMap.put(new Point(1, 0), 3);
			spreadMap.put(new Point(0, 1), 3);
			spreadMap.put(new Point(1, 1), 1);
		}

		@Override
		public int total() {
			return 8;
		}

		@Override
		public Map<Point, Integer> spreadMap() {
			return spreadMap;
		}

		@Override
		public String name() {
			return "Floyd-Steinberg 8x";
		}
	};
	private static final ErrorSpreadDitherFunction FLOYD_STEINBERG_16 = new ErrorSpreadDitherFunction() {
		private Map<Point, Integer> spreadMap = new HashMap<>();
		{
			spreadMap.put(new Point(1, 0), 7);
			spreadMap.put(new Point(-1, 1), 3);
			spreadMap.put(new Point(0, 1), 5);
			spreadMap.put(new Point(1, 1), 1);
		}

		@Override
		public int total() {
			return 16;
		}

		@Override
		public Map<Point, Integer> spreadMap() {
			return spreadMap;
		}

		@Override
		public String name() {
			return "Floyd-Steinberg 16x";
		}
	};
	private static final ErrorSpreadDitherFunction JARVIS_JUDICE_NIKE = new ErrorSpreadDitherFunction() {
		private Map<Point, Integer> spreadMap = new HashMap<>();
		{
			spreadMap.put(new Point(1, 0), 7);
			spreadMap.put(new Point(2, 0), 5);
			spreadMap.put(new Point(-2, 1), 3);
			spreadMap.put(new Point(-1, 1), 5);
			spreadMap.put(new Point(0, 1), 7);
			spreadMap.put(new Point(1, 1), 5);
			spreadMap.put(new Point(2, 1), 3);
			spreadMap.put(new Point(-2, 2), 1);
			spreadMap.put(new Point(-1, 2), 3);
			spreadMap.put(new Point(0, 2), 5);
			spreadMap.put(new Point(1, 2), 3);
			spreadMap.put(new Point(2, 2), 1);
		}

		@Override
		public int total() {
			return 48;
		}

		@Override
		public Map<Point, Integer> spreadMap() {
			return spreadMap;
		}

		@Override
		public String name() {
			return "Jarvis, Judice, and Ninke";
		}
	};
	private static final ErrorSpreadDitherFunction STUCKI = new ErrorSpreadDitherFunction() {
		private Map<Point, Integer> spreadMap = new HashMap<>();
		{
			spreadMap.put(new Point(1, 0), 8);
			spreadMap.put(new Point(2, 0), 4);
			spreadMap.put(new Point(-2, 1), 2);
			spreadMap.put(new Point(-1, 1), 4);
			spreadMap.put(new Point(0, 1), 8);
			spreadMap.put(new Point(1, 1), 4);
			spreadMap.put(new Point(2, 1), 2);
			spreadMap.put(new Point(-2, 2), 1);
			spreadMap.put(new Point(-1, 2), 2);
			spreadMap.put(new Point(0, 2), 4);
			spreadMap.put(new Point(1, 2), 2);
			spreadMap.put(new Point(2, 2), 1);
		}

		@Override
		public int total() {
			return 42;
		}

		@Override
		public Map<Point, Integer> spreadMap() {
			return spreadMap;
		}

		@Override
		public String name() {
			return "Stucki";
		}
	};
	private static final ErrorSpreadDitherFunction ATKINSON = new ErrorSpreadDitherFunction() {
		private Map<Point, Integer> spreadMap = new HashMap<>();
		{
			spreadMap.put(new Point(1, 0), 1);
			spreadMap.put(new Point(2, 0), 1);
			spreadMap.put(new Point(-1, 1), 1);
			spreadMap.put(new Point(0, 1), 1);
			spreadMap.put(new Point(1, 1), 1);
			spreadMap.put(new Point(0, 2), 1);
		}

		@Override
		public int total() {
			return 8;
		}

		@Override
		public Map<Point, Integer> spreadMap() {
			return spreadMap;
		}

		@Override
		public String name() {
			return "Atkinson";
		}
	};
	private static final ErrorSpreadDitherFunction BURKES = new ErrorSpreadDitherFunction() {
		private Map<Point, Integer> spreadMap = new HashMap<>();
		{
			spreadMap.put(new Point(1, 0), 8);
			spreadMap.put(new Point(2, 0), 4);
			spreadMap.put(new Point(-2, 1), 2);
			spreadMap.put(new Point(-1, 1), 4);
			spreadMap.put(new Point(0, 1), 8);
			spreadMap.put(new Point(1, 1), 4);
			spreadMap.put(new Point(2, 1), 2);
		}

		@Override
		public int total() {
			return 32;
		}

		@Override
		public Map<Point, Integer> spreadMap() {
			return spreadMap;
		}

		@Override
		public String name() {
			return "Burkes";
		}
	};
	private static final ErrorSpreadDitherFunction SIERRA_3 = new ErrorSpreadDitherFunction() {
		private Map<Point, Integer> spreadMap = new HashMap<>();
		{
			spreadMap.put(new Point(1, 0), 5);
			spreadMap.put(new Point(2, 0), 3);
			spreadMap.put(new Point(-2, 1), 2);
			spreadMap.put(new Point(-1, 1), 4);
			spreadMap.put(new Point(0, 1), 5);
			spreadMap.put(new Point(1, 1), 4);
			spreadMap.put(new Point(2, 1), 2);
			spreadMap.put(new Point(-1, 2), 2);
			spreadMap.put(new Point(0, 2), 3);
			spreadMap.put(new Point(1, 2), 2);
		}

		@Override
		public int total() {
			return 32;
		}

		@Override
		public Map<Point, Integer> spreadMap() {
			return spreadMap;
		}

		@Override
		public String name() {
			return "Sierra-3";
		}
	};
	private static final ErrorSpreadDitherFunction SIERRA_TWO_ROW = new ErrorSpreadDitherFunction() {
		private Map<Point, Integer> spreadMap = new HashMap<>();
		{
			spreadMap.put(new Point(1, 0), 4);
			spreadMap.put(new Point(2, 0), 3);
			spreadMap.put(new Point(-2, 1), 1);
			spreadMap.put(new Point(-1, 1), 2);
			spreadMap.put(new Point(0, 1), 3);
			spreadMap.put(new Point(1, 1), 2);
			spreadMap.put(new Point(2, 1), 1);
		}

		@Override
		public int total() {
			return 16;
		}

		@Override
		public Map<Point, Integer> spreadMap() {
			return spreadMap;
		}

		@Override
		public String name() {
			return "Sierra Two-row";
		}
	};
	private static final ErrorSpreadDitherFunction SIERRA_LITE = new ErrorSpreadDitherFunction() {
		private Map<Point, Integer> spreadMap = new HashMap<>();
		{
			spreadMap.put(new Point(1, 0), 2);
			spreadMap.put(new Point(-1, 1), 1);
			spreadMap.put(new Point(0, 1), 1);
		}

		@Override
		public int total() {
			return 4;
		}

		@Override
		public Map<Point, Integer> spreadMap() {
			return spreadMap;
		}

		@Override
		public String name() {
			return "Sierra Lite";
		}
	};

	private static final int[][] M(int n) {
		if(n == 1) {
			return new int[][] {
				{0, 2},{3, 1}
			};
		} else if (n < 1) {
			throw new IllegalArgumentException("M1 is minimum: " + n);
		}
		final int size = 1 << n, halfSize = 1 << (n - 1);
		int[][] mn = new int[size][size];
		int[][] mnm1 = M(n - 1);
		int[][] mu = new int[halfSize][halfSize];
		for(int i = 0; i < halfSize; i++) {
			for(int j = 0; j < halfSize; j++) {
				mu[i][j] = 1;
			}
		}
		
		for(int i = 0; i < halfSize; i++) {
			for(int j = 0; j < halfSize; j++) {
				mn[i][j] = mnm1[i][j] * 4;
				mn[i][j + halfSize] = mnm1[i][j] * 4 + mu[i][j] * 2;
				mn[i + halfSize][j] = mnm1[i][j] * 4 + mu[i][j] * 3;				
				mn[i + halfSize][j + halfSize] = mnm1[i][j] * 4 + mu[i][j];
			}
		}
		return mn;
	}
	private static BufferedImage bayerDither(BufferedImage im) {
		int w = im.getWidth(), h = im.getHeight();
		BufferedImage ret = new BufferedImage(w, h,
				BufferedImage.TYPE_BYTE_GRAY);
		//ret.getGraphics().drawImage(im, 0, 0, null);
		int[][] m3 = M(3);
		for(int i = 0; i < w; i++) {
			for(int j = 0; j < h; j++) {
				int gray = (im.getRGB(i, j) & 0xFF) >> 2;
				ret.setRGB(i, j, m3[i & 7][j & 7] < gray ? 0xFFFFFFFF : 0x0);
			}
		}
		return ret;
	}
	private static BufferedImage generalErrorSpreadDither(BufferedImage img,
			ErrorSpreadDitherFunction fun) {
		int w = img.getWidth(), h = img.getHeight();
			BufferedImage ret = new BufferedImage(w, h,
					BufferedImage.TYPE_BYTE_GRAY);
		float[][] errorMap = new float[w][h];
		final int total = fun.total();
		final Map<Point, Integer> spreadMap = Collections
				.unmodifiableMap(fun.spreadMap());
		
 		for(int i = 0; i < w; i++) {
			for(int j = 0; j < h; j++) {
				float gray = (img.getRGB(i, j) & 0xFF) + errorMap[i][j];
				float error;
				if(gray > 127) {
					ret.setRGB(i, j, 0xFFFFFFFF);
					error = gray - 0xFF;
				} else {
					ret.setRGB(i, j, 0x0);
					error = gray;
				}
				for(Entry<Point, Integer> entry : spreadMap.entrySet()) {
					Point pt = entry.getKey();
					int val = entry.getValue();
					int x = i + pt.x, y = j + pt.y;
					if(x >= w || y >= h || x < 0 || y < 0) continue;
					errorMap[x][y] += error * val / total;
				}
			}
		}
		return ret;
	}
	
	private interface ErrorSpreadDitherFunction {
		Map<Point, Integer> spreadMap();
		int total();
		String name();
 	}
	public interface DitherMethod {
		String name();
		BufferedImage process(BufferedImage img);
		
	}
	private static class GeneralDitherMethod implements DitherMethod{
		private final ErrorSpreadDitherFunction func;
		private GeneralDitherMethod(ErrorSpreadDitherFunction func) {
			this.func = func;
		}
		@Override
		public String name() { return func.name(); }
		@Override
		public BufferedImage process(BufferedImage img) {
			return generalErrorSpreadDither(img, func);
		}
		@Override
		public String toString() { return name(); }
	}
	
	private static final List<DitherMethod> DITHER_METHODS = Arrays.asList(new DitherMethod() {
		@Override public String name() { return "Bayer 8x"; }
		@Override public BufferedImage process(BufferedImage img) {
			return bayerDither(img);
		}
		@Override
		public String toString() {
			return name();
		}
	}, new GeneralDitherMethod(FLOYD_STEINBERG_8),  new GeneralDitherMethod(FLOYD_STEINBERG_16),
			 new GeneralDitherMethod(JARVIS_JUDICE_NIKE), new GeneralDitherMethod(STUCKI),
			 new GeneralDitherMethod(ATKINSON),  new GeneralDitherMethod(BURKES),
			 new GeneralDitherMethod(SIERRA_3),  new GeneralDitherMethod(SIERRA_TWO_ROW),
			 new GeneralDitherMethod(SIERRA_LITE));
	
	public static Iterator<DitherMethod> allDitherMethods() {
		return DITHER_METHODS.iterator();
	}
	public static BufferedImage decoloring(BufferedImage im) {
		BufferedImage ret = new BufferedImage(im.getWidth(),
				im.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		
		ret.getGraphics().drawImage(im, 0, 0, null);
		return ret;
	}

	public static void main(String[] args) throws Exception {
		BufferedImage im = ImageIO.read(new File("D:\\lena.png"));
		BufferedImage imgGray = decoloring(im);
		
		JFrame fr = new JFrame("Picture");
		ImageIcon ic = new ImageIcon();
		JLabel lbl = new JLabel(ic);
		
		JComboBox<DitherMethod> combo = new JComboBox<>(new Vector<>(DITHER_METHODS));
		combo.setEditable(false);
		combo.addItemListener(ev->{
			DitherMethod mt = (DitherMethod) combo.getSelectedItem();
			ic.setImage(mt.process(imgGray));
			fr.pack();
			lbl.repaint();
		});
		
		fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		fr.add(lbl, BorderLayout.CENTER);
		fr.add(combo, BorderLayout.SOUTH);
		fr.setResizable(false);
		fr.pack();
		fr.setLocationRelativeTo(null);
		fr.setVisible(true);
	}
}
