package net.flyingff.web.dpic.config;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.google.gson.Gson;

@EnableWebMvc
@Configuration
@ComponentScan("net.flyingff.web.dpic.ctrl")
public class WebConfig extends WebMvcConfigurerAdapter {
	private static final Logger LOG = LoggerFactory.getLogger("UncaughtException");
	
	@Bean
	public ViewResolver jsonViewResolver(Gson g) {
		JsonViewResolver resolver = new JsonViewResolver(g);
		resolver.setOrder(1);
		return resolver;
	}
	@Bean
	public ViewResolver binaryViewResolver() {
		BinaryViewResolver resolver = new BinaryViewResolver();
		resolver.setOrder(2);
		return resolver;
	}
	@Bean
	public MultipartResolver multipartResolver(ServletContext context){
		return new StandardServletMultipartResolver();
	}
	@Bean
	public HandlerExceptionResolver exceptionHandler() {
		return new HandlerExceptionResolver() {
			@Override
			public ModelAndView resolveException(HttpServletRequest request, 
					HttpServletResponse response, Object handler,
					Exception ex) {
				ModelAndView mv = new ModelAndView();
				int code = 2;
				if(ex instanceof IllegalArgumentException) {
					code = 1;
				} else {
					LOG.warn("UE", ex);
				}
				mv.getModel().put("err", code);
				mv.setViewName(JsonViewResolver.VIEW_NAME);
				return mv;
			}
		};
	}
}
