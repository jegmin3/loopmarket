package com.loopmarket.domain.admin.products;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.loopmarket.domain.product.dto.ProductAdminListDTO;
import com.loopmarket.domain.product.entity.ProductEntity;
import com.loopmarket.domain.product.service.ProductService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/api/products")
public class AdminProductRestController {

    private final ProductService productService;

    @GetMapping
    public List<ProductEntity> getAllProductsForAdmin() {
        return productService.getAllProducts(); // 숨김 포함 전체
    }

    @PostMapping("/{id}/hide")
    public void hideProduct(@PathVariable Long id) {
        productService.updateHiddenStatus(id, true);
    }

    @DeleteMapping("/{id}")
    public void deleteProductByAdmin(@PathVariable Long id) {
        productService.deleteProductById(id);
    }
    
    @PostMapping("/{id}/unhide")
    public void unhideProduct(@PathVariable Long id) {
        productService.updateHiddenStatus(id, false);
    }
    
    @GetMapping("/paged")
    public Page<ProductAdminListDTO> getPagedProducts(@RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductEntity> pageData = productService.getProductsPage(pageable);

        System.out.println("💡 총 상품 수: " + pageData.getTotalElements()); // 로그 찍기

        return pageData.map(product -> {
            System.out.println("✅ Mapping: " + product.getTitle()); // 각 상품 제목 출력
            return new ProductAdminListDTO(
                product.getProductId(),
                product.getTitle(),
                product.getPrice(),
                product.getCtgCode(),
                product.getSaleType(),
                product.getCondition(),
                product.getStatus(),
                product.getIsHidden()
            );
        });
    }
}
