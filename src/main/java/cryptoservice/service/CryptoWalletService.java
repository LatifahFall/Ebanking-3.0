package cryptoservice.service;

import cryptoservice.model.CryptoWallet;
import cryptoservice.model.WalletStatus;
import cryptoservice.repository.CryptoWalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CryptoWalletService {

    private final CryptoWalletRepository walletRepository;

    public CryptoWalletService(CryptoWalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Transactional
    public CryptoWallet createWallet(Long userId) {
        // Check if wallet already exists
        if (walletRepository.findByUserId(userId).isPresent()) {
            throw new RuntimeException("Wallet already exists for user: " + userId);
        }

        CryptoWallet wallet = new CryptoWallet(userId);
        wallet.setStatus(WalletStatus.INACTIVE);
        return walletRepository.save(wallet);
    }

    public CryptoWallet getWalletByUserId(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found for user: " + userId));
    }
    
    @Transactional
    public CryptoWallet activateWallet(Long userId) {
        CryptoWallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found for user: " + userId));
        
        wallet.setStatus(WalletStatus.ACTIVE);
        return walletRepository.save(wallet);
    }
    
    @Transactional
    public CryptoWallet deactivateWallet(Long userId) {
        CryptoWallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found for user: " + userId));
        
        wallet.setStatus(WalletStatus.INACTIVE);
        return walletRepository.save(wallet);
    }
}
