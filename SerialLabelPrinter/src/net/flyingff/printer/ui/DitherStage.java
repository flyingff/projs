package net.flyingff.printer.ui;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JList;

import net.flyingff.printer.DitherProcessor;
import net.flyingff.printer.DitherProcessor.DitherMethod;

public class DitherStage implements IProcessStage {
	private Map<String, DitherMethod> map = new HashMap<>();
	private DitherMethod method;
	public DitherStage(JList<String> list) {
		DefaultListModel<String> model = new DefaultListModel<>();
		list.setModel(model);
		DitherProcessor.allDitherMethods().forEachRemaining(x->{
			model.addElement(x.name());
			map.put(x.name(), x);
		});
		
		list.addListSelectionListener(ev->{
			method = map.get(list.getSelectedValue());
			if(updateListener != null) {
				updateListener.run();
			}
		});
		list.setSelectedIndex(0);
		method = map.get(model.get(0));
	}
	private Runnable updateListener;
	@Override
	public void setUpdateRequiredListener(Runnable listener) {
		this.updateListener = listener;
	}

	@Override
	public BufferedImage process(BufferedImage img) {
		return method.process(img);
	}

}
