package net.flyingff.douyu.barrage.resolver;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class AbstractBarrageListener {
	private Map<String, TypeHandler> handlerMap = new HashMap<>();
	public AbstractBarrageListener() {
		for(Method m : getClass().getDeclaredMethods()) {
			Type t = m.getAnnotation(Type.class);
			if(t == null) {
				continue;
			}
			String typeName = t.value();
			if(handlerMap.containsKey(typeName)) {
				throw new AssertionError("Duplicate handler for type " + typeName);
			}
			
			TypeHandler th = new TypeHandler();
			
			Parameter[] pArr = m.getParameters();
			th.parsers = new Parser[pArr.length];
			
			for(int i = 0; i < pArr.length; i++) {
				Parameter par = pArr[i];
				Param aPa = par.getAnnotation(Param.class);
				
				String key = aPa == null ? par.getName() : aPa.value();
				Class<?> type = par.getType();
				if(type.isAssignableFrom(Map.class)) {
					th.parsers[i] = it->it;
				} else if (type.isAssignableFrom(String.class)) {
					th.parsers[i] = it->it.get(key);
				} else if (type.isAssignableFrom(int.class)) {
					th.parsers[i] = it->Integer.parseInt(it.get(key));
				} else {
					throw new RuntimeException("Not supported type:" + type);
				}
			}
			
			
			th.args = new Object[pArr.length];
			th.handler = packet ->{
				try {
					for(int i = 0; i < th.args.length; i++) {
						th.args[i] = th.parsers[i].parse(packet);
					}
					m.invoke(this, th.args);
				} catch (Exception e) {
					System.err.println("Error when parsing message, type: " + typeName);
					e.printStackTrace();
				}
			};
			m.setAccessible(true);
			handlerMap.put(typeName, th);
		}
		
	}
	public void handle(Map<String, String> packet) {
		TypeHandler hd = handlerMap.get(packet.get("type"));
		if(hd == null) {
			onDefault(packet);
		} else {
			hd.handler.accept(packet);
		}
	}
	protected void onDefault(Map<String, String> packet) {
		System.out.println("Message: " + packet);
	}
}

class TypeHandler {
	public String name;
	public Object[] args;
	public Parser[] parsers;
	public Consumer<Map<String, String>> handler;
}
interface Parser {
	Object parse(Map<String, String> packet);
}