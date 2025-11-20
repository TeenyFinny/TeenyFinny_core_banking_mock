package dev.syntax.domain.account.repository;

import dev.syntax.domain.account.entity.AutoTransfer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AutoTransferRepository extends JpaRepository<AutoTransfer, Long> {
}
