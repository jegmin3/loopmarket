package com.loopmarket.domain.admin.user;

import com.loopmarket.domain.member.MemberEntity;
import com.loopmarket.domain.member.MemberEntity.Role;
import com.loopmarket.domain.member.MemberRepository;
import com.loopmarket.domain.member.MemberService;
import com.loopmarket.common.controller.BaseController;
import com.loopmarket.domain.admin.user.UserStatusEntity;
import com.loopmarket.domain.admin.user.UserStatusRepository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
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
    
    private final MemberService memberService;
    

    public AdminUserController(MemberRepository memberRepository, UserStatusRepository userStatusRepository, MemberService memberService) {
		this.memberRepository = memberRepository;
		this.userStatusRepository = userStatusRepository;
		this.memberService = memberService;
	}
    
    private String renderAdminPage(HttpServletRequest request, Model model, String viewName) {
	    if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
	        return viewName + " :: content";
	    } else {
	        return render(viewName, model);
	    }
	}
private void addUsersToModel(Model model, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<MemberEntity> memberPage = memberRepository.findAll(pageable);

        List<MemberEntity> members = memberPage.getContent();
        model.addAttribute("members", members);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", memberPage.getTotalPages());

        Map<Integer, String> userStatusMap = new HashMap<>();
        for (MemberEntity member : members) {
            userStatusRepository.findById(member.getUserId()).ifPresent(status ->
                userStatusMap.put(member.getUserId(), status.getAccountStatus().name())
            );
        }
        model.addAttribute("userStatusMap", userStatusMap);
    }
    
	//사용자 목록 조회
    @GetMapping
    public String listUsers(HttpServletRequest request, Model model,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size) {

        addUsersToModel(model, page, size);
        return renderAdminPage(request, model, "admin/user_admin");
    }
    
    //상태 변경 처리 (예: 활성화 <-> 정지)
    @PostMapping("/changeStatus")
    @ResponseBody
    public Map<String, Object> changeUserStatus(@RequestParam("userId") int userId,
                                                @RequestParam("newStatus") String newStatus) {
        Map<String, Object> result = new HashMap<>();
        try {
            UserStatusEntity status = userStatusRepository.findById(userId).orElse(null);

            if (status == null) {
                MemberEntity member = memberRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
                status = UserStatusEntity.builder()
                    .member(member)
                    .accountStatus(UserStatusEntity.AccountStatus.valueOf(newStatus))
                    .build();
            } else {
                status.setAccountStatus(UserStatusEntity.AccountStatus.valueOf(newStatus));
            }

            userStatusRepository.save(status);

            result.put("success", true);
            result.put("message", "상태가 변경되었습니다.");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "오류 발생: " + e.getMessage());
        }

        return result;
    }
    
    // 계정 권한 변경
    @PostMapping("/changeRole")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> changeUserRole(
            @RequestParam("userId") Integer userId,
            @RequestParam("newRole") String newRoleStr) {

        Map<String, Object> response = new HashMap<>();
        try {
            Role newRole = Role.valueOf(newRoleStr);
            memberService.updateUserRole(userId, newRole);
            response.put("success", true);
            response.put("message", "권한이 성공적으로 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", "잘못된 권한 값입니다.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
        }
        return ResponseEntity.ok(response);
    }
    
    
}
