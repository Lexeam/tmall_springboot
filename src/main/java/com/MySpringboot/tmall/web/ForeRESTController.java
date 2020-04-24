package com.MySpringboot.tmall.web;

import com.MySpringboot.tmall.comparator.*;
import com.MySpringboot.tmall.pojo.*;
import com.MySpringboot.tmall.service.*;
import com.MySpringboot.tmall.comparator.*;
import com.MySpringboot.tmall.pojo.*;
import com.MySpringboot.tmall.service.*;
import com.MySpringboot.tmall.util.Result;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
public class ForeRESTController {
    @Autowired
    CategoryService categoryService;
    @Autowired
    ProductService productService;
    @Autowired
    UserService userService;
    @Autowired
    ProductImageService productImageService;
    @Autowired
    PropertyValueService propertyValueService;
    @Autowired
    OrderItemService orderItemService;
    @Autowired
    ReviewService reviewService;
    @Autowired
    OrderService orderService;

    @GetMapping("/forehome")
    public Object home() {
        List<Category> cs= categoryService.list();//查询所有分类
        productService.fill(cs);//给分类填充对应的产品集合
        productService.fillByRow(cs);//给分类填充对应的推荐产品集合
        categoryService.removeCategoryFromProduct(cs);//避免序列化时候产生递归死循环
        return cs;
    }

//    使用 shiro 前的注册代码
//    @PostMapping("/foreregister")
//    public Object register(@RequestBody User user) {
//        String name =  user.getName();
//        String password = user.getPassword();
//        name = HtmlUtils.htmlEscape(name);//防止恶意的script名称
//        user.setName(name);
//        boolean exist = userService.isExist(name);
//
//        if(exist){
//            String message ="用户名已经被使用,不能使用";
//            return Result.fail(message);
//        }
//
//        user.setPassword(password);
//
//        userService.add(user);
//
//        return Result.success();
//    }

    @PostMapping("/foreregister")
    public Object register(@RequestBody User user) {
        String name =  user.getName();
        String password = user.getPassword();
        name = HtmlUtils.htmlEscape(name);
        user.setName(name);

        boolean exist = userService.isExist(name);

        if(exist){
            String message ="用户名已经被使用,不能使用";
            return Result.fail(message);
        }

        String salt = new SecureRandomNumberGenerator().nextBytes().toString();
        int times = 2;
        String algorithmName = "md5";

        String encodedPassword = new SimpleHash(algorithmName, password, salt, times).toString();

        user.setSalt(salt);
        user.setPassword(encodedPassword);

        userService.add(user);

        return Result.success();
    }

//    使用 shiro 前的登陆代码
//    @PostMapping("/forelogin")
//    public Object login(@RequestBody User userParam, HttpSession session) {
//        String name =  userParam.getName();
//        name = HtmlUtils.htmlEscape(name);
//
//        User user =userService.get(name,userParam.getPassword());
//        if(null==user){
//            String message ="账号密码错误";
//            return Result.fail(message);
//        }
//        else{
//            session.setAttribute("user", user);
//            return Result.success();
//        }
//    }

    @PostMapping("/forelogin")
    public Object login(@RequestBody User userParam, HttpSession session) {
        String name =  userParam.getName();
        name = HtmlUtils.htmlEscape(name);

        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(name, userParam.getPassword());
        try {
            subject.login(token);
            User user = userService.getByName(name);
//          subject.getSession().setAttribute("user", user);
            session.setAttribute("user", user);
            return Result.success();
        } catch (AuthenticationException e) {
            String message ="账号密码错误";
            return Result.fail(message);
        }

    }

