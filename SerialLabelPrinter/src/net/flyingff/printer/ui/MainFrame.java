package net.flyingff.printer.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class MainFrame extends JFrame {
	/*
	private static final String TEXT_STOP_PRINTING = "停止打印";
	private static final String TEXT_START_PRINT = "开始打印";
	private static final String COM_PREFIX = "COM";
	*/
	private static final long serialVersionUID = -9031352517578036795L;
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					MainFrame frame = new MainFrame();
					Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
						@Override
						public void uncaughtException(Thread t, Throwable e) {
							EventQueue.invokeLater(()->{
								JOptionPane.showMessageDialog(frame, e.getMessage(), "出错了",
										JOptionPane.ERROR_MESSAGE);
								e.printStackTrace();
							});
						}
					});
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public MainFrame() {
		setResizable(false);
		setTitle("\u6807\u7B7E\u6253\u5370\u7A0B\u5E8F");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JPanel panel_1 = new JPanel();
		contentPane.add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new GridLayout(0, 1, 0, 0));
		
		JPanel panel_2 = new JPanel();
		panel_1.add(panel_2);
		panel_2.setLayout(new BorderLayout(0, 0));
		
		JLabel label_1 = new JLabel("\u5904\u7406\u524D\u56FE\u7247");
		label_1.setBorder(new EmptyBorder(4, 0, 4, 0));
		label_1.setHorizontalAlignment(SwingConstants.CENTER);
		panel_2.add(label_1, BorderLayout.NORTH);
		
		ImageIcon icSrc = new ImageIcon(), icDest = new ImageIcon();
		JLabel lblOriginPicture = new JLabel(icSrc);
		lblOriginPicture.setBorder(new LineBorder(Color.LIGHT_GRAY));
		lblOriginPicture.setPreferredSize(new Dimension(384, 240));
		panel_2.add(lblOriginPicture, BorderLayout.CENTER);
		
		JPanel panel_3 = new JPanel();
		panel_1.add(panel_3);
		panel_3.setLayout(new BorderLayout(0, 0));
		
		JLabel label_2 = new JLabel("\u5904\u7406\u540E\u56FE\u7247");
		label_2.setBorder(new EmptyBorder(4, 0, 4, 0));
		label_2.setHorizontalAlignment(SwingConstants.CENTER);
		panel_3.add(label_2, BorderLayout.NORTH);
		
		JLabel lblProcessedPicture = new JLabel(icDest);
		lblProcessedPicture.setBorder(new LineBorder(Color.LIGHT_GRAY));
		lblProcessedPicture.setPreferredSize(new Dimension(384, 240));
		panel_3.add(lblProcessedPicture, BorderLayout.CENTER);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setPreferredSize(new Dimension(264, 5));
		contentPane.add(tabbedPane, BorderLayout.WEST);
		
		JPanel panel_7 = new JPanel();
		panel_7.setBackground(Color.WHITE);
		tabbedPane.addTab("\u6E90", null, panel_7, null);
		panel_7.setLayout(new BorderLayout(0, 0));
		
		JLabel lblSource = new JLabel("\u62D6\u52A8\u56FE\u7247\u81F3\u6B64");
		lblSource.setBackground(Color.WHITE);
		lblSource.setHorizontalAlignment(SwingConstants.CENTER);
		panel_7.add(lblSource, BorderLayout.CENTER);
		
		JButton btnSelectPic = new JButton("\u6216\u70B9\u6211\u9009\u53D6\u56FE\u7247");
		panel_7.add(btnSelectPic, BorderLayout.SOUTH);
		
		JPanel panel_8 = new JPanel();
		panel_8.setBackground(Color.WHITE);
		tabbedPane.addTab("\u88C1\u526A", null, panel_8, null);
		panel_8.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JPanel panel_12 = new JPanel();
		panel_12.setBackground(Color.WHITE);
		panel_12.setPreferredSize(new Dimension(256, 80));
		panel_12.setName("");
		panel_8.add(panel_12);
		panel_12.setLayout(new GridLayout(0, 1, 0, 0));
		
		JLabel lblxY = new JLabel("\u5E73\u79FB\u8C03\u6574(X, Y)");
		lblxY.setHorizontalAlignment(SwingConstants.CENTER);
		panel_12.add(lblxY);
		
		JSlider sliderX = new JSlider();
		sliderX.setBackground(Color.WHITE);
		panel_12.add(sliderX);
		
		JSlider sliderY = new JSlider();
		sliderY.setBackground(Color.WHITE);
		panel_12.add(sliderY);
		
		JPanel panel_13 = new JPanel();
		panel_13.setBackground(Color.WHITE);
		panel_13.setPreferredSize(new Dimension(256, 50));
		panel_8.add(panel_13);
		panel_13.setLayout(new GridLayout(0, 1, 0, 0));
		
		JLabel label_5 = new JLabel("\u7F29\u653E\u8C03\u6574(%)");
		label_5.setHorizontalAlignment(SwingConstants.CENTER);
		panel_13.add(label_5);
		
		JSlider sliderScale = new JSlider();
		sliderScale.setBackground(Color.WHITE);
		panel_13.add(sliderScale);
		
		JPanel panel_9 = new JPanel();
		panel_9.setBackground(Color.WHITE);
		tabbedPane.addTab("\u7070\u5EA6", null, panel_9, null);
		panel_9.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JLabel label_7 = new JLabel("\u4EAE\u5EA6\u8C03\u6574");
		panel_9.add(label_7);
		
		JSlider sliderLight = new JSlider();
		sliderLight.setBackground(Color.WHITE);
		sliderLight.setPreferredSize(new Dimension(256, 26));
		panel_9.add(sliderLight);
		
		JLabel label_6 = new JLabel("\u66F2\u7EBF\u8C03\u6574");
		panel_9.add(label_6);
		
		JLabel panelLight = new JLabel();
		panelLight.setBorder(new LineBorder(Color.LIGHT_GRAY));
		panelLight.setPreferredSize(new Dimension(256, 256));
		panel_9.add(panelLight);
		
		JPanel panel_10 = new JPanel();
		panel_10.setBackground(Color.WHITE);
		tabbedPane.addTab("\u6296\u52A8", null, panel_10, null);
		panel_10.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JLabel label_8 = new JLabel("\u6296\u52A8\u65B9\u6CD5");
		panel_10.add(label_8);
		
		JList<String> list = new JList<String>();
		list.setBorder(new LineBorder(Color.LIGHT_GRAY));
		list.setPreferredSize(new Dimension(250, 320));
		panel_10.add(list);
		tabbedPane.setEnabledAt(3, true);
		
		JPanel panel_11 = new JPanel();
		panel_11.setBackground(Color.WHITE);
		tabbedPane.addTab("\u8F93\u51FA", null, panel_11, null);
		
		JPanel panel_14 = new JPanel();
		panel_14.setBackground(Color.WHITE);
		panel_14.setPreferredSize(new Dimension(256, 24));
		panel_14.setMinimumSize(new Dimension(256, 20));
		panel_11.add(panel_14);
		panel_14.setLayout(new BorderLayout(0, 0));
		
		JLabel label = new JLabel("\u4F7F\u7528\u7AEF\u53E3:");
		label.setPreferredSize(new Dimension(80, 15));
		panel_14.add(label, BorderLayout.WEST);
		
		JComboBox<String> selectCOM = new JComboBox<String>();
		panel_14.add(selectCOM);
		
		JPanel panel_15 = new JPanel();
		panel_15.setBackground(Color.WHITE);
		panel_15.setPreferredSize(new Dimension(256, 24));
		panel_11.add(panel_15);
		panel_15.setLayout(new BorderLayout(0, 0));
		
		JProgressBar progressBar = new JProgressBar();
		panel_15.add(progressBar);
		
		JButton btnPrint = new JButton("\u5F00\u59CB\u6253\u5370");
		btnPrint.setPreferredSize(new Dimension(80, 23));
		panel_15.add(btnPrint, BorderLayout.WEST);
		tabbedPane.setEnabledAt(4, true);
		
		imageChangeNotifier = (before, after) ->{
			if(before == null) {
				lblOriginPicture.setIcon(null);
			} else {
				icSrc.setImage(before);
				lblOriginPicture.setIcon(icSrc);
			}
			if(after == null) {
				lblProcessedPicture.setIcon(null);
			} else {
				icDest.setImage(after);
				lblProcessedPicture.setIcon(icDest);
			}
			lblOriginPicture.repaint();
			lblProcessedPicture.repaint();
		};
		// configure process stages
		processStages = Arrays.asList(new SourceStage(this, btnSelectPic, lblSource),
				new CropStage(sliderX, sliderY, sliderScale),
				new GrayifyStage(sliderLight, panelLight),
				new DitherStage(list),
				new PrintStage(selectCOM, btnPrint, progressBar));
		
		for(int i = 0; i < processStages.size(); i++) {
			final int index = i;
			processStages.get(i).setUpdateRequiredListener(()->{
				onTabChanged(index, true);
			});
			cachedOutputImage.add(null);
		}
		
		tabbedPane.addChangeListener(ev->{
			onTabChanged(tabbedPane.getSelectedIndex(), false);
		});
		tabbedPane.setSelectedIndex(0);
		onTabChanged(0, true);
		
		display();
	}
	private final BiConsumer<BufferedImage, BufferedImage> imageChangeNotifier;
	private final List<BufferedImage> cachedOutputImage = new ArrayList<>();
	private final List<IProcessStage> processStages;
	
	private void onTabChanged(int selectedIndex, boolean update) {
		if(update) {
			for(int i = selectedIndex; i < processStages.size(); i++) {
				cachedOutputImage.set(i, null);
			}
		}
		for(int i = 0; i <= selectedIndex; i++) {
			if(cachedOutputImage.get(i) == null) {
				BufferedImage input = i == 0 ? null : 
					cachedOutputImage.get(i - 1);
				BufferedImage output = processStages.get(i).process(input);
				if(output == null) break;
				cachedOutputImage.set(i, output);
			}
		}
		
		imageChangeNotifier.accept(selectedIndex == 0 ? null: 
			 cachedOutputImage.get(selectedIndex - 1),
			 cachedOutputImage.get(selectedIndex));
	}
	
	private void display() {
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
}
