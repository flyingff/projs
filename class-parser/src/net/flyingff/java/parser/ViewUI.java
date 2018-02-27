package net.flyingff.java.parser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;

public class ViewUI extends JFrame {
	private static final long serialVersionUID = -4801515324188278868L;
	public ViewUI(List<List<ClassViewInfo>> layers) {
		super("Class reference map");
		
		setSize(640, 480);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JList<String> list = new JList<>();
		list.setPreferredSize(new Dimension(180, 0));
		getContentPane().add(list, BorderLayout.EAST);
		
		JComponent canvas = new ClassViewer(layers, info->{
			System.out.println("Select: " + info);
		});
		getContentPane().add(canvas, BorderLayout.CENTER);
		
		setLocationRelativeTo(null);
		setVisible(true);
	}
	public static void main(String[] args) {
		new ViewUI(Collections.emptyList());
	}
}

class ClassViewer extends JComponent {
	private static final int V_GAP = 64, H_GAP = 12;
	private static final long serialVersionUID = 1L;
	private static Color COLOR_BG = new Color(0xf4f2ff),
			COLOR_BLOCK_FG = Color.GREEN,
			COLOR_BLOCK_BG = Color.blue,
			COLOR_BLOCK_BORDER = Color.yellow,
			COLOR_LINE = Color.black;
	private int dx = 0, dy = 0;
	private final int width, height;
	private Map<ClassViewInfo, Set<ClassViewInfo>> dependencyMap = new HashMap<>();
	private ClassViewInfo selected = null;
	
	private final List<List<ClassViewInfo>> layers;
	public ClassViewer(List<List<ClassViewInfo>> layers, Consumer<ClassViewInfo> onSelectListener) {
		this.layers = layers;
		Map<String, ClassViewInfo> index = new HashMap<>();
		for(List<ClassViewInfo> list : layers) {
			for(ClassViewInfo info : list) {
				index.put(info.getName(), info);
			}
		}
		for(List<ClassViewInfo> list : layers) {
			for(ClassViewInfo info : list) {
				dependencyMap.put(info, info.getDependencies().stream()
						.map(index::get)
						.collect(Collectors.toSet()));
			}
		}
		
		width = layers.stream().mapToInt(List::size).max().getAsInt() * (ClassViewInfo.WIDTH + H_GAP) - H_GAP;
		height = layers.size() * (ClassViewInfo.HEIGHT + V_GAP);
		for(int layer = layers.size() - 1; layer >= 0; layer--) {
			List<ClassViewInfo> list = layers.get(layer);
			int marginLeft = (width - (list.size() * (ClassViewInfo.WIDTH + H_GAP) - H_GAP)) / 2;
			int marginTop = (layers.size() - 1 - layer) * (ClassViewInfo.HEIGHT + V_GAP);
			
			int x = marginLeft, d = ClassViewInfo.WIDTH + H_GAP;
			for(ClassViewInfo info : list) {
				info.getRect().setLocation(x, marginTop);
				x += d;
			}
		}
		
		dx = (width + getWidth()) / 2;
		dy = (height + getHeight()) / 2;
		MouseAdapter mouseListener = new MouseAdapter() {
			private int lastX, lastY;
			@Override
			public void mouseClicked(MouseEvent e) {
				int x = e.getX() + dx, y = e.getY() + dy;
				onSelectListener.accept(selected = getSelectInfo(x, y));
				repaint();
			}
			private ClassViewInfo getSelectInfo(int x, int y) {
				if(x < 0 || x >= width || y < 0 || y >= height) {
					return null;
				}
				int layer = layers.size() - 1 - y / (ClassViewInfo.HEIGHT + V_GAP);
				List<ClassViewInfo> list = layers.get(layer);
				return list.stream()
						.filter(it->it.getRect().contains(x, y))
						.findAny().orElse(null);
			}
			@Override
			public void mousePressed(MouseEvent e) { lastX = e.getX(); lastY = e.getY(); }
			@Override
			public void mouseDragged(MouseEvent e) {
				dx += lastX - e.getX();
				dy += lastY - e.getY();
				lastX = e.getX(); lastY = e.getY();
				repaint();
			}
		};
		addMouseListener(mouseListener);
		addMouseMotionListener(mouseListener);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		// background
		g.setColor(COLOR_BG);
		g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
		
		// font y
		FontMetrics fm = g.getFontMetrics();
		int stringY = (ClassViewInfo.HEIGHT - fm.getHeight()) / 2 + fm.getAscent();
		
		// draw each block
		for(List<ClassViewInfo> list : layers) {
			for(ClassViewInfo info : list) {
				Rectangle rect = info.getRect();
				int x = rect.x - dx,
						y = rect.y - dy,
						w = rect.width,
						h = rect.height;
				g.setColor(COLOR_BLOCK_BG);
				g.fillRect(x, y, w, h);
				g.setColor(COLOR_BLOCK_BORDER);
				g.drawRect(x, y, w, h);
				
				g.setColor(COLOR_BLOCK_FG);
				int stringW = Math.min(w, fm.stringWidth(info.getSimpleName()));
				g.drawString(info.getSimpleName(), (w - stringW) / 2 + x, stringY + y);
			}
		}
		
		g.setColor(COLOR_LINE);
		// draw connection lines
		for(ClassViewInfo info : Arrays.asList(selected)) {
			if(info == null) continue;
			
			Rectangle rect = info.getRect();
			int x = (int) (rect.x - dx + ClassViewInfo.WIDTH / 2),
					y = (int) (rect.y - dy + ClassViewInfo.HEIGHT);
			for(ClassViewInfo other : dependencyMap.get(info)) {
				Rectangle r2 = other.getRect();
				g.drawLine(x, y, r2.x - dx + ClassViewInfo.WIDTH / 2, r2.y - dy);
			}
		}
	}
}