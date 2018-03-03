package net.flyingff.java.parser;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class ClassInfo {
	private String name;
	private int modifiers;
	
	private final List<String> references = new ArrayList<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPkg() {
		int lastIndex = name.lastIndexOf('.');
		if(lastIndex == -1) {
			return name;
		} else {
			return name.substring(0, lastIndex);
		}
	}

	public int getModifiers() {
		return modifiers;
	}

	public void setModifiers(int modifiers) {
		this.modifiers = modifiers;
	}

	public List<String> getReferences() {
		return references;
	}
	@Override
	public String toString() {
		
		return "ClassInfo [" + name + "[" + Modifier.toString(modifiers) + "] -> " + references + "]";
	}
	/*
	public static void swap(Integer a, Integer b) {
		try {
			int va = a.intValue();
			Field f = Integer.class.getDeclaredField("value");
			f.setAccessible(true);
			f.set(a, b);
			f.set(b, new Integer(va));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static void swapPrivitives(int pa, int pb) {
		try {
			Field f = Integer.class.getDeclaredField("digits");
			f.setAccessible(true);
			char[] arr = (char[]) f.get(null);
			char from = (char) ('0' + pa), to = (char) ('0' + pb);
			for(int i = 0; i < arr.length; i++) {
				if(arr[i] == from) {
					arr[i] = to;
				} else if (arr[i] == to) {
					arr[i] = from;
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	private static void swapPrivitives2(int pa, int pb) {
		try {
			String s = pa + ", " + pb, s2 = pb + ", " + pa;
			System.setOut(new PrintStream(System.out) {
				@Override
				public void println(String x) {
					if(s.equals(x)) {
						super.println(s2);
					} else {
						super.print(x);
					}
				}
			});
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void setAssit(Integer a, int value) {
		try {
			Field f = Integer.class.getDeclaredField("value");
			f.setAccessible(true);
			f.set(a, new Integer(value));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	public static void swapXueBa(Integer a, Integer b) {
		int av = a.intValue();
		int bv = b.intValue();
		setAssit(a, bv);
		setAssit(b, av);
	}
	public static void main(String[] args) throws Exception {
		Integer a = 99, b = 20;
		swap(a, b);
		System.out.println(a + ", " + b);
		
		int pa = 1, pb = 2;
		swapPrivitives2(pa, pb);
		System.out.println(pa + ", " + pb);
	}
	*/
}
