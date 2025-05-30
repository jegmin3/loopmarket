package com.loopmarket.domain.admin.dashboard;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.loopmarket.common.controller.BaseController;


@RequestMapping("/admin")
@Controller
public class DashboardController  extends BaseController{
	
	private String renderAdminPage(HttpServletRequest request, Model model, String viewName) {
	    if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
	        return viewName + " :: content";
	    } else {
	        return render(viewName, model);
	    }
	}
	
	
	
	@GetMapping("/dashboard")
	public String dashboardAdmin(HttpServletRequest request, Model model) {
	    return renderAdminPage(request, model, "admin/dashboard");
	}

}
