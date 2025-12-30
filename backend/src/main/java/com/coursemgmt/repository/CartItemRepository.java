package com.coursemgmt.repository;

import com.coursemgmt.model.Cart;
import com.coursemgmt.model.CartItem;
import com.coursemgmt.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    // Tìm CartItem theo Cart và Course
    Optional<CartItem> findByCartAndCourse(Cart cart, Course course);
    
    // Xóa tất cả items trong Cart (derived query)
    void deleteByCart(Cart cart);
    
    // Xóa tất cả items trong Cart theo Cart ID (explicit query - more reliable)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId")
    int deleteAllByCartId(@Param("cartId") Long cartId);
    
    // Xóa tất cả cart items của một course
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM CartItem ci WHERE ci.course.id = :courseId")
    void deleteByCourseId(@Param("courseId") Long courseId);
}

