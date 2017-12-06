package net.flyingff.snat.ui;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;

import net.flyingff.snat.NATEntry;
import net.flyingff.snat.ProxyManager;

public class Application {
	private final TrayItem tray;
	private final Image icon;
	private StatusDialog sh;
	private LogViewer logViewer;
	
	private List<NATEntry> entries;
	
	private Application() {
		Display d = Display.getDefault();
		this.icon = loadIcon(d);
		loadEntries();
		sh = new StatusDialog(icon, entries, this::saveEntries);
		sh.setVisible(false);
		
		this.tray = createTray(d);
		createTrayMenu();
		new ProxyManager(entries);

		while(!sh.isDisposed()) {
			if(d.readAndDispatch()) {
				d.sleep();
			}
		}
		saveEntries();
		d.dispose();
	}
	private TrayItem createTray(Display d) {
		Tray systemTray = d.getSystemTray();
		if(systemTray == null) {
			throw new RuntimeException("Tray is not supported");
		}
		
		TrayItem tray = new TrayItem(systemTray, SWT.NONE);
		
		tray.setText("Soft NAT");
		tray.setImage(icon);
		tray.setToolTipText("Soft NAT");
		tray.setVisible(true);
		return tray;
	}
	private void createTrayMenu() {
		// pop-up menu
		Menu popup = new Menu(sh, SWT.POP_UP);
		tray.addListener(SWT.MenuDetect, ev->{
			popup.setVisible(true);
		});
		tray.addListener(SWT.DefaultSelection, ev->{
			showStatusDialog();
		});
		
		MenuItem itShowDialog = new MenuItem(popup, SWT.NONE);
		itShowDialog.setText("Show Status Dialog");
		
		itShowDialog.addListener(SWT.Selection, ev->{
			showStatusDialog();
		});
		
		MenuItem itShowLog = new MenuItem(popup, SWT.NONE);
		itShowLog.setText("Show Log Viewer");
		itShowLog.addListener(SWT.Selection, ev->{
			if(logViewer != null) {
				logViewer.setActive();
				logViewer.setFocus();
			} else {
				logViewer = new LogViewer(Display.getDefault());
				logViewer.addListener(SWT.Dispose, e->logViewer = null);
			}
		});
		
		
		new MenuItem(popup, SWT.SEPARATOR);
		MenuItem itExit = new MenuItem(popup, SWT.NONE);
		itExit.setText("Exit");
		itExit.addListener(SWT.Selection, ev->{
			sh.dispose();
			tray.dispose();
		});
	}
	private Image loadIcon(Display d) {
		ImageLoader il = new ImageLoader();
		il.load(Application.class.getResourceAsStream("icon.png"));
		Image icon = new Image(d, il.data[0]);
		Dialog.setDefaultImage(icon);
		return icon;
	}
	public Image getIcon() {
		return icon;
	}
	private void showStatusDialog() {
		sh.setVisible(true);
		sh.setActive();
		sh.setFocus();
	}
	
	@SuppressWarnings("unchecked")
	private void loadEntries() {
		File storageFile = new File(System.getProperty("user.home"), ".snat");
		if(!storageFile.exists()) {
			entries = new ArrayList<>();
			return;
		}
		try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(storageFile))) {
			entries = (List<NATEntry>) ois.readObject();
			entries.forEach(it->it.restore());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void saveEntries() {
		File storageFile = new File(System.getProperty("user.home"), ".snat");
		
		try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(storageFile))) {
			oos.writeObject(entries);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {
		new Application();
	}
}
