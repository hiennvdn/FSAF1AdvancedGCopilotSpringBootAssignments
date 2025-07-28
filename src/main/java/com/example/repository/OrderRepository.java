package com.example.repository;

import com.example.entity.Order;
import com.example.entity.OrderStatus;
import com.example.repository.base.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends BaseRepository<Order, Long> {
    
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId")
    Page<Order> findByUserId(@Param("userId") Long userId, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.status = :status")
    Page<Order> findByStatus(@Param("status") OrderStatus status, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.status = :status")
    List<Order> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") OrderStatus status);
    
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT o FROM Order o WHERE o.totalAmount >= :minAmount")
    List<Order> findByMinAmount(@Param("minAmount") BigDecimal minAmount);
    
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);
    
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = :status")
    BigDecimal calculateTotalAmountByStatus(@Param("status") OrderStatus status);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);
    
    @Query(value = "SELECT DATE(o.created_at) as order_date, COUNT(*) as order_count, SUM(o.total_amount) as total_amount " +
           "FROM orders o " +
           "WHERE o.created_at BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(o.created_at) " +
           "ORDER BY order_date", nativeQuery = true)
    List<Object[]> getOrderStatistics(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
