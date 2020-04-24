package com.MySpringboot.tmall.dao;

import java.util.List;

import com.MySpringboot.tmall.pojo.User;
import org.springframework.data.jpa.repository.JpaRepository;

import com.MySpringboot.tmall.pojo.Order;

public interface OrderDAO extends JpaRepository<Order,Integer>{
    public List<Order> findByUserAndStatusNotOrderByIdDesc(User user, String status);
}