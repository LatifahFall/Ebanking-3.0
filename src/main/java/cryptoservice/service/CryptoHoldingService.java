package cryptoservice.service;

import cryptoservice.model.CryptoHolding;
import cryptoservice.repository.CryptoHoldingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CryptoHoldingService {

    private final CryptoHoldingRepository holdingRepository;

    public CryptoHoldingService(CryptoHoldingRepository holdingRepository) {
        this.holdingRepository = holdingRepository;
    }

    public List<CryptoHolding> getHoldingsByWalletId(Long walletId) {
        return holdingRepository.findByWalletId(walletId);
    }
}
