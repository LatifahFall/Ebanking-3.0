package cryptoservice.controller;

import cryptoservice.model.CryptoTransaction;
import cryptoservice.service.CryptoTransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class CryptoTransactionController {

    private final CryptoTransactionService transactionService;

    public CryptoTransactionController(CryptoTransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<List<CryptoTransaction>> getTransactionsByWallet(@PathVariable Long walletId) {
        List<CryptoTransaction> transactions = transactionService.getTransactionsByWalletId(walletId);
        return ResponseEntity.ok(transactions);
    }
}
