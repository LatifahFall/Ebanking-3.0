package cryptoservice.service;

import cryptoservice.model.CryptoTransaction;
import cryptoservice.repository.CryptoTransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CryptoTransactionService {

    private final CryptoTransactionRepository transactionRepository;

    public CryptoTransactionService(CryptoTransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<CryptoTransaction> getTransactionsByWalletId(Long walletId) {
        return transactionRepository.findByWalletId(walletId);
    }
}
