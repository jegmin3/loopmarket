package com.loopmarket.domain.product.service;

import com.loopmarket.domain.product.entity.ProductEntity;
import com.loopmarket.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

  private final ProductRepository productRepository;

  public void registerProduct(ProductEntity product) {
    product.setCreatedAt(LocalDateTime.now());
    product.setUpdateAt(LocalDateTime.now());
    product.setStatus("판매중");
    product.setIsHidden(true);

    productRepository.save(product);  //
  }
  public List<ProductEntity> getAllProducts() {
    return productRepository.findAll();
  }
  
  //결제 페이지에 필요하여 추가했습니다
  public ProductEntity getProductById(Long id) {
	    return productRepository.findById(id)
	        .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));
	}
}

