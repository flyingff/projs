package net.flyingff.snat.ui;

import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import net.flyingff.snat.NATEntry;
import net.flyingff.snat.NATEntry.NATStatus;

public class StatusDialog extends Shell {
	private static final int HEIGHT_OF_SHELL = 320;
	private static final int WIDTH_OF_SHELL = 640;
	private Table table;
	private List<NATEntry> entries;
	private TableViewer tableViewer;
	private final Runnable hideSelf;
	private final Color colorRed, colorGreen, colorBlue;
	
	public StatusDialog(Image icon, List<NATEntry> entries, Runnable hideListener) {
		super(Display.getCurrent(), SWT.CLOSE | SWT.MIN | SWT.TITLE);
		Display d = Display.getCurrent();
		colorRed = new Color(d, 0xCC, 0x33, 0x33);
		colorGreen = new Color(d, 0x99, 0xCC, 0x66);
		colorBlue = new Color(d, 0x33, 0x66, 0xCC);
		
		this.entries = entries;
		
		setText("Status - Soft NAT ver 1.0");
		setImage(icon);
		setSize(WIDTH_OF_SHELL, HEIGHT_OF_SHELL);
		
		createControls();
		hideSelf = ()->{
			setVisible(false);
			hideListener.run();
		};
		addListener(SWT.Close, ev->{
			ev.doit = false;
			hideSelf.run();
		});
		
		d.timerExec(0, this::refreshStatus);
	}
	
	private void refreshStatus() {
		Display d = Display.getCurrent();
		if(isDisposed()) return;
		if(isVisible()) {
			tableViewer.refresh();
		}
		d.timerExec(500, this::refreshStatus);
	}

	protected void createControls() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginBottom = 5;
		gridLayout.marginRight = 5;
		gridLayout.marginLeft = 5;
		gridLayout.marginTop = 5;
		gridLayout.numColumns = 2;
		setLayout(gridLayout);
		
