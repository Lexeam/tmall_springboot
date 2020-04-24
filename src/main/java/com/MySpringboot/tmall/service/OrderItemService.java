package com.MySpringboot.tmall.service;

import java.util.List;

import com.MySpringboot.tmall.pojo.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.MySpringboot.tmall.dao.OrderItemDAO;
import com.MySpringboot.tmall.pojo.Order;
import com.MySpringboot.tmall.pojo.OrderItem;
import com.MySpringboot.tmall.pojo.User;
import com.MySpringboot.tmall.util.SpringContextUtil;

@Service
@CacheConfig(cacheNames="orderItems")
public class OrderItemService {
    @Autowired OrderItemDAO orderItemDAO;
    @Autowired ProductImageService productImageService;

    //根据订单来取得订单对应的订单项，并存入Order里面的List<OrderItem>
    public void fill(List<Order> orders) {
        for (Order order : orders)
            fill(order);
    }
    @CacheEvict(allEntries=true)
    public void update(OrderItem orderItem) {
        orderItemDAO.save(orderItem);
    }

    public void fill(Order order) {
        OrderItemService orderItemService = SpringContextUtil.getBean(OrderItemService.class);
        List<OrderItem> orderItems = orderItemService.listByOrder(order);
        float total = 0;
        int totalNumber = 0;
        for (OrderItem oi :orderItems) {
            total+=oi.getNumber()*oi.getProduct().getPromotePrice();
            totalNumber+=oi.getNumber();
            productImageService.setFirstProdutImage(oi.getProduct());
        }
        order.setTotal(total);
        order.setOrderItems(orderItems);
        order.setTotalNumber(totalNumber);
        order.setOrderItems(orderItems);
    }

    @CacheEvict(allEntries=true)
    public void add(OrderItem orderItem) {
        orderItemDAO.save(orderItem);
    }
    @Cacheable(key="'orderItems-one-'+ #p0")
    public OrderItem get(int id) {
        return orderItemDAO.findOne(id);
    }

    @CacheEvict(allEntries=true)
    public void delete(int id) {
        orderItemDAO.delete(id);
    }

    //得到一个产品的销售数量
    public int getSaleCount(Product product) {
        OrderItemService orderItemService = SpringContextUtil.getBean(OrderItemService.class);
        List<OrderItem> ois =orderItemService.listByProduct(product);
        int result =0;
        for (OrderItem oi : ois) {
            if(null!=oi.getOrder())
                if(null!= oi.getOrder() && null!=oi.getOrder().getPayDate())
                    result+=oi.getNumber();
        }
        return result;
    }

    @Cacheable(key="'orderItems-uid-'+ #p0.id")
    public List<OrderItem> listByUser(User user) {
        return orderItemDAO.findByUserAndOrderIsNull(user);
    }

    @Cacheable(key="'orderItems-pid-'+ #p0.id")
    public List<OrderItem> listByProduct(Product product) {
        return orderItemDAO.findByProduct(product);
    }
    @Cacheable(key="'orderItems-oid-'+ #p0.id")
    public List<OrderItem> listByOrder(Order order) {
        //在结算的时候可能对于某个产品的订单项早已存在（即之前被加入过购物车）
        //但是这种订单项并没有对应的订单，所以 Order 栏目会是 null
        //之所以有这个方法是因为需要在结算之前将购物车内一样的产品进行数量的叠加！
        //比如我之前在购物车内有一个产品A，然后现在我在产品A的产品页面点击购买
        //那么跳到接算页面后，实际的购买数量会是 2 （将购物车的也算进去了）
        return orderItemDAO.findByOrderOrderByIdDesc(order);
    }

}