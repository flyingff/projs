package net.flyingff.printer.ui;

import java.awt.FileDialog;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;

public class SourceStage implements IProcessStage {
	public SourceStage(JFrame frame, JButton btnSelect, JComponent dropTarget) {
		btnSelect.addActionListener(ev->{
			FileDialog fd = new FileDialog(frame);
			fd.setTitle("选择图片");
			fd.setMode(FileDialog.LOAD);

			fd.setVisible(true);
			
			Optional.ofNullable(fd.getFile())
				.map(it->new File(fd.getDirectory(), it))
				.ifPresent(this::loadImage);
		});
		dropTarget.setDropTarget(new DropTarget() {
			private static final long serialVersionUID = 1L;
			public synchronized void drop(DropTargetDropEvent e) {
				try {
					e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
					Transferable t = e.getTransferable();
					List<?> fileList = (List<?>) t.getTransferData(DataFlavor.javaFileListFlavor);
					File file = (File) fileList.iterator().next();
					loadImage(file);
					e.dropComplete(true);
				} catch (UnsupportedFlavorException ex) {
					e.dropComplete(false);
				} catch (IOException e1) {
					throw new RuntimeException(e1);
				}
			}
		});
	}
	
	
	private BufferedImage src = null;
	public void loadImage(File f) {
		try {
			src = ImageIO.read(f);
			if(src != null) {
				/*if(src.getWidth() != 384 || src.getHeight() != 240) try {
					throw new RuntimeException(String.format("图片大小应当为%dx%d, 实际大小为%dx%d",
							PIC_WIDTH, PIC_HEIGHT, src.getWidth(), src.getHeight()));
				} finally {
					src = null;
				}*/
				if(updateListener != null) {
					updateListener.run();
				}
			} else throw new IOException("EMPTY");
		} catch (IOException e) {
			e.printStackTrace();
			src = null;
			throw new RuntimeException("打开图片出错！");
		}
	}
	
	private Runnable updateListener;
	@Override
	public void setUpdateRequiredListener(Runnable listener) {
		this.updateListener = Objects.requireNonNull(listener);
	}

	@Override
	public BufferedImage process(BufferedImage img) {
		return src;
	}

}
