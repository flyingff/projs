package net.flyingff.snat.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Date;
import java.util.Deque;
import java.util.function.Consumer;

public class TheLogger {
	private static final int MAX_LINE = 100;
	private static final TheLogger INST = new TheLogger();
	public static TheLogger getInst() {
		return INST;
	}
	public static void log(Object... ln) {
		String text = String.join(" ", (Iterable<String>)Arrays.stream(ln)
				.map(String::valueOf)::iterator);
		INST.write(0, text);
	}
	public static void err(Object... ln) {
		String text = String.join(" ", (Iterable<String>)Arrays.stream(ln)
				.map(String::valueOf)::iterator);
		INST.write(1, text);
	}
	
	private Deque<LogLine> lines = new ArrayDeque<>();
	private Consumer<LogLine> newLineAdded;
	
	private TheLogger() {}
	public void registNewLineFunction(Consumer<LogLine> newLineAdded) {
		this.newLineAdded = newLineAdded;
	}
	public void cancelNewLineFunction() {
		this.newLineAdded = null;
	}
	public synchronized void write(int level, String line) {
		LogLine newLine = new LogLine(line, level);
		lines.addLast(newLine);
		if(lines.size() > MAX_LINE) {
			lines.removeFirst();
		}
		if(newLineAdded != null) {
			newLineAdded.accept(newLine);
		}
	}
	public Deque<LogLine> getLines() {
		return lines;
	}
	
	public static final class LogLine implements Comparable<LogLine>{
		private static final DateFormat DF = SimpleDateFormat.getDateTimeInstance(
				SimpleDateFormat.SHORT, SimpleDateFormat.SHORT);
		public final String str;
		public final int level;
		public final long tm = System.currentTimeMillis();
		public LogLine(String str, int level) {
			this.str = str;
			this.level = level;
		}
		@Override
		public String toString() {
			return String.format("[%s] %s", DF.format(new Date(tm)), str);
		}
		@Override
		public int compareTo(LogLine o) {
			return Long.compare(tm, o.tm);
		}
	}
}

