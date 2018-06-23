package net.flyingff.web.dpic.config;

import java.io.Writer;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

import com.google.gson.Gson;

/**
 * 一个返回JSON的视图解析器
 * @author mg
 */
public class JsonViewResolver implements ViewResolver, Ordered {
	public static final String VIEW_NAME = "json";
	private JsonView view;
	private int order = 0;
	@Override
	public int getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}
	public JsonViewResolver(Gson g) {
		view = new JsonView(g);
	}
	
	@Override
	public View resolveViewName(String viewName, Locale locale)
	        throws Exception {
		if(VIEW_NAME.equalsIgnoreCase(viewName)) {
			return view;
		} else {
			return null;
		}
	}
	
	/***
	 * Json视图的具体实现
	 * @author mg
	 */
	static class JsonView implements View {
		private Gson g;
		public JsonView(Gson g) {
			this.g = g;
		}
		@Override
		public String getContentType() {
			return "application/json";
		}

		@Override
		public void render(Map<String, ?> model, HttpServletRequest request,
		        HttpServletResponse response) throws Exception {
			// filter for some special properties
			model.keySet().removeIf(x->x.startsWith("org.springframework"));
			
			response.setContentType(getContentType());
			response.setCharacterEncoding("utf-8");
			try(Writer wr = response.getWriter()) {
				String jsonStr = g.toJson(model);
				wr.write(jsonStr);
			}
		}
	}
}