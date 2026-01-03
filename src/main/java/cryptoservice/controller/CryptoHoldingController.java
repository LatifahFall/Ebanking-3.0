package cryptoservice.controller;

import cryptoservice.model.CryptoHolding;
import cryptoservice.service.CryptoHoldingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/holdings")
public class CryptoHoldingController {

    private final CryptoHoldingService holdingService;

    public CryptoHoldingController(CryptoHoldingService holdingService) {
        this.holdingService = holdingService;
    }

    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<List<CryptoHolding>> getHoldingsByWallet(@PathVariable Long walletId) {
        List<CryptoHolding> holdings = holdingService.getHoldingsByWalletId(walletId);
        return ResponseEntity.ok(holdings);
    }
}