    /*
    1. 获取参数pid
    2. 根据pid获取Product 对象product
    3. 根据对象product，获取这个产品对应的单个图片集合
    4. 根据对象product，获取这个产品对应的详情图片集合
    5. 获取产品的所有属性值
    6. 获取产品对应的所有的评价
    7. 设置产品的销量和评价数量
    8. 把上述取值放在 map 中
    9. 通过 Result 把这个 map 返回到浏览器去
     */
    @GetMapping("/foreproduct/{pid}")
    public Object product(@PathVariable("pid") int pid) {
        Product product = productService.get(pid);
        List<ProductImage> productSingleImages = productImageService.listSingleProductImages(product);
        List<ProductImage> productDetailImages = productImageService.listDetailProductImages(product);
        product.setProductSingleImages(productSingleImages);
        product.setProductDetailImages(productDetailImages);

        List<PropertyValue> pvs = propertyValueService.list(product);
        List<Review> reviews = reviewService.list(product);
        productService.setSaleAndReviewNumber(product);
        productImageService.setFirstProdutImage(product);

        Map<String,Object> map= new HashMap<>();
        map.put("product", product);//产品
        map.put("pvs", pvs);//产品属性值
        map.put("reviews", reviews);//产品评论

        return Result.success(map);
    }

//    使用shiro前
//    //点击一些需要登陆才能做的操作的时候，需要检查是否登陆了，在 session 里面
//    @GetMapping("forecheckLogin")
//    public Object checkLogin( HttpSession session) {
//        User user =(User)  session.getAttribute("user");
//        if(null!=user)
//            return Result.success();
//        return Result.fail("未登录");
//    }

    @GetMapping("forecheckLogin")
    public Object checkLogin() {
        Subject subject = SecurityUtils.getSubject();
        if(subject.isAuthenticated())
            return Result.success();
        else
            return Result.fail("未登录");
    }

    /**
     *
     *  1. 获取参数cid
        2. 根据cid获取分类Category对象 c
        3. 为c填充产品
        4. 为产品填充销量和评价数据
        5. 获取参数sort
          5.1 如果sort==null，即不排序
          5.2 如果sort!=null，则根据sort的值，从5个Comparator比较器中选择一个对应的排序器进行排序
        6. 返回对象 c
     */
    @GetMapping("forecategory/{cid}")
    public Object category(@PathVariable int cid,String sort) {
        Category c = categoryService.get(cid);
        productService.fill(c);
        productService.setSaleAndReviewNumber(c.getProducts());
        categoryService.removeCategoryFromProduct(c);

        if(null!=sort){
            switch(sort){
                case "review":
                    Collections.sort(c.getProducts(),new ProductReviewComparator());
                    break;
                case "date" :
                    Collections.sort(c.getProducts(),new ProductDateComparator());
                    break;

                case "saleCount" :
                    Collections.sort(c.getProducts(),new ProductSaleCountComparator());
                    break;

                case "price":
                    Collections.sort(c.getProducts(),new ProductPriceComparator());
                    break;

                case "all":
                    Collections.sort(c.getProducts(),new ProductAllComparator());
                    break;
            }
        }
        return c;
    }

    @PostMapping("foresearch")
    public Object search( String keyword){
        if(null==keyword)
            keyword = "";
        List<Product> ps= productService.search(keyword,0,20);
        productImageService.setFirstProdutImages(ps);
        productService.setSaleAndReviewNumber(ps);
        return ps;
    }

    @GetMapping("forebuyone") //“立即购买”按钮触发
    public Object buyone(int pid, int num, HttpSession session) {
        return buyoneAndAddCart(pid,num,session);
    }

    private int buyoneAndAddCart(int pid, int num, HttpSession session) {
        Product product = productService.get(pid); //获取产品id
        int oiid = 0;
        User user =(User)  session.getAttribute("user");//获取当前用户
        boolean found = false;
        List<OrderItem> ois = orderItemService.listByUser(user);//根据用户获取所有未归属至任何订单的订单项
        for (OrderItem oi : ois) {//寻找购物车内是否有此用户对这个产品曾添加到购物车过
            if(oi.getProduct().getId()==product.getId()){
                oi.setNumber(oi.getNumber()+num);
                orderItemService.update(oi);
                found = true;
                oiid = oi.getId();
                break;
            }
        }

        if(!found){//若没找到则需要新建订单项、找到则只需要修改数量就行了
                   //注意：加入购物车或者点击购买都会产生订单项，只有结算了才产生订单
            OrderItem oi = new OrderItem();
            oi.setUser(user);
            oi.setProduct(product);
            oi.setNumber(num);
            orderItemService.add(oi);
            oiid = oi.getId();
        }
        return oiid;
    }

