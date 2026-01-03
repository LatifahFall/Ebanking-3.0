package cryptoservice.repository;

import cryptoservice.model.CryptoHolding;
import cryptoservice.model.CryptoHoldingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CryptoHoldingRepository extends JpaRepository<CryptoHolding, CryptoHoldingId> {
    List<CryptoHolding> findByWalletId(Long walletId);
}
