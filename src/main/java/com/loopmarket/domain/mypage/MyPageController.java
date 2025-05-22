package com.loopmarket.domain.mypage;

import com.loopmarket.domain.product.entity.ProductEntity;
import com.loopmarket.domain.product.repository.ProductRepository;
import com.loopmarket.common.controller.BaseController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/mypage")
public class MyPageController extends BaseController {

    private final ProductRepository productRepository;

    @Autowired
    public MyPageController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    private String renderMypage(HttpServletRequest request, Model model, String viewName) {
        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            return viewName + " :: content";
        } else {
            return render(viewName, model);
        }
    }

    // 마이페이지 기본 진입
    @GetMapping
    public String myPageHome(HttpServletRequest request, Model model) {
        return renderMypage(request, model, "mypage/mypage_main");
    }

    // 내가 등록한 상품 목록
    @GetMapping("/selling")
    public String mySellingItems(HttpServletRequest request, Model model, Principal principal) {
        String username = principal.getName(); // 로그인 사용자 ID
        List<ProductEntity> myProducts = productRepository.findBySeller(username);
        model.addAttribute("products", myProducts);
        return renderMypage(request, model, "mypage/my_selling_items");
    }

    // 상품 삭제 (AJAX)
    @PostMapping("/selling/delete/{id}")
    @ResponseBody
    public String deleteMyProduct(@PathVariable("id") Long id, Principal principal) {
        ProductEntity product = productRepository.findById(id).orElse(null);
        if (product != null && product.getSeller().equals(principal.getName())) {
            productRepository.delete(product);
            return "success";
        }
        return "error";
    }
}