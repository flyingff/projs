package net.flyingff.douyu.barrage;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

import javax.swing.JWindow;

public class FullScreenBarrageWindow extends JWindow {
	private static final long serialVersionUID = 3738633837345864786L;
	private static final int BARRAGE_SIZE = 48;
	private static final Font font = new Font("Microsoft YaHei UI", Font.BOLD, BARRAGE_SIZE);
	private static final long TIME_BARRAGE_DISPLAY = 15_000;
	
	private Deque<BarrageMessage> barrageQueue = new ArrayDeque<>();
	private Dimension dimScreen = Toolkit.getDefaultToolkit()
			.getScreenSize();
	private boolean usageMask[] = new boolean[(int) Math.ceil(dimScreen.getHeight() / BARRAGE_SIZE)];
	public FullScreenBarrageWindow() {
		
		try {
			Class.forName("com.sun.awt.AWTUtilities").getMethod("setWindowOpaque", Window.class, boolean.class)
				.invoke(null, this, false);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		setSize(Toolkit.getDefaultToolkit().getScreenSize());
		setLocation(0, 0);
		setAlwaysOnTop(true);
		setVisible(true);
		
		new Thread(()->{
			while(isShowing()) {
				refreshBarrage();
				repaint();
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) { }
			}
		}).start();
	}
	private synchronized void refreshBarrage() {
		Iterator<BarrageMessage> it = barrageQueue.iterator();
		long now = System.currentTimeMillis();
		int scrw = dimScreen.width;
		while(it.hasNext()) {
			BarrageMessage bm = it.next();
			long dt = now - bm.startTm;
			bm.x = (int) (scrw - dt * scrw / TIME_BARRAGE_DISPLAY);
			if(bm.x <= -bm.w) {
				it.remove();
			}
			if(bm.emptyTime > 0 && now > bm.emptyTime) {
				usageMask[bm.row] = false;
				bm.emptyTime = 0;
			}
		}
	}
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		synchronized (this) {
			for(BarrageMessage bm : barrageQueue) {
				g.drawImage(bm.img, bm.x, bm.y, null);
			}
		}
	}
	
	public synchronized void pushBarrage(String text, String nm) {
		Graphics2D g2d = (Graphics2D) getGraphics();
		g2d.setFont(font);
		
		BarrageMessage bm = new BarrageMessage();
		int i;
		for(i = 0; i < usageMask.length; i ++) {
			if(!usageMask[i]) break;
		}
		if(i >= usageMask.length) return;
		usageMask[i] = true;
		bm.row = i;
		
		Color c = new Color(Color.HSBtoRGB((nm.hashCode() % 360) / 360f, 0.2f, 1));
		bm.img = getBufferedImage(g2d, text, c);
		long now =  System.currentTimeMillis();
		bm.w = bm.img.getWidth();
		bm.startTm = now;
		bm.emptyTime = now + bm.w * TIME_BARRAGE_DISPLAY / dimScreen.width ;
		bm.y = i * BARRAGE_SIZE;
		bm.x = dimScreen.width;
		
		
		barrageQueue.push(bm);
	}
	
	private Color cBorder = new Color(64, 64, 64, 192);
	private BufferedImage getBufferedImage(Graphics2D g, String text, Color c) {
		
		FontMetrics fm = g.getFontMetrics();
		Rectangle2D rect = fm.getStringBounds(text, g);
		
		int w = (int)Math.ceil(rect.getWidth()), h = (int)Math.ceil(rect.getHeight());
		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
		
		Graphics2D g2 = (Graphics2D) img.getGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		GlyphVector v = font.createGlyphVector(fm.getFontRenderContext(), text);
	    Shape shape = v.getOutline();
	    g2.translate(0, fm.getLeading() + fm.getAscent());
	    g2.setColor(c);
	    g2.fill(shape);
	    g2.setColor(cBorder);
	    g2.draw(shape);
	    g2.dispose();
		return img;
	}
}

class BarrageMessage {
	BufferedImage img;
	int x, y, w, row;
	long startTm, emptyTime;
}
