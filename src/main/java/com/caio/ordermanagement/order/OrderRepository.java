package com.caio.ordermanagement.order;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface  OrderRepository extends JpaRepository<Order, Long>{
    
    List<Order> findAllByUserId(Long userId);
}