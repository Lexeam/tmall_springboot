package com.MySpringboot.tmall.dao;

import java.util.List;

import com.MySpringboot.tmall.pojo.Product;
import com.MySpringboot.tmall.pojo.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageDAO extends JpaRepository<ProductImage,Integer>{
    public List<ProductImage> findByProductAndTypeOrderByIdDesc(Product product, String type);
}