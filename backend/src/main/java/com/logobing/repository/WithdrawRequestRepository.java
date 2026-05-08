package com.logobing.repository;
import org.springframework.data.repository.query.Param;

import com.logobing.model.WithdrawRequest;
import com.logobing.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WithdrawRequestRepository extends JpaRepository<WithdrawRequest, Long> {
    Optional<WithdrawRequest> findByRequestId(String requestId);
    List<WithdrawRequest> findByPlayerOrderByCreatedAtDesc(Player player);
    List<WithdrawRequest> findByStatus(String status);
}
