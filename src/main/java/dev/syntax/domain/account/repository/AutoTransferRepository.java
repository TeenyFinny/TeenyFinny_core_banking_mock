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

	@Query(value = "SELECT a FROM AutoTransfer a JOIN FETCH a.user JOIN FETCH a.fromAccount JOIN FETCH a.toAccount WHERE a.status = :status",
		countQuery = "SELECT count(a) FROM AutoTransfer a WHERE a.status = :status")
	Page<AutoTransfer> findByStatus(@Param("status") AutoTransferStatus status, Pageable pageable);

	@Query(value = "SELECT a FROM AutoTransfer a JOIN FETCH a.user JOIN FETCH a.fromAccount JOIN FETCH a.toAccount WHERE a.nextTransferDay BETWEEN :startDate AND :endDate",
		countQuery = "SELECT count(a) FROM AutoTransfer a WHERE a.nextTransferDay BETWEEN :startDate AND :endDate")
	Page<AutoTransfer> findByNextTransferDayBetween(
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate,
		Pageable pageable
	);

	@Query(value = "SELECT a FROM AutoTransfer a JOIN FETCH a.user JOIN FETCH a.fromAccount JOIN FETCH a.toAccount WHERE a.status = :status AND a.nextTransferDay BETWEEN :startDate AND :endDate",
		countQuery = "SELECT count(a) FROM AutoTransfer a WHERE a.status = :status AND a.nextTransferDay BETWEEN :startDate AND :endDate")
	Page<AutoTransfer> findByStatusAndNextTransferDayBetween(
		@Param("status") AutoTransferStatus status,
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate,
		Pageable pageable
	);
}
