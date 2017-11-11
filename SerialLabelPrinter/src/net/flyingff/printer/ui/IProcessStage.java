package net.flyingff.printer.ui;

import java.awt.image.BufferedImage;

public interface IProcessStage {
	default void onActive() {};
	default void onDeactive() {};
	void setUpdateRequiredListener(Runnable listener);
	BufferedImage process(BufferedImage img);
}
