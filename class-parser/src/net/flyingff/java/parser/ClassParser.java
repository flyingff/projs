package net.flyingff.java.parser;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class ClassParser {
	private static final ThreadLocal<ClassParser> CPS = new ThreadLocal<ClassParser>() {
		protected ClassParser initialValue() { return new ClassParser(); }
	};
	
	private List<byte[]> classFiles = new ArrayList<>();
	public static List<ClassInfo> parseFileTree(String string) {
		return parseFileTree(new File(string));
	}
	public static List<ClassInfo> parseFileTree(File folder) {
		ClassParser parser = CPS.get();
		
		workTree(folder, file->{
			if(!file.getName().toLowerCase().endsWith(".class")) {
				return;
			}
			parser.addFile(file);
		});
		return parser.parse();
	}
	public void addFile(File f) {
		int len = (int)f.length();
		byte[] data = new byte[len];
		try(DataInputStream dis = new DataInputStream(new FileInputStream(f))) {
			dis.readFully(data);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		addClass(data);
	}
	public void addClass(byte[] data) {
		classFiles.add(data);
	}
	public void clear() {
		classFiles.clear();
	}
	public List<ClassInfo> parse() {
		List<byte[]> data;
		synchronized (this) {
			data = new ArrayList<>(classFiles);
		}
		// Map<String, Set<String>> referenceMap = new HashMap<>();
		
		List<ClassInfo> classes = new ArrayList<>();
		for(byte[] single : data) {
			classes.add(parseSingle(single));
		}
		return classes;
	}
	private ClassInfo parseSingle(byte[] data) {
		ClassInfo info = new ClassInfo();
		ByteBuffer buffer = ByteBuffer.wrap(data);
		buffer.position(0);
		buffer.limit(data.length);
		buffer.order(ByteOrder.BIG_ENDIAN);
		
		int magic = buffer.getInt();
		if(magic != 0xCAFEBABE) {
			throw new RuntimeException("Magic number is wrong: " + Integer.toHexString(magic));
		}
		
		short minorVersion = buffer.getShort(), majorVersion = buffer.getShort();
		if(System.currentTimeMillis() == 12345) {
			System.out.printf("Class file in version %d.%d\n", majorVersion, minorVersion);
		}
		int constantCnt = (buffer.getShort() & 0xFFFF) - 1;
		Map<Integer, String> u8Constants = new HashMap<>();
		Map<Integer, Object> digits = new HashMap<>();
		Map<Integer, Integer> classInfos = new HashMap<>();
		IntConsumer refCheck = index->{
			if(index > constantCnt) throw new RuntimeException("Malformed reference: " + index + ", but max = " + constantCnt);
		};
		
		int[] tags = new int[constantCnt + 1];
		for(int i = 1; i <= constantCnt; i++) {
			int tag = buffer.get() & 0xFF;
			tags[i] = tag;
			switch(tag) {
			case 7: {
				// class ref 
				int ref = buffer.getShort() & 0xFFFF;
				refCheck.accept(ref);
				classInfos.put(ref, i);
				break;
			}
			case 9:
				// Field ref Info
				// u2 class index
				// u2 name_and type index
			case 10:
				// method ref info
				// u2 class index
				// u2 name and type index
			case 11: {
				// interface method ref info
				// u2 class index
				// u2 name and type index
				int classRef = buffer.getShort() & 0xFFFF;
				int nameAndIndex = buffer.getShort() & 0xFFFF;
				refCheck.accept(classRef);
				refCheck.accept(nameAndIndex);
				break;
			}
			case 8: {
				// string ref
				int ref = buffer.getShort() & 0xFFFF;
				refCheck.accept(ref);
				break;
			}
			case 3: 
				// integer
				digits.put(i, buffer.getInt());
				break;
			case 4: 
				// float
				digits.put(i, buffer.getFloat());
				break;
			case 5:
				// long
				digits.put(i, buffer.getLong());
				i++;
				break;
			case 6:
				// double
				digits.put(i, buffer.getDouble());
				i++;
				break;
			case 12:
				// name and type
				// u2 name index		(utf8)
				// u2 descriptor_index	(utf8)
				refCheck.accept(buffer.getShort() & 0xFFFF);
				refCheck.accept(buffer.getShort() & 0xFFFF);
				break;
			case 1: {
				// utf8
				int len = buffer.getShort() & 0xFFFF;
				byte[] chs = new byte[len];
				buffer.get(chs);
				u8Constants.put(i, new String(chs));
				break;
			} 
			case 15:
				// method handle
				// u1 ref kind
				// u2 ref index
				if((buffer.get() & 0xFF) > 9) {
					throw new RuntimeException("Wrong ref kind");
				}
				refCheck.accept(buffer.getShort() & 0xFFFF);
				break;
			case 16:
				// method type
				refCheck.accept(buffer.getShort() & 0xFFFF);
				break;
			case 18: {
				// invoke dynamic
				// u2 bootstrap method attribute index
				// u2 name and type index
				int methodRef = buffer.getShort() & 0xFFFF;
				refCheck.accept(buffer.getShort() & 0xFFFF);
				Objects.requireNonNull(methodRef);
				
				break;
			}
				default:
					throw new RuntimeException("Unknown tag: " + tag);
			}
		}
		int accessFlag = buffer.getShort() & 0xFFFF;
		int thisClassRef = buffer.getShort() & 0xFFFF;
		
		
		info.setModifiers(accessFlag);
		Map<Integer, String> classInfoEntries = new HashMap<>();
		classInfos.keySet().stream()
			.map(it->{
				String ret = u8Constants.get(it);
				if(ret == null) {
					return "[Tag=" + tags[it] + "]";
				} else {
					classInfoEntries.put(classInfos.get(it), ret);
					return ret;
				}
			})
			.forEach(info.getReferences()::add);
		info.setName(Objects.requireNonNull(classInfoEntries.get(thisClassRef)));
		return info;
	}
	
	public static void workTree(File folder, Consumer<File> onFile) {
		for(File child : folder.listFiles()) {
			if(child.isDirectory()) {
				workTree(child, onFile);
			} else {
				onFile.accept(child);
			}
		}
	}
}
