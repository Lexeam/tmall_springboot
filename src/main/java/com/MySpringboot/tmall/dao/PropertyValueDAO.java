package com.MySpringboot.tmall.dao;

import java.util.List;

import com.MySpringboot.tmall.pojo.Product;
import com.MySpringboot.tmall.pojo.PropertyValue;
import org.springframework.data.jpa.repository.JpaRepository;

import com.MySpringboot.tmall.pojo.Property;

public interface PropertyValueDAO extends JpaRepository<PropertyValue,Integer>{
    List<PropertyValue> findByProductOrderByIdDesc(Product product);
    //产品 + 属性 确定具体属性值
    PropertyValue getByPropertyAndProduct(Property property, Product product);
}