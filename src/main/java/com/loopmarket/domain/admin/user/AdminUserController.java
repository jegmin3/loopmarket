package com.loopmarket.domain.admin.user;

import com.loopmarket.domain.member.MemberEntity;
import com.loopmarket.domain.member.MemberRepository;
import com.loopmarket.common.controller.BaseController;
import com.loopmarket.domain.admin.user.UserStatusEntity;
import com.loopmarket.domain.admin.user.UserStatusRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/admin/user")
public class AdminUserController extends BaseController{

    private final MemberRepository memberRepository;

    private final UserStatusRepository userStatusRepository;

    public AdminUserController(MemberRepository memberRepository, UserStatusRepository userStatusRepository) {
		this.memberRepository = memberRepository;
		this.userStatusRepository = userStatusRepository;
	}
    
    private String renderAdminPage(HttpServletRequest request, Model model, String viewName) {
	    if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
	        return viewName + " :: content";
	    } else {
	        return render(viewName, model);
	    }
	}

//	@GetMapping("")
//	public String userAdmin(HttpServletRequest request, Model model) {
//	    return renderAdminPage(request, model, "admin/user_admin");
//	}
    
    // 사용자 목록 조회
    @GetMapping
    public String listUsers(HttpServletRequest request, Model model) {
        List<MemberEntity> members = memberRepository.findAll();
        model.addAttribute("members", members);

        // 각 사용자별 상태도 같이 넘겨줘야 하므로 Map 구성
        Map<Integer, String> userStatusMap = new HashMap<>();
        for (MemberEntity member : members) {
            userStatusRepository.findById(member.getUserId()).ifPresent(status ->
            userStatusMap.put(member.getUserId(), status.getAccountStatus().name())
            );
        }
        model.addAttribute("userStatusMap", userStatusMap);
        
        return renderAdminPage(request, model, "admin/user_admin");
    }
    
	//상태 변경 처리 (예: 활성화 <-> 정지)
    @PostMapping("/changeStatus")
    public String changeUserStatus(@RequestParam("userId") int userId,
                                   @RequestParam("newStatus") String newStatus) {

        // 기존 상태 조회
        UserStatusEntity status = userStatusRepository.findById(userId).orElse(null);

        if (status == null) {
            // 없으면 MemberEntity 조회하여 새로 생성
            MemberEntity member = memberRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

            status = UserStatusEntity.builder()
                    .member(member)
                    .accountStatus(UserStatusEntity.AccountStatus.valueOf(newStatus))
                    .build();
        } else {
            // DELETED가 아닌 경우에만 상태 업데이트
            if (!status.getAccountStatus().equals(UserStatusEntity.AccountStatus.DELETED)) {
                status.setAccountStatus(UserStatusEntity.AccountStatus.valueOf(newStatus));
            }
        }

        userStatusRepository.save(status);
        return "redirect:/admin/user";
        //return "admin/user_admin :: content";
    }
}