    @GetMapping("forebuy")
    public Object buy(String[] oiid,HttpSession session){
        List<OrderItem> orderItems = new ArrayList<>();
        float total = 0;

        for (String strid : oiid) {
            int id = Integer.parseInt(strid);
            OrderItem oi= orderItemService.get(id);
            total +=oi.getProduct().getPromotePrice()*oi.getNumber();
            orderItems.add(oi);
        }

        productImageService.setFirstProdutImagesOnOrderItems(orderItems);

        session.setAttribute("ois", orderItems);

        Map<String,Object> map = new HashMap<>();
        map.put("orderItems", orderItems);
        map.put("total", total);
        return Result.success(map);
    }

    @GetMapping("foreaddCart")
    public Object addCart(int pid, int num, HttpSession session) {
        buyoneAndAddCart(pid,num,session);
        return Result.success();
    }

    @GetMapping("forecart")
    public Object cart(HttpSession session) {//供查看购物车界面使用
        User user =(User)  session.getAttribute("user");
        List<OrderItem> ois = orderItemService.listByUser(user);//找出属于这个用户，但是还未隶属与任何订单的订单项
        productImageService.setFirstProdutImagesOnOrderItems(ois);
        return ois;
    }

    @GetMapping("forechangeOrderItem") //对应 cartPage.html 中 syncPrice => 对购物车内订单项的数量加减产生的响应
    public Object changeOrderItem( HttpSession session, int pid, int num) {
        /*
          1. 判断用户是否登录
          2. 获取pid和number
          3. 遍历出用户当前所有的未生成订单的OrderItem
          4. 根据pid找到匹配的OrderItem，并修改数量后更新到数据库
          5. 返回 Result.success()
        */
        User user =(User)  session.getAttribute("user");
        if(null==user)
            return Result.fail("未登录");

        List<OrderItem> ois = orderItemService.listByUser(user);
        for (OrderItem oi : ois) {
            if(oi.getProduct().getId()==pid){
                oi.setNumber(num);
                orderItemService.update(oi);
                break;
            }
        }
        return Result.success();
    }

    @GetMapping("foredeleteOrderItem")//对应 cartPage.html 中 cartPageRegisterListeners => 对购物车内的订单项进行删除的响应
    public Object deleteOrderItem(HttpSession session,int oiid){
        User user =(User)  session.getAttribute("user");
        if(null==user)
            return Result.fail("未登录");
        orderItemService.delete(oiid);
        return Result.success();
    }

    @PostMapping("forecreateOrder")//对应 buyPage.html 中 submitOrder 函数 => 创建订单使用
    public Object createOrder(@RequestBody Order order, HttpSession session){
        User user =(User)  session.getAttribute("user");
        if(null==user)
            return Result.fail("未登录"); //根据 session 检查登陆状态
        String orderCode = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()) + RandomUtils.nextInt(10000);//生成随机的订单号（包含了日期信息）
        order.setOrderCode(orderCode);
        order.setCreateDate(new Date());
        order.setUser(user);
        order.setStatus(OrderService.waitPay);//设置订单状态为未支付
        List<OrderItem> ois= (List<OrderItem>)  session.getAttribute("ois");//获取需要添加的订单项

        //给 ois 里面的所有订单项设置隶属于当前订单，即将 OrderItem 表中的 Order id 项给填充了（这个项是外键关联的！）
        // total 记录了所有订单的总价格
        float total =orderService.add(order,ois);


