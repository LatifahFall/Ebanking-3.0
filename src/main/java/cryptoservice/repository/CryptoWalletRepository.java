package cryptoservice.repository;

import cryptoservice.model.CryptoWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CryptoWalletRepository extends JpaRepository<CryptoWallet, Long> {
    Optional<CryptoWallet> findByUserId(Long userId);
}
