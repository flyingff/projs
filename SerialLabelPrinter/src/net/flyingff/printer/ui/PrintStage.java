package net.flyingff.printer.ui;

import java.awt.image.BufferedImage;

import javax.swing.JButton;

import javax.swing.JProgressBar;import javax.swing.JComboBox;

import net.flyingff.printer.ImagePrinter;

public class PrintStage implements IProcessStage {
	private ImagePrinter printer = new ImagePrinter();
	private BufferedImage img;
	
	public PrintStage(JComboBox<String> comSelect, JButton print, JProgressBar progress) {
		ImagePrinter.avaiableCOMPorts().forEach(comSelect::addItem);
		
		progress.setMinimum(0);
		progress.setMaximum(100);
		print.addActionListener(ev->{
			progress.setValue(0);
			printer.print(img, progress::setValue);
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
