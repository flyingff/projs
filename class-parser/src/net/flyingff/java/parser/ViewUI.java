package net.flyingff.java.parser;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.AbstractListModel;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

public class ViewUI extends JFrame {
	private static final long serialVersionUID = -4801515324188278868L;
	public ViewUI(List<List<ClassViewInfo>> layers) {
		super("Class reference map");
		
		setSize(640, 480);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		List<String> listData = new ArrayList<>();
		NotifiableListModel model = new NotifiableListModel(listData);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setPreferredSize(new Dimension(480, 0));
		getContentPane().add(scrollPane, BorderLayout.EAST);
		JList<String> list = new JList<>(model);
		list.setBackground(new Color(0x333333));
		list.setForeground(new Color(0xeeeeff));
		list.setFont(new Font("Consolas", Font.PLAIN, 14));
		scrollPane.setViewportView(list);
		
		JComponent canvas = new ClassViewer(layers, (info, idepends)->{
			listData.clear();
			if(info != null) {
				listData.addAll(info.getMessages());
				listData.add("Indirect dependencies:");
				if (idepends.isEmpty()) {
					listData.add("  (empty)");
				} else {
					idepends.forEach(it->{
						listData.add("  " + it);
					});
				}
			}
			model.notifyChange();
			list.repaint();
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
	private static final int ALPHA = 0x3F;
	private static Color alpha(Color src) {
		return new Color((src.getRGB() & 0xFFFFFF) | (ALPHA << 24), true);
	}
	private static final Color COLOR_BG = new Color(0x333333),
			COLOR_BLOCK_FG = new Color(0xCDEFFF), COLOR_BLOCK_FG_T = alpha(COLOR_BLOCK_FG),
			COLOR_BLOCK_BG = new Color(0x336699), COLOR_BLOCK_BG_T = alpha(COLOR_BLOCK_BG),
			COLOR_BLOCK_BORDER = new Color(0x99CCFF), COLOR_BLOCK_BORDER_T = alpha(COLOR_BLOCK_BORDER),
			COLOR_LINE = new Color(0xFF99CC);
	
	private static final Font FONT = new Font("Consolas", Font.PLAIN, 14);
	private int dx = 0, dy = 0;
	private final int width, height;
	private Map<ClassViewInfo, Set<ClassViewInfo>> dependencyMap = new HashMap<>();
	private ClassViewInfo selected = null;
	private final Set<ClassViewInfo> selectedRelation = new HashSet<>();
	
	private final List<List<ClassViewInfo>> layers;
	public ClassViewer(List<List<ClassViewInfo>> layers, IOnSelection onSelectListener) {
		this.layers = layers;
		BufferedImage im = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
		Graphics g = im.getGraphics();
		FontMetrics fm = g.getFontMetrics(FONT);
		
		Map<String, ClassViewInfo> index = new HashMap<>();
		for(List<ClassViewInfo> list : layers) {
			for(ClassViewInfo info : list) {
				info.updateWidth(fm);
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
		
		int[] widths = layers.stream()
					.mapToInt(it->it.stream().mapToInt(info->info.getRect().width + H_GAP).sum() - H_GAP)
					.toArray();
		width = Arrays.stream(widths).max().orElse(0);
		height = layers.size() * (ClassViewInfo.HEIGHT + V_GAP);
		for(int layer = layers.size() - 1; layer >= 0; layer--) {
			List<ClassViewInfo> list = layers.get(layer);
			int marginLeft = (width - widths[layer]) / 2;
			int marginTop = (layers.size() - 1 - layer) * (ClassViewInfo.HEIGHT + V_GAP);
			
			int x = marginLeft;
			for(ClassViewInfo info : list) {
				Rectangle rect = info.getRect();
				rect.setLocation(x, marginTop);
				x += rect.width + H_GAP;
			}
		}
		
		dx = (width + getWidth()) / 2;
		dy = (height + getHeight()) / 2;
		MouseAdapter mouseListener = new MouseAdapter() {
			private int lastX, lastY;
			@Override
			public void mouseClicked(MouseEvent e) {
				int x = e.getX() + dx, y = e.getY() + dy;
				selected = getSelectInfo(x, y);
				
				selectedRelation.clear();
				if(selected != null) {
					calculateRelation();
					
					Set<String> idep = selectedRelation.stream()
							.map(ClassViewInfo::getName)
							.collect(Collectors.toSet());
					idep.remove(selected.getName());
					new HashSet<>(idep).forEach(it->{
						if(it.startsWith("!GROUP")) {
							idep.addAll(index.get(it).getContentClass());
							idep.remove(it);
						}
					});
					idep.removeAll(selected.getDependencies());
					
					onSelectListener.onSelection(selected, idep.stream()
							.sorted().collect(Collectors.toList()));
				} else {
					onSelectListener.onSelection(null, Collections.emptyList());
				}
				repaint();
			}
			private void calculateRelation() {
				// get indirect dependencies
				Deque<String> toResolve = new ArrayDeque<>(Arrays.asList(selected.getName()));
				while(!toResolve.isEmpty()) {
					ClassViewInfo info = index.get(toResolve.pollFirst());
					if(selectedRelation.add(info)) {
						toResolve.addAll(info.getDependencies());
					}
				}
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
		
		g.dispose();
	}
	
	private final Stroke sRelation = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1),
			sBorder = new BasicStroke(1);
	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setStroke(sBorder);
		// background
		g.setColor(COLOR_BG);
		g.fillRect(0, 0, getWidth(), getHeight());
		
		
		// font y
		g.setFont(FONT);
		FontMetrics fm = g.getFontMetrics();
		int stringY = (ClassViewInfo.HEIGHT - fm.getHeight()) / 2 + fm.getAscent() + 1;
		
		// draw each block
		for(List<ClassViewInfo> list : layers) {
			for(ClassViewInfo info : list) {
				boolean t = selected != null && !selectedRelation.contains(info);
				
				Rectangle rect = info.getRect();
				int x = rect.x - dx,
						y = rect.y - dy,
						w = rect.width,
						h = rect.height;
				g.setColor(t ? COLOR_BLOCK_BG_T : COLOR_BLOCK_BG);
				g.fillRect(x, y, w, h);
				g.setColor(t ? COLOR_BLOCK_BORDER_T : COLOR_BLOCK_BORDER);
				g.drawRect(x, y, w, h);
				
				g.setColor(t ? COLOR_BLOCK_FG_T : COLOR_BLOCK_FG);
				int stringW = Math.min(w, fm.stringWidth(info.getSimpleName()));
				g.drawString(info.getSimpleName(), (w - stringW) / 2 + x, stringY + y);
			}
		}
		
		g.setColor(COLOR_LINE);
		g2d.setStroke(sRelation);
		// draw connection lines
		for(ClassViewInfo info : selectedRelation) {
			if(info == null) throw new AssertionError();
			
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

class NotifiableListModel extends AbstractListModel<String> {
	private static final long serialVersionUID = 1L;
	private final List<String> data;
	public NotifiableListModel(List<String> data) {
		this.data = data;
	}
	@Override public int getSize() { return data.size(); }
	@Override public String getElementAt(int index) { return data.get(index); }
	public void notifyChange() {
		super.fireContentsChanged(this, 0, getSize() - 1);
	}
};

interface IOnSelection {
	void onSelection(ClassViewInfo selected, Collection<String> dependencies);
}