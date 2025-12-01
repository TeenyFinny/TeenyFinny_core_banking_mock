package dev.syntax.domain.account.repository;

import dev.syntax.domain.account.entity.AutoTransfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AutoTransferRepository extends JpaRepository<AutoTransfer, Long> {
    List<AutoTransfer> findByNextTransferDay(LocalDate date);
}