        Map<String,Object> map = new HashMap<>();
        map.put("oid", order.getId());
        map.put("total", total);

        return Result.success(map);//返回一个 map 来给前端整数据
    }

    @GetMapping("forepayed")//对应 payedPage.html 里面的 load 函数 => 用于确认支付后修改订单的状态、并返回订单信息给前台
    public Object payed(int oid) {
        Order order = orderService.get(oid);
        order.setStatus(OrderService.waitDelivery);
        order.setPayDate(new Date());
        orderService.update(order);
        return order;
    }

    @GetMapping("forebought")// 对应 boughtPage.html 里面的 load 函数 => 用于查询 session 中的用户所包含的未被删除的订单的信息，并返回给前台
    public Object bought(HttpSession session) {
        User user =(User)  session.getAttribute("user");
        if(null==user)
            return Result.fail("未登录");
        List<Order> os= orderService.listByUserWithoutDelete(user);
        orderService.removeOrderFromOrderItem(os);
        return os;
    }

    @GetMapping("foreconfirmPay")//对应 confirmPayPage.html 里面的 load 函数 => 用于获取订单对象返回给前台进行渲染
    public Object confirmPay(int oid) {
        Order o = orderService.get(oid);//通过订单的id获取订单对象
        orderItemService.fill(o);//为订单对象填充对应的订单项
        orderService.cacl(o);//计算订单的总金额
        orderService.removeOrderFromOrderItem(o);//防止序列化为 json 数据的时候重复递归（因为order和orderItem有双向关联的对象）
        return o;
    }

    @GetMapping("foreorderConfirmed")//对应 orderConfirmedPage.html 里面的 load 函数 => 用于确认收货后对订单状态的修改
    public Object orderConfirmed( int oid) {
        Order o = orderService.get(oid);
        o.setStatus(OrderService.waitReview);
        o.setConfirmDate(new Date());
        orderService.update(o);
        return Result.success();
    }

    @PutMapping("foredeleteOrder")//对应 boughtPage.html 里面的 orderPageRegisterListeners 函数 => 用于对订单的删除操作
    public Object deleteOrder(int oid){
        Order o = orderService.get(oid);
        o.setStatus(OrderService.delete);//修改订单状态为删除状态
        orderService.update(o);//更新到数据库
        return Result.success();
    }

    @GetMapping("forereview")//对应 reviewPage.html 里面的 load 函数 => 用于
    public Object review(int oid) {
        Order o = orderService.get(oid);//根据订单id获取订单对象
        orderItemService.fill(o);//给订单对象的订单项填充内容
        orderService.removeOrderFromOrderItem(o);//删除所有订单项里面的订单归属关系防止序列化时递归
        Product p = o.getOrderItems().get(0).getProduct();//获取第一个产品（为了简化，每个订单只对其第一个产品进行评价）
        List<Review> reviews = reviewService.list(p);//获取订单p的评价集合
        productService.setSaleAndReviewNumber(p);//为product对象设置销量和评价数，和上面的fill一个道理，都是为transient标注的属性进行内容填充，因为数据库里面是取不出这些信息的！
        Map<String,Object> map = new HashMap<>();
        map.put("p", p);
        map.put("o", o);
        map.put("reviews", reviews);

        return Result.success(map);
    }

    @PostMapping("foredoreview")//对应 reviewPage.html 的 doreview 函数 => 把前台的评价信存储到数据库中、并修改订单信息为已完成
    public Object doreview( HttpSession session,int oid,int pid,String content) {
        Order o = orderService.get(oid);
        o.setStatus(OrderService.finish);
        orderService.update(o);

        Product p = productService.get(pid);
        content = HtmlUtils.htmlEscape(content);//对字符进行转义，防止恶意的前台代码产生

        User user =(User)  session.getAttribute("user");
        Review review = new Review();
        review.setContent(content);
        review.setProduct(p);
        review.setCreateDate(new Date());
        review.setUser(user);
        reviewService.add(review);
        return Result.success();
    }

}