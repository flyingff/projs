package net.flyingff.web.dpic.config;

import java.io.OutputStream;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

/**
 * 一个返回JSON的视图解析器
 * @author mg
 */
public class BinaryViewResolver implements ViewResolver, Ordered {
	public static final String VIEW_NAME = "binary",
			MODEL_KEY_DATA = "data",
			MODEL_KEY_ATTACHMENT_NAME = "name";
	private BinaryView view;
	private int order;
	@Override
	public int getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}
	public BinaryViewResolver() {
		view = new BinaryView();
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
	
	static class BinaryView implements View {
		@Override
		public String getContentType() {
			return "application/octet-stream";
		}

		@Override
		public void render(Map<String, ?> model, HttpServletRequest request,
		        HttpServletResponse response) throws Exception {
			String attachmentName = (String) Objects.requireNonNull(model.get(MODEL_KEY_ATTACHMENT_NAME));
			byte[] data = (byte[]) Objects.requireNonNull(model.get(MODEL_KEY_DATA));
			
			response.setContentType(getContentType());
			response.setContentLength(data.length);
			response.setHeader("Content-Disposition", "attachment;filename="+attachmentName);
			response.setCharacterEncoding("utf-8");
			
			try(OutputStream os = response.getOutputStream()) {
				os.write(data);
			}
		}
	}
}