package com.logobing.repository;
import org.springframework.data.repository.query.Param;

import com.logobing.model.DepositRequest;
import com.logobing.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DepositRequestRepository extends JpaRepository<DepositRequest, Long> {
    Optional<DepositRequest> findByRequestId(String requestId);
    List<DepositRequest> findByPlayerOrderByCreatedAtDesc(Player player);
    List<DepositRequest> findByStatus(String status);
}
