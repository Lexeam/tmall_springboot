package com.MySpringboot.tmall.service;

import java.util.List;

import com.MySpringboot.tmall.dao.CategoryDAO;
import com.MySpringboot.tmall.pojo.Product;
import com.MySpringboot.tmall.util.Page4Navigator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.MySpringboot.tmall.pojo.Category;

@Service
@CacheConfig(cacheNames="categories")
public class  CategoryService {
    @Autowired
    CategoryDAO categoryDAO;

    @CacheEvict(allEntries=true)
//  @CachePut(key="'category-one-'+ #p0")
    public void add(Category bean) {
        categoryDAO.save(bean);
    }

    @CacheEvict(allEntries=true)
//  @CacheEvict(key="'category-one-'+ #p0")
    public void delete(int id) {
        categoryDAO.delete(id);
    }

    @Cacheable(key="'categories-one-'+ #p0")
    public Category get(int id) {
        Category c= categoryDAO.findOne(id);
        return c;
    }

    @CacheEvict(allEntries=true)
//  @CachePut(key="'category-one-'+ #p0")
    public void update(Category bean) {
        categoryDAO.save(bean);
    }

    @Cacheable(key="'categories-page-'+#p0+ '-' + #p1")
    public Page4Navigator<Category> list(int start, int size, int navigatePages) {
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable = new PageRequest(start, size,sort);
        Page pageFromJPA =categoryDAO.findAll(pageable);

        return new Page4Navigator<>(pageFromJPA,navigatePages);
    }

    @Cacheable(key="'categories-all'")
    public List<Category> list() {
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        //Iterable<T> findAll(Sort sort)
        // 此函数原型说明及拓展：https://blog.csdn.net/hbtj_1216/article/details/79773839
        return categoryDAO.findAll(sort);
    }

    //这里 remove 的作用和 Order 和 OrderItem 的关系一样
    //因为两者有双向的联系，在组成 json 的时候会出现死循环
    //所以在初始化完 category 后要把 product 里面的 category 对象抹掉

    public void removeCategoryFromProduct(List<Category> cs) {
        for (Category category : cs) {
            removeCategoryFromProduct(category);
        }
    }

    public void removeCategoryFromProduct(Category category) {
        List<Product> products =category.getProducts();
        if(null!=products) {
            for (Product product : products) {
                product.setCategory(null);
            }
        }

        List<List<Product>> productsByRow =category.getProductsByRow();
        if(null!=productsByRow) {
            for (List<Product> ps : productsByRow) {
                for (Product p: ps) {
                    p.setCategory(null);
                }
            }
        }
    }
}