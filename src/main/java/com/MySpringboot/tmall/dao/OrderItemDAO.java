package com.MySpringboot.tmall.dao;

import java.util.List;

import com.MySpringboot.tmall.pojo.Order;
import com.MySpringboot.tmall.pojo.OrderItem;
import com.MySpringboot.tmall.pojo.Product;
import com.MySpringboot.tmall.pojo.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemDAO extends JpaRepository<OrderItem,Integer>{
    List<OrderItem> findByOrderOrderByIdDesc(Order order);
    List<OrderItem> findByProduct(Product product);
    List<OrderItem> findByUserAndOrderIsNull(User user); //查找所有被加入到购物车内的订单项（即还没有属于任何一个订单的），在结算页面清算数量时候使用
}
