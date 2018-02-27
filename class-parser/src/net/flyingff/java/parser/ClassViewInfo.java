package net.flyingff.java.parser;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ClassViewInfo {
	public static final int WIDTH = 64, HEIGHT = 32;
	private final String name, simpleName;
	private final boolean group;
	private final List<String> contentClass, dependencies;
	private final Rectangle rect = new Rectangle(0, 0, WIDTH, HEIGHT);
	
	private static final String getSimpleName(String name) {
		return name.substring(name.lastIndexOf('/') + 1);
	}
	public ClassViewInfo(String name, Collection<String> dependencies) {
		this.name = name;
		this.simpleName = getSimpleName(name);
		this.group = false;
		this.dependencies = new ArrayList<>(dependencies);
		contentClass = Collections.emptyList();
	}
	
	public ClassViewInfo(String name, Collection<String> content, Collection<String> dependencies) {
		this.name = name;
		this.simpleName = getSimpleName(name);
		this.group = true;
		this.dependencies = new ArrayList<>(dependencies);
		this.contentClass = new ArrayList<>(content);
	}

	public String getName() {
		return name;
	}
	public String getSimpleName() {
		return simpleName;
	}

	public boolean isGroup() {
		return group;
	}

	public List<String> getContentClass() {
		return contentClass;
	}

	public List<String> getDependencies() {
		return dependencies;
	}
	public Rectangle getRect() {
		return rect;
	}
	@Override
	public String toString() {
		return name;
	}
}
