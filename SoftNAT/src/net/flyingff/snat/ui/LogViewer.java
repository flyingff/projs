package net.flyingff.snat.ui;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import net.flyingff.snat.ui.TheLogger.LogLine;

public class LogViewer extends Shell {
	private static final int HEIGHT_OF_SHELL = 480;
	private static final int WIDTH_OF_SHELL = 640;
	private static final Image[] ICONS; 
	private static final Color[] COLORS; static{
		String[] names = { "info.png", "error.png" };
		ICONS = new Image[names.length];
		ImageLoader il = new ImageLoader();
		int i = 0;
		Display d = Display.getDefault();
		for(String str : names) {
			il.load(LogViewer.class.getResourceAsStream(str));
			ICONS[i++] = new Image(d, il.data[0]);
		}
		COLORS = new Color[] {
			new Color(d, 0, 0x66, 0xCC), new Color(d, 0xFF, 0x66, 0x66)
		};
	}
	private Table table;
	private java.util.List<LogLine> lines = new ArrayList<>();
	
	public LogViewer(Display display) {
		super(display, SWT.CLOSE | SWT.MIN | SWT.TITLE);
		createContents();
		setVisible(true);
	}

	protected void createContents() {
		setText("Log Viewer  - Soft NAT ver 1.0");
		setSize(WIDTH_OF_SHELL, HEIGHT_OF_SHELL);
		
		setLayout(new GridLayout(1, false));
		
		TableViewer tableViewer = new TableViewer(this, SWT.BORDER | SWT.FULL_SELECTION);
		table = tableViewer.getTable();
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		TableViewerColumn tvcIcon = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnIcon = tvcIcon.getColumn();
		tblclmnIcon.setResizable(false);
		tblclmnIcon.setAlignment(SWT.CENTER);
		tblclmnIcon.setWidth(20);
		tblclmnIcon.setText("Icon");
		
		TableViewerColumn tvcText = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnText = tvcText.getColumn();
		tblclmnText.setWidth(100);
		tblclmnText.setText("Text");
		
		Button btnClose = new Button(this, SWT.NONE);
		GridData gd_btnClose = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_btnClose.widthHint = 96;
		btnClose.setLayoutData(gd_btnClose);
		btnClose.setText("Close");
		btnClose.addListener(SWT.Selection, ev->dispose());
		
		tableViewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public Object[] getElements(Object e) {
				if(e instanceof java.util.List) {
					return ((java.util.List<?>) e).toArray();
				}
				return new Object[0];
			}
		});
		
		tvcIcon.setLabelProvider(new ColumnLabelProvider() {
			public Image getImage(Object e) {
				if(e instanceof LogLine) {
					return ICONS[((LogLine) e).level];
				}
				return null;
			};
		});
		tvcText.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object e) {
				if(e instanceof LogLine) {
					return ((LogLine) e).str;
				}
				return String.valueOf(e);
			};
			public Color getForeground(Object e) {
				if(e instanceof LogLine) {
					return COLORS[((LogLine) e).level];
				}
				return null;
			};
		});
		
		lines.clear();
		synchronized (TheLogger.getInst()) {
			lines.addAll(TheLogger.getInst().getLines());
		}
		lines.sort(null);
		tableViewer.setInput(lines);
		tblclmnText.pack();
		table.setTopIndex(Integer.MAX_VALUE);
		
		TheLogger.getInst().registNewLineFunction(newLine->{
			getDisplay().asyncExec(()->{
				lines.add(newLine);
				tableViewer.refresh();
				tblclmnText.pack();
				table.setTopIndex(Integer.MAX_VALUE);
			});
		});
		addListener(SWT.Dispose, ev->{
			TheLogger.getInst().cancelNewLineFunction();
		});
		
	}
	
	@Override
	public void setVisible(boolean visible) {
		if(visible) {
			Rectangle bound = getDisplay().getPrimaryMonitor().getBounds();
			setLocation(bound.x + (bound.width - WIDTH_OF_SHELL) / 2, 
					bound.y + (bound.height - HEIGHT_OF_SHELL) / 2);
		}
		super.setVisible(visible);
	}
	
	@Override
	protected void checkSubclass() { }
}

