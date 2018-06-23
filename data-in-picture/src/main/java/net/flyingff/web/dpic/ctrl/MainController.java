package net.flyingff.web.dpic.ctrl;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import net.flyingff.web.dpic.config.JsonViewResolver;

@Controller
public class MainController {
	@RequestMapping("test")
	public String test(Model m) {
		m.asMap().put("test", "val");
		return JsonViewResolver.VIEW_NAME;
	}
}
