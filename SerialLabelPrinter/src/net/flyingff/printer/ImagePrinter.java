package net.flyingff.printer;

import java.awt.EventQueue;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ImagePrinter {
	private String comPort = null;
	private boolean stopFlag = false;
	private OutputStream os = null;
	
	
	private static final byte[] CMD_START_PAGE = {
			0x1A, 0x5B, 0x01,						// command
			0x00, 0x00, 0x00, 0x00, 				// page offset X/Y
			(byte) 0x80, 0x01, (byte) 0xf0, 0x00, 	// page size (384 * 240)
			00										// page direction (horizontal)
	}, CMD_END_PAGE = {
			0x1A, 0x5D, 0x00
	}, CMD_PRINT_PAGE = {
			0x1A, 0x4F, 0x00
	}, CMD_BITMAP = {
			0x1A, 0x21, 0x01,						// command
			0x00, 0x00, 0x00, 0x00,					// image offset X/Y
			(byte) 0x80, 0x01, (byte) 0xf0, 0x00,	// image size
			0x00, 0x11								// painting options(no zoom, 
													// 		no rotation, no inverse color)
	};

	public static final List<String> avaiableCOMPorts() {
		return IntStream.rangeClosed(1, 16)
				.mapToObj(it->String.format("COM%d", it))
				.filter(it->Files.exists(Paths.get(it)))
				.collect(Collectors.toList());
	}
	public void setComPort(String comPort) {
		this.comPort = comPort;
	}
	public void print(BufferedImage image, Consumer<Integer> processNotifier) {
		if(comPort == null) {
			throw new RuntimeException("请指定打印串口");
		}
		if(image == null) {
			throw new RuntimeException("请选择要打印的图片");
		}
		stopFlag = false;
		new Thread(()->{
			try {
				byte[] imgData = imageSerializer(image, x->{
					processNotifier.accept((int) (x * 0.4));
				});
				if(stopFlag) return;
				commandSender(imgData, x->{
					processNotifier.accept((int) (x * 0.6) + 40);
				});
				if(stopFlag) return;
			} catch (Exception e) {
				e.printStackTrace();
				EventQueue.invokeLater(()->{
					processNotifier.accept(-1);
					throw new RuntimeException("打印出错了:" + e.getMessage());
				});
			}
		}).start();
	}
	
	private byte[] imageSerializer(BufferedImage img, Consumer<Integer> progress) {
		int w = img.getWidth(), h = img.getHeight();
		int index = 0, total = w * h;
		int lastReported = 0;
		BitSet bs = new BitSet();
		for(int j = 0; j < h; j++) {
			for(int i = 0; i < w; i++) {
				bs.set((index / 8 * 8) + (7 - index % 8), (img.getRGB(i, j) & 0xFF) < 128);
				index++;
				int prog = index * 100 / total;
				if(prog > lastReported) {
					progress.accept(prog);
					lastReported = prog;
				}
				if(stopFlag) return null;
			}
		}
		// ensure its length
		return Arrays.copyOf(bs.toByteArray(), index / 8);
	}
	
	private static final int BULK_SIZE = 4096;
	private void commandSender(byte[] imageData, Consumer<Integer> progress) throws IOException {
		try(FileOutputStream fos = new FileOutputStream(comPort)) {
			os = fos;
			
			fos.write(CMD_START_PAGE);
			if(stopFlag) return;
			progress.accept(2);
			fos.write(CMD_BITMAP);
			if(stopFlag) return;
			progress.accept(3);
			
			int total = imageData.length, curr = 0;
			while(curr < total) {
				int writeLen = Math.min(BULK_SIZE, total - curr);
				fos.write(imageData, curr, writeLen);
				fos.flush();
				curr += writeLen;
				progress.accept((curr * 95 / total) + 3);
				
				if(stopFlag) return;
			}
			
			fos.write(CMD_END_PAGE);
			fos.write(CMD_PRINT_PAGE);
			if(stopFlag) return;
			fos.flush();
			progress.accept(100);
		} catch (IOException e) {
			if(!stopFlag) {
				throw e;
			} else e.printStackTrace();
		} finally {
			os = null;
		}
	}
	public void stop() {
		stopFlag = true;
		if(os != null) {
			try {
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
