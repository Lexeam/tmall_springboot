package com.MySpringboot.tmall.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="category")
@JsonIgnoreProperties({ "handler","hibernateLazyInitializer" })
//前后端分离，前后端交互采用 json ，这个对象会被转化成 json 数据
//jpa 默认会使用 hibernate, 在 jpa 工作过程中，就会创造代理类来继承 Category
//并添加 handler 和 hibernateLazyInitializer 这两个无须 json 化的属性
//所以这里需要用 JsonIgnoreProperties 把这两个属性忽略掉。
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    int id;

    String name;

    @Transient
    List<Product> products;
    @Transient
    List<List<Product>> productsByRow;

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public List<Product> getProducts() {
        return products;
    }
    public void setProducts(List<Product> products) {
        this.products = products;
    }
    public List<List<Product>> getProductsByRow() {
        return productsByRow;
    }
    public void setProductsByRow(List<List<Product>> productsByRow) {
        this.productsByRow = productsByRow;
    }
    @Override
    public String toString() {
        return "Category [id=" + id + ", name=" + name + "]";
    }
}
