package com.loopmarket.domain.admin;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.loopmarket.common.controller.BaseController;

@RequestMapping("/admin")
@Controller
public class AdminController extends BaseController {
	
	private String renderAdminPage(HttpServletRequest request, Model model, String viewName) {
	    if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
	        return viewName + " :: content";
	    } else {
	        return render(viewName, model);
	    }
	}

	@GetMapping
    public String adminHome(Model model) {
        return render("admin/admin",model);
    }
	
//	@GetMapping("product")
//	public String productAdmin(HttpServletRequest request, Model model) {
//	    return renderAdminPage(request, model, "admin/product_admin");
//	}
	
//	@GetMapping("user")
//	public String userAdmin(HttpServletRequest request, Model model) {
//	    return renderAdminPage(request, model, "admin/user_admin");
//	}
	
//	@GetMapping("ads")
//	public String adsAdmin(HttpServletRequest request, Model model) {
//	    return renderAdminPage(request, model, "admin/ads_admin");
//	}
	
//	@GetMapping("dashboard")
//	public String dashboardAdmin(HttpServletRequest request, Model model) {
//	    return renderAdminPage(request, model, "admin/dashboard");
//	}
	
//	@GetMapping("notice")
//	public String noticeAdmin(HttpServletRequest request, Model model) {
//	    return renderAdminPage(request, model, "admin/notice_admin");
//	}
	

	/** 관리자용 알림페이지로 이동 */
  @GetMapping("/categories")
	public String categoryAdminPage(HttpServletRequest request, Model model) {
	    return renderAdminPage(request, model, "admin/category_admin");
	}
	

	@GetMapping("notifications")
	public String notificationAdmin(HttpServletRequest request, Model model) {
	    return renderAdminPage(request, model, "admin/admin_alram");
	}
	
	@GetMapping("report")
	public String reportAdmin(HttpServletRequest request, Model model) {
	    return renderAdminPage(request, model, "admin/report_admin");
	}
	
}
