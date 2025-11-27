package dev.syntax.domain.user.repository;

import dev.syntax.domain.user.entity.CoreUser;
import jakarta.validation.constraints.NotNull;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CoreUserRepository extends JpaRepository<CoreUser, Long> {
    boolean existsByChannelUserId(@NotNull Long channelUserId);

    Optional<CoreUser> findByChannelUserId(Long channelUserId);
}
