package com.coursemgmt.repository;

import com.coursemgmt.model.ETransactionStatus;
import com.coursemgmt.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    // Tìm theo user
    Page<Transaction> findByUserId(Long userId, Pageable pageable);
    
    // Tìm theo course
    Page<Transaction> findByCourseId(Long courseId, Pageable pageable);
    
    // Tìm theo transaction code (có thể có nhiều transactions cùng code khi thanh toán giỏ hàng)
    Optional<Transaction> findByTransactionCode(String transactionCode);
    
    // Tìm tất cả transactions theo transaction code (cho cart checkout với nhiều courses)
    List<Transaction> findAllByTransactionCode(String transactionCode);
    
    // Tìm theo status
    Page<Transaction> findByStatus(ETransactionStatus status, Pageable pageable);
    
    // Đếm theo status
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.status = :status")
    Long countByStatus(@Param("status") ETransactionStatus status);
    
    // Tìm giao dịch của user cho 1 course cụ thể
    Optional<Transaction> findByUserIdAndCourseIdAndStatus(
        Long userId, 
        Long courseId, 
        ETransactionStatus status
    );
    
    // Thống kê doanh thu theo khoảng thời gian
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE " +
           "t.status = 'SUCCESS' AND " +
           "t.createdAt BETWEEN :startDate AND :endDate")
    Double calculateRevenueByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    // Đếm số giao dịch thành công
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.status = 'SUCCESS'")
    Long countSuccessfulTransactions();
    
    // Top courses bán chạy
    @Query("SELECT t.course.id, t.course.title, COUNT(t) as total " +
           "FROM Transaction t WHERE t.status = 'SUCCESS' " +
           "GROUP BY t.course.id, t.course.title " +
           "ORDER BY total DESC")
    List<Object[]> findTopSellingCourses(Pageable pageable);
    
    // Doanh thu theo tháng
    @Query("SELECT MONTH(t.createdAt) as month, " +
           "YEAR(t.createdAt) as year, " +
           "SUM(t.amount) as revenue, " +
           "COUNT(t) as transactions " +
           "FROM Transaction t " +
           "WHERE t.status = 'SUCCESS' AND YEAR(t.createdAt) = :year " +
           "GROUP BY MONTH(t.createdAt), YEAR(t.createdAt) " +
           "ORDER BY month")
    List<Object[]> getMonthlyRevenue(@Param("year") int year);
    
    // Tính doanh thu từ transactions thành công của các courses thuộc instructor
    @Query("SELECT SUM(t.amount) FROM Transaction t " +
           "WHERE t.status = 'SUCCESS' AND t.course.instructor.id = :instructorId")
    Double calculateRevenueByInstructor(@Param("instructorId") Long instructorId);
    
    // Lấy transactions thành công của các courses thuộc instructor trong khoảng thời gian
    @Query("SELECT t FROM Transaction t " +
           "WHERE t.status = 'SUCCESS' AND t.course.instructor.id = :instructorId " +
           "AND t.createdAt BETWEEN :startDate AND :endDate")
    List<Transaction> findSuccessfulTransactionsByInstructorAndDateRange(
            @Param("instructorId") Long instructorId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    
    // Tính doanh thu từ transactions thành công của một course cụ thể
    @Query("SELECT SUM(t.amount) FROM Transaction t " +
           "WHERE t.status = 'SUCCESS' AND t.course.id = :courseId")
    Double calculateRevenueByCourseId(@Param("courseId") Long courseId);
    
    // Doanh thu theo tháng cho một course cụ thể
    @Query("SELECT MONTH(t.createdAt) as month, " +
           "YEAR(t.createdAt) as year, " +
           "SUM(t.amount) as revenue " +
           "FROM Transaction t " +
           "WHERE t.status = 'SUCCESS' AND t.course.id = :courseId AND YEAR(t.createdAt) = :year " +
           "GROUP BY MONTH(t.createdAt), YEAR(t.createdAt) " +
           "ORDER BY month")
    List<Object[]> getMonthlyRevenueByCourse(@Param("courseId") Long courseId, @Param("year") int year);
    
    // Lấy tất cả transactions của instructor (để lọc và sắp xếp)
    @Query("SELECT t FROM Transaction t " +
           "WHERE t.course.instructor.id = :instructorId " +
           "ORDER BY t.createdAt DESC")
    List<Transaction> findByInstructorIdOrderByCreatedAtDesc(@Param("instructorId") Long instructorId);
    
    // Xóa tất cả transactions của một course
    @Modifying
    @Query("DELETE FROM Transaction t WHERE t.course.id = :courseId")
    void deleteByCourseId(@Param("courseId") Long courseId);
}
