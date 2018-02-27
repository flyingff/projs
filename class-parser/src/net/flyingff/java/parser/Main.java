package net.flyingff.java.parser;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

public class Main {
	private static final String GROUP = "!GROUP";
	
	public static void main(String[] args) throws Exception{
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		JFileChooser fc = new JFileChooser("D:\\git\\teeth\\teeth.main\\bin");
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if(fc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File f = fc.getSelectedFile();
		if(!f.isDirectory()) {
			JOptionPane.showMessageDialog(null, "Please choose a folder!");
			return;
		}
		
		List<ClassInfo> classes = ClassParser.parseFileTree(f.getAbsolutePath());
		
		List<String> nameList = classes.stream().map(ClassInfo::getName).collect(Collectors.toList());
		Map<String, ClassInfo> nameMap = new HashMap<>();
		Map<String, Set<String>> dependencyMap = new HashMap<>();
		classes.forEach(it->{
			String name = it.getName();
			nameMap.put(name, it);
			Set<String> deps = dependencyMap.get(name);
			
			if(deps == null) {
				deps = new HashSet<>();
				dependencyMap.put(name, deps);
			}
			deps.addAll(it.getReferences());
			// remove self-reference
			deps.remove(name);
			
			it.getReferences().forEach(refClass->{
				if(!dependencyMap.containsKey(refClass)) {
					dependencyMap.put(refClass, new HashSet<>());
				}
			});
		});
		
		List<Set<String>> groups = new ArrayList<>();
		Set<String> circle;
		try {
			while((circle = findCircle(nameList, dependencyMap)) != null) {
				formGroup(circle, groups, nameList, dependencyMap);
			}
		} catch(Exception e) {
			System.out.println("Groups:");
			groups.forEach(System.out::println);
			
			System.out.println("Dependency map:");
			dependencyMap.forEach((k, v)->{
				System.out.println(k + "->" + v);
			});
			
			e.printStackTrace(System.out);
			System.exit(-1);
		}
		//System.out.println("Groups:");
		//groups.forEach(System.out::println);
		
		//System.out.println("Dependency map:");
		//dependencyMap.forEach((k, v)->{
		//	System.out.println(k + "->" + v);
		//});
		
		
		List<String> baseClasses = new ArrayList<>();
		dependencyMap.forEach((name, dep)->{
			if(dep == null || dep.isEmpty()) {
				baseClasses.add(name);
			}
		});
		Map<String, Integer> destMap = new HashMap<>();
		Set<String> toSort = new HashSet<>(nameList);
		baseClasses.forEach(it->{ destMap.put(it, 0); toSort.remove(it); });
		int round = 0;
		while(!toSort.isEmpty()) {
			round++;
			final int thisRound = round;
			new HashSet<>(toSort).stream().forEach(it->{
				int maxVal = dependencyMap.get(it)
					.stream()
					.mapToInt(x-> {
						Integer v = destMap.get(x);
						return v == null ? Integer.MAX_VALUE : v + 1;
					})
					.max()
					.getAsInt();
				if(maxVal == thisRound) {
					destMap.put(it, thisRound);
					toSort.remove(it);
				}
			});
		}
		List<List<ClassViewInfo>> infos = new ArrayList<>();
		for(int i = 0; i <= round; i++) {
			infos.add(new ArrayList<>());
		}
		
		//System.out.println("Base classes:");
		//baseClasses.forEach(it->System.out.println("\t" + it));
		
		//List<String> output = new ArrayList<>();
		destMap.forEach((k, v)->{
			ClassViewInfo info;
			if(k.startsWith(GROUP)) {
				info = new ClassViewInfo(k, groups.get(Integer.parseInt(k.substring(GROUP.length()))), dependencyMap.get(k));
			} else {
				info = new ClassViewInfo(k, dependencyMap.get(k));
			}
			infos.get(v).add(info);
		});
		//output.sort(null);
		
		//output.forEach(System.out::println);
		new ViewUI(infos);
	}
	
	private static Set<String> findCircle(Collection<String> elements, Map<String, Set<String>> connections) {
		Set<String> expanded = new HashSet<>(), toExpand = new HashSet<>(elements);
		Map<String, String> foundPath = new HashMap<>();
		Deque<DFSStackUnit> stack = new ArrayDeque<>();
		
		while(!toExpand.isEmpty()) {
			// starting element
			String start = toExpand.iterator().next();
			stack.push(new DFSStackUnit(start, connections, toExpand));
			// DFS
			while(!stack.isEmpty()) {
				DFSStackUnit top = stack.peek();
				expanded.add(top.value());
				// expand top
				String next = top.next();
				if(next == null) {
					// pop, no more children
					stack.pop(); continue;
				}
				
				// find element in stack?
				if(stack.stream()
						.filter(x->next.equals(x.value()))
						.findAny().isPresent()) {
					// found reverse edge, top.value -> next 
					List<String> path = new ArrayList<>();
					String p = top.value();
					while(!p.equals(next)) {
						path.add(p);
						String before = p;
						p = foundPath.get(p);
						if(p == null) {
							System.err.println("start=" + start);
							System.err.println("from=" + top.value());
							System.err.println("to=" + next);
							System.err.println("path=" + path);
							System.err.println("stack=" + stack);
							System.err.println(before + " prev is null");
						}
					}
					path.add(next);
					return new HashSet<>(path);
				} else if(toExpand.contains(next)) {
					foundPath.put(next, top.value());
					stack.push(new DFSStackUnit(next, connections, toExpand));
				} else throw new AssertionError();
			}
			// new round of DFS, remove all expanded elements
			toExpand.removeAll(expanded);
			expanded.clear();
		}
		
		return null;
	}
	
	private static final void formGroup(Collection<String> groupMembers, List<Set<String>> groups,
			Collection<String> allElements, Map<String, Set<String>> connections) {
		List<String> groupsInGroup = groupMembers.stream()
				.filter(x->x.startsWith(GROUP))
				.collect(Collectors.toList());
		String destGroupName; 
		Set<String> destGroup, toBeReplaced = new HashSet<>(groupMembers);
		
		if(groupsInGroup.isEmpty()) {
			// simple form a new group
			destGroupName = GROUP + groups.size();
			groups.add(destGroup = new HashSet<>(groupMembers));
		} else {
			// combination groups
			destGroupName = groupsInGroup.get(0);
			destGroup = groups.get(Integer.parseInt(destGroupName.substring(GROUP.length())));
			destGroup.addAll(groupMembers);
			destGroup.removeAll(groupsInGroup);
			toBeReplaced.remove(destGroupName);
		}
		
		// do replacement
		allElements.removeAll(toBeReplaced);
		allElements.add(destGroupName);
		
		Set<String> groupDependency = toBeReplaced.stream()
			.flatMap(name->connections.get(name).stream())
			.collect(Collectors.toSet());
		connections.put(destGroupName, groupDependency);
		
		connections.keySet().removeAll(toBeReplaced);
		connections.values().forEach(it->{
			if(it.removeAll(toBeReplaced)) {
				it.add(destGroupName);
			}
		});
		groupDependency.remove(destGroupName);
		
	}
}

class DFSStackUnit {
	private final Iterator<String> iterator;
	private final String value;
	private DFSStackUnit(String val, Set<String> children) {
		iterator = children.iterator();
		value = Objects.requireNonNull(val);
	}
	public DFSStackUnit(String val, Map<String, Set<String>> connections, Set<String> toExpand) {
		this(val, intersection(connections.get(val), toExpand));
	}
	private static final Set<String> intersection(Set<String> a, Set<String> b) {
		Set<String> x = new HashSet<>(a);
		x.retainAll(b);
		return x;
	}
	public String next() {
		return iterator.hasNext() ? iterator.next() : null;
	}
	public String value() {
		return  value;
	}
}