package net.flyingff.web.dpic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Configuration
@ComponentScan(value="net.flyingff.web.dpic",excludeFilters={
	@Filter(type=FilterType.REGEX,pattern="net.flyingff.web.dpic.(config|ctrl).*")
})
public class RootConfig {
	@Bean
	public Gson gson() {
		return new GsonBuilder()
				.addSerializationExclusionStrategy(new ExclusionStrategy() {
					@Override
					public boolean shouldSkipField(FieldAttributes f) {
						return f.getAnnotation(ServerOnly.class) != null;
					}
					@Override
					public boolean shouldSkipClass(Class<?> clazz) {
						return false;
					}
				})
				.create();
	}
}
