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
}
