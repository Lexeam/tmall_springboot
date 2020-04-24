package com.MySpringboot.tmall.web;

import com.MySpringboot.tmall.pojo.Order;
import com.MySpringboot.tmall.service.OrderItemService;
import com.MySpringboot.tmall.service.OrderService;
import com.MySpringboot.tmall.util.Page4Navigator;
import com.MySpringboot.tmall.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Date;

@RestController
public class OrderController {
    @Autowired
    OrderService orderService;
    @Autowired
    OrderItemService orderItemService;

    @GetMapping("/orders")
    public Page4Navigator<Order> list(@RequestParam(value = "start", defaultValue = "0") int start, @RequestParam(value = "size", defaultValue = "5") int size) throws Exception {
        start = start<0 ? 0 : start;
        Page4Navigator<Order> page =orderService.list(start, size, 5);
        //查询 Order ，查出来的 Order 里面是有许多的 @Transient 字段标注的东西，那些东西数据库里面是没有的，所以需要人为填充
        //特别是里面包含了一个其他类，且用了 Transient 标注，数据库不存储这个信息，但是作为 pojo 对象又因为需求要有！
        orderItemService.fill(page.getContent());
        orderService.removeOrderFromOrderItem(page.getContent());
        return page;
    }
    @PutMapping("deliveryOrder/{oid}")
    public Object deliveryOrder(@PathVariable int oid) throws IOException {
        Order o = orderService.get(oid);
        o.setDeliveryDate(new Date());
        o.setStatus(OrderService.waitConfirm);
        orderService.update(o);
        return Result.success();
    }
}