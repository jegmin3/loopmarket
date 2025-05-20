package com.loopmarket.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.loopmarket.BaseController;

@RequestMapping("/admin")
@Controller
public class AdminController extends BaseController {

	@GetMapping
    public String adminHome(Model model) {
        return render("admin/admin",model);
    }
	
	@GetMapping("product")
    public String productAdmin() {
        return "admin/product_admin";
    }
	
	@GetMapping("user")
    public String userAdmin() {
        return "admin/user_admin";
    }
	
	@GetMapping("ads")
    public String adsAdmin() {
        return "admin/ads_admin";
    }
	
	@GetMapping("dashboard")
    public String dashboardAdmin() {
        return "admin/dashboard";
    }
	
	@GetMapping("notice")
    public String noticeAdmin() {
        return "admin/notice_admin";
    }
	
	@GetMapping("notifications")
    public String notificationAdmin() {
        return "admin/notifications_admin";
    }
	
	@GetMapping("report")
    public String reportAdmin() {
        return "admin/report_admin";
    }
	
	
	
}