		tableViewer = new TableViewer(this, SWT.BORDER | SWT.FULL_SELECTION);
		table = tableViewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 6));
		tableViewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public Object[] getElements(Object e) {
				if(e instanceof List) {
					return ((List<?>) e).toArray();
				}
				return new Object[0];
			}
		});
		
		TableViewerColumn tableViewerLocalPort = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnLocalPort = tableViewerLocalPort.getColumn();
		tblclmnLocalPort.setWidth(70);
		tblclmnLocalPort.setText("Local Port");
		tableViewerLocalPort.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object e) {
				if(e instanceof NATEntry) {
					return String.valueOf(((NATEntry) e).getLocalPort());
				}
				return String.valueOf(e);
			}
		});
		
		TableViewerColumn tableViewerExternalPort = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnExternalPort = tableViewerExternalPort.getColumn();
		tblclmnExternalPort.setWidth(91);
		tblclmnExternalPort.setText("External Port");
		tableViewerExternalPort.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object e) {
				if(e instanceof NATEntry) {
					return String.valueOf(((NATEntry) e).getExternalPort());
				}
				return String.valueOf(e);
			}
		});
		
		TableViewerColumn tableViewerIP = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnIpRegexp = tableViewerIP.getColumn();
		tblclmnIpRegexp.setWidth(102);
		tblclmnIpRegexp.setText("IP RegExp");
		tableViewerIP.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object e) {
				if(e instanceof NATEntry) {
					return ((NATEntry) e).getIpRegExp();
				}
				return String.valueOf(e);
			}
		});
		
		TableViewerColumn tableViewerStatus = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnStatus = tableViewerStatus.getColumn();
		tblclmnStatus.setWidth(72);
		tblclmnStatus.setText("Status");
		tableViewerStatus.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object e) {
				if(e instanceof NATEntry) {
					return ((NATEntry) e).getStatus().toString();
				}
				return String.valueOf(e);
			}
			@Override
			public Color getForeground(Object e) {
				if(e instanceof NATEntry) {
					NATStatus status =  ((NATEntry) e).getStatus();
					switch (status) {
					case ERROR:
						return colorRed;
					case STOPPING:
					case STARTED:
						return colorGreen;
					case STARTING:
					case STOPPED:
						return colorBlue;
					}
				}
				return null;
			}
		});
		
		
		TableViewerColumn tableViewerConn = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnConnections = tableViewerConn.getColumn();
		tblclmnConnections.setWidth(56);
		tblclmnConnections.setText("Connections");
		tableViewerConn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object e) {
				if(e instanceof NATEntry) {
					return String.valueOf(((NATEntry) e).getConnections());
				}
				return String.valueOf(e);
			}
		});
		
		TableViewerColumn tableViewerData = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnDataTransfered = tableViewerData.getColumn();
		tblclmnDataTransfered.setWidth(95);
		tblclmnDataTransfered.setText("Total Flow");
		tableViewerData.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object e) {
				if(e instanceof NATEntry) {
					return String.valueOf(((NATEntry) e).getDataTransfered());
				}
				return String.valueOf(e);
			}
		});
		
		Button btnNewEntry = new Button(this, SWT.NONE);
		GridData gd_btnNewEntry = new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1);
		gd_btnNewEntry.widthHint = 96;
		btnNewEntry.setLayoutData(gd_btnNewEntry);
		btnNewEntry.setText("New Entry...");
		btnNewEntry.addListener(SWT.Selection, ev-> newEntry());
		
		Button btnDeleteEntry = new Button(this, SWT.NONE);
		GridData gd_btnDeleteEntry = new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1);
		gd_btnDeleteEntry.widthHint = 96;
		btnDeleteEntry.setLayoutData(gd_btnDeleteEntry);
		btnDeleteEntry.setText("Delete Entry");
		btnDeleteEntry.addListener(SWT.Selection, ev->{
			TableItem[] selected = table.getSelection();
			if(selected.length > 0) {
				deleteEntry((NATEntry) selected[0].getData());
			}
		});
		Button btnRestrictIp = new Button(this, SWT.NONE);
		GridData gd_btnRestrictIp = new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1);
		gd_btnRestrictIp.widthHint = 96;
		btnRestrictIp.setLayoutData(gd_btnRestrictIp);
		btnRestrictIp.setText("IP Range...");
		btnRestrictIp.addListener(SWT.Selection, ev->{
			TableItem[] selected = table.getSelection();
			if(selected.length > 0) {
				configureIP((NATEntry) selected[0].getData());
			}
		});
		
		Button btnStart = new Button(this, SWT.NONE);
		GridData gd_btnStart = new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1);
		gd_btnStart.widthHint = 96;
		btnStart.setLayoutData(gd_btnStart);
		btnStart.setText("Start");
		btnStart.addListener(SWT.Selection, ev->{
			if(table.getSelectionCount() == 1) {
				NATEntry entry = (NATEntry) table.getSelection()[0].getData();
				if(entry.getStatus() == NATStatus.STOPPED || entry.getStatus() == NATStatus.ERROR) {
					entry.setStatus(NATStatus.STARTING);
					tableViewer.refresh();
				}
			}
		});
		
		Button btnStop = new Button(this, SWT.NONE);
		GridData gd_btnStop = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_btnStop.widthHint = 96;
		btnStop.setLayoutData(gd_btnStop);
		btnStop.setText("Stop");
		btnStop.addListener(SWT.Selection, ev->{
			if(table.getSelectionCount() == 1) {
				NATEntry entry = (NATEntry) table.getSelection()[0].getData();
				if(entry.getStatus() == NATStatus.STARTED) {
					entry.setStatus(NATStatus.STOPPING);
					tableViewer.refresh();
				} else if (entry.getStatus() == NATStatus.ERROR) {
					entry.setStatus(NATStatus.STOPPED);
					tableViewer.refresh();
				}
			}
		});
		
		Button btnClose = new Button(this, SWT.CENTER);
		GridData gd_btnClose = new GridData(SWT.CENTER, SWT.BOTTOM, false, false, 1, 1);
		gd_btnClose.widthHint = 96;
		btnClose.setLayoutData(gd_btnClose);
		btnClose.setText("Close");
		
		btnClose.addListener(SWT.Selection, ev->hideSelf.run());
		tableViewer.setInput(entries);
	}

	private void newEntry() {
		InputDialog id = new InputDialog(getShell(), "New Entry", "What's the local port?",
				"", x->{
					try {
						int val = Integer.parseInt(x);
						if(val < 1 || val > 65536) {
							return "Port out of range(1 ~ 65536 are valid)";
						}
						return null;
					} catch (NumberFormatException e) {
						return "It's not a number!";
					}
				});
		if(id.open() != Window.OK) {
			return;
		}
		int localPort = Integer.parseInt(id.getValue());
		id = new InputDialog(getShell(), "New Entry", "What's the external port?",
				"", x->{
					try {
						int val = Integer.parseInt(x);
						if(val < 1024 || val > 65536) {
							return "Port out of range(1024 ~ 65536 are valid)";
						}
						if(val == localPort) {
							return "Cannot be same as local port!";
						}
						return null;
					} catch (NumberFormatException e) {
						return "It's not a number!";
					}
				});
		if(id.open() != Window.OK) {
			return;
		}
		int externalPort = Integer.parseInt(id.getValue());
		
		entries.add(new NATEntry(localPort, externalPort));
		tableViewer.refresh();
	}
	
	private void deleteEntry(NATEntry entry) {
		entries.remove(entry);
		tableViewer.refresh();
	}
	
	private void configureIP(NATEntry entry) {
		InputDialog id = new InputDialog(getShell(), "Configure IP Regexp",
				"Specifie a regular expression that matches all valid external IP:", 
				entry.getIpRegExp(), x->{
					try {
						Pattern.compile(x);
					} catch (Exception e) {
						return "Invalid regular Expression";
					}
					return null;
				});
		if(id.open() == Window.OK) {
			if(!entry.setIpRegExp(id.getValue())) {
				throw new AssertionError();
			}
			tableViewer.refresh();
		}
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
	
	@Override protected void checkSubclass() { }
}
