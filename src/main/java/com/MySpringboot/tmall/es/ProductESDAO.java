package com.MySpringboot.tmall.es;

import com.MySpringboot.tmall.pojo.Product;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProductESDAO extends ElasticsearchRepository<Product,Integer>{

}