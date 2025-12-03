package dev.syntax.domain.account.repository;

import dev.syntax.domain.account.entity.AutoTransfer;
import dev.syntax.domain.account.enums.AutoTransferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AutoTransferRepository extends JpaRepository<AutoTransfer, Long> {
    List<AutoTransfer> findByNextTransferDay(LocalDate date);
    
    /**
     * 상태별 자동이체 조회 (페이징)
     */
    Page<AutoTransfer> findByStatus(AutoTransferStatus status, Pageable pageable);
    
    /**
     * 날짜 범위별 자동이체 조회 (페이징)
     */
    @Query("SELECT a FROM AutoTransfer a WHERE a.nextTransferDay BETWEEN :startDate AND :endDate")
    Page<AutoTransfer> findByNextTransferDayBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );
    
    /**
     * 상태와 날짜 범위로 자동이체 조회 (페이징)
     */
    @Query("SELECT a FROM AutoTransfer a WHERE a.status = :status AND a.nextTransferDay BETWEEN :startDate AND :endDate")
    Page<AutoTransfer> findByStatusAndNextTransferDayBetween(
            @Param("status") AutoTransferStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );
}
