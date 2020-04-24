package com.MySpringboot.tmall.dao;

import java.util.List;

import com.MySpringboot.tmall.pojo.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import com.MySpringboot.tmall.pojo.Review;

public interface ReviewDAO extends JpaRepository<Review,Integer>{

    List<Review> findByProductOrderByIdDesc(Product product);
    int countByProduct(Product product);

}