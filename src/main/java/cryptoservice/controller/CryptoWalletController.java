package cryptoservice.controller;

import cryptoservice.model.CryptoWallet;
import cryptoservice.service.CryptoWalletService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallets")
public class CryptoWalletController {

    private final CryptoWalletService walletService;

    public CryptoWalletController(CryptoWalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping
    public ResponseEntity<CryptoWallet> createWallet(@RequestParam Long userId) {
        CryptoWallet wallet = walletService.createWallet(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(wallet);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<CryptoWallet> getWalletByUserId(@PathVariable Long userId) {
        CryptoWallet wallet = walletService.getWalletByUserId(userId);
        return ResponseEntity.ok(wallet);
    }
    
    @PatchMapping("/activate")
    public ResponseEntity<CryptoWallet> activateWallet(@RequestParam Long userId) {
        CryptoWallet wallet = walletService.activateWallet(userId);
        return ResponseEntity.ok(wallet);
    }
    
    @PatchMapping("/deactivate")
    public ResponseEntity<CryptoWallet> deactivateWallet(@RequestParam Long userId) {
        CryptoWallet wallet = walletService.deactivateWallet(userId);
        return ResponseEntity.ok(wallet);
    }
}
