package net.flyingff.printer.ui;

import java.awt.image.BufferedImage;

import javax.swing.JButton;

import javax.swing.JProgressBar;import javax.swing.JComboBox;

import net.flyingff.printer.ImagePrinter;

public class PrintStage implements IProcessStage {
	private static final String START_PRINTING = "开始打印";
	private static final String STOP_PRINTING = "停止打印";
	private ImagePrinter printer = new ImagePrinter();
	private BufferedImage img;
	
	private boolean printing = false;
	public PrintStage(JComboBox<String> comSelect, JButton print, JProgressBar progress) {
		ImagePrinter.avaiableCOMPorts().forEach(comSelect::addItem);
		
		progress.setMinimum(0);
		progress.setMaximum(100);
		print.addActionListener(ev->{
			print.setEnabled(false);
			try {
				if(printing) {
					printer.stop();
					printing = false;
				} else {
					progress.setValue(0);
					printer.setComPort((String) comSelect.getSelectedItem());
					printer.print(img, v->{
						progress.setValue(v);
						if(v >= 100) {
							printing = false;
							print.setText(STOP_PRINTING);
						}
					});
					printing = true;
				}
			} finally {
				print.setText(printing ? STOP_PRINTING : START_PRINTING);
				print.setEnabled(true);
			}
		});
		
		comSelect.setSelectedIndex(0);
		printer.setComPort(comSelect.getItemAt(0));
	}
	@Override
	public void setUpdateRequiredListener(Runnable listener) { }
	
	@Override
	public BufferedImage process(BufferedImage img) {
		this.img = img;
		return null;
	}

}
