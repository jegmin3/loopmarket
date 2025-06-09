package com.loopmarket.domain.admin.alram;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.loopmarket.domain.alram.AlramDTO;

@Controller
@RequestMapping("/admin/alram")
public class AdminAlramController {
	
	@Autowired(required = false)
	private AdminAlramService alramS;
	
	/**
	 * 알림 전송
	 */
	//@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/send")
	public ResponseEntity<?> sendAdminAlram(@RequestBody AlramRequestDTO req) {
	    if (req.getUserId() != null) {
	        // 단일 사용자 알림
	        alramS.sendAdminAlramToUser(req);
	    } else {
	        // 전체 사용자 알림
	        alramS.sendAdminAlramToAll(req);
	    }
	    return ResponseEntity.ok().build();
	}
	
	@GetMapping("/list")
	@ResponseBody
	public List<AlramDTO> getAdminAlrams() {
	    return alramS.getRecentAdminAlrams(); // type = "ADMIN"만 조회
	}

}
