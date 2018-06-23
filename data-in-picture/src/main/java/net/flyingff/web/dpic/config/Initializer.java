package net.flyingff.web.dpic.config;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration.Dynamic;

import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class Initializer extends AbstractAnnotationConfigDispatcherServletInitializer {
	@Override
	protected Class<?>[] getRootConfigClasses() {
		return new Class[]{RootConfig.class};
	}
	@Override
	protected Class<?>[] getServletConfigClasses() {
		return new Class[]{WebConfig.class};
	}
	@Override
	protected String[] getServletMappings() {
		return new String[]{"/api/*"};
	}
	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		super.onStartup(servletContext);
		CharacterEncodingFilter encFilter = new CharacterEncodingFilter();
		encFilter.setEncoding("utf-8");
		encFilter.setForceEncoding(true);
		servletContext.addFilter("characterFilter", encFilter).addMappingForUrlPatterns(null, false, "/*");
	}
	@Override
	protected void customizeRegistration(Dynamic registration) {
		registration.setMultipartConfig(new MultipartConfigElement(""));
	}
}
