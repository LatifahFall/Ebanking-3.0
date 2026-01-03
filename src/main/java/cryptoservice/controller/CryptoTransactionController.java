package cryptoservice.controller;
import cryptoservice.dto.BuyCryptoRequest;
import cryptoservice.model.CryptoTransaction;
import cryptoservice.service.CryptoTransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import cryptoservice.dto.SellCryptoRequest;
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

    @PostMapping("/buy")
    public ResponseEntity<CryptoTransaction> buyCrypto(
            @RequestParam Long walletId,
            @Valid @RequestBody BuyCryptoRequest request) {
        
        CryptoTransaction transaction = transactionService.buyTransaction(
                walletId,
                request.getSymbol(),
                request.getEurAmount()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @PostMapping("/sell")
    public ResponseEntity<CryptoTransaction> sellCrypto(
            @RequestParam Long walletId,
            @Valid @RequestBody SellCryptoRequest request) {
        
        CryptoTransaction transaction = transactionService.sellTransaction(
                walletId,
                request.getSymbol(),
                request.getCryptoAmount()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }
}
