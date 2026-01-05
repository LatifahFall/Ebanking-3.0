package cryptoservice.service;

import cryptoservice.model.*;
import cryptoservice.repository.CryptoTransactionRepository;
import cryptoservice.repository.CryptoWalletRepository;
import cryptoservice.repository.CryptoHoldingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Service
public class CryptoTransactionService {

    private final CryptoTransactionRepository transactionRepository;
    private final CryptoWalletRepository walletRepository;
    private final CryptoHoldingRepository holdingRepository;
    private final CoinsCacheService coinsCacheService;
    private final TransactionAuditProducer auditProducer;

    private static final BigDecimal MIN_EUR_AMOUNT = new BigDecimal("10.00");
    private static final BigDecimal FEE_PERCENTAGE = new BigDecimal("0.01"); // 1%

    public CryptoTransactionService(
            CryptoTransactionRepository transactionRepository,
            CryptoWalletRepository walletRepository,
            CryptoHoldingRepository holdingRepository,
            CoinsCacheService coinsCacheService,
            TransactionAuditProducer auditProducer) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
        this.holdingRepository = holdingRepository;
        this.coinsCacheService = coinsCacheService;
        this.auditProducer = auditProducer;
    }

    public List<CryptoTransaction> getTransactionsByWalletId(Long walletId) {
        return transactionRepository.findByWalletId(walletId);
    }

    @Transactional
    public CryptoTransaction buyTransaction(Long walletId, String symbol, BigDecimal eurAmount) {
        // 1. Wallet exists and is ACTIVE
        CryptoWallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new IllegalStateException("Wallet is not active");
        }

        // 2. Symbol is supported
        if (!SupportedCryptoCoins.isSupported(symbol)) {
            throw new IllegalArgumentException("Unsupported crypto symbol: " + symbol);
        }

        // 3. eurAmount ≥ 10.00 EUR
        if (eurAmount.compareTo(MIN_EUR_AMOUNT) < 0) {
            throw new IllegalArgumentException("Minimum EUR amount is " + MIN_EUR_AMOUNT);
        }

        // 4. eurAmount ≤ wallet.balance
        if (eurAmount.compareTo(wallet.getBalance()) > 0) {
            throw new IllegalArgumentException("Insufficient balance. Available: " + wallet.getBalance() + " EUR");
        }

        // Calculate fee: 1% of eurAmount
        BigDecimal fee = eurAmount.multiply(FEE_PERCENTAGE).setScale(2, RoundingMode.HALF_UP);

        // Get current price from Redis cache
        BigDecimal eurPricePerUnit = getCurrentPriceForSymbol(symbol);
        
        // Calculate crypto amount: (eurAmount - fee) / currentPrice
        BigDecimal eurAmountAfterFee = eurAmount.subtract(fee);
        BigDecimal cryptoAmount = eurAmountAfterFee.divide(eurPricePerUnit, 8, RoundingMode.DOWN);

        // Create transaction (status COMPLETED)
        CryptoTransaction transaction = new CryptoTransaction(
                walletId,
                TransactionType.BUY,
                symbol.toUpperCase(),
                cryptoAmount,
                eurAmount,
                eurPricePerUnit,
                fee
        );
        transaction.setStatus(TransactionStatus.COMPLETED);

        // Update balances atomically (wallet balance - full eurAmount including fee)
        wallet.setBalance(wallet.getBalance().subtract(eurAmount));
        walletRepository.save(wallet);

        // Update or create holding (only the crypto amount purchased, after fee)
        updateHolding(walletId, symbol.toUpperCase(), cryptoAmount);

        // Save and return transaction and publish audit event (non-blocking)
        CryptoTransaction savedTransaction = transactionRepository.save(transaction);
        //kafka event
        auditProducer.publish(savedTransaction);
        return savedTransaction;
    }

    @Transactional
    public CryptoTransaction sellTransaction(Long walletId, String symbol, BigDecimal cryptoAmount) {
        // 1. Wallet exists and is ACTIVE
        CryptoWallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new IllegalStateException("Wallet is not active");
        }

        // 2. Symbol is supported
        if (!SupportedCryptoCoins.isSupported(symbol)) {
            throw new IllegalArgumentException("Unsupported crypto symbol: " + symbol);
        }

        // 3. cryptoAmount ≥ 0.0001 (minimum crypto amount)
        BigDecimal minCryptoAmount = new BigDecimal("0.0001");
        if (cryptoAmount.compareTo(minCryptoAmount) < 0) {
            throw new IllegalArgumentException("Minimum crypto amount is 0.0001");
        }

        // 4. User has enough crypto (cryptoAmount ≤ holding.amount)
        CryptoHoldingId holdingId = new CryptoHoldingId(walletId, symbol.toUpperCase());
        CryptoHolding holding = holdingRepository.findById(holdingId)
                .orElseThrow(() -> new IllegalArgumentException("You don't own this crypto: " + symbol));

        if (cryptoAmount.compareTo(holding.getAmount()) > 0) {
            throw new IllegalArgumentException(
                "Insufficient crypto. You own: " + holding.getAmount() + " " + symbol
            );
        }

        // Get current price from Redis cache
        BigDecimal eurPricePerUnit = getCurrentPriceForSymbol(symbol);
        
        // Calculate EUR amount: cryptoAmount * currentPrice
        BigDecimal eurAmount = cryptoAmount.multiply(eurPricePerUnit).setScale(2, RoundingMode.HALF_UP);

        // Calculate fee: 1% of eurAmount received
        BigDecimal fee = eurAmount.multiply(FEE_PERCENTAGE).setScale(2, RoundingMode.HALF_UP);

        // EUR amount after fee (what user receives)
        BigDecimal eurAmountAfterFee = eurAmount.subtract(fee);

        // Create transaction (status COMPLETED)
        CryptoTransaction transaction = new CryptoTransaction(
                walletId,
                TransactionType.SELL,
                symbol.toUpperCase(),
                cryptoAmount,
                eurAmount,
                eurPricePerUnit,
                fee
        );
        transaction.setStatus(TransactionStatus.COMPLETED);

        // Update balances atomically
        // Add EUR to wallet (after fee deduction)
        wallet.setBalance(wallet.getBalance().add(eurAmountAfterFee));
        walletRepository.save(wallet);

        // Update holding (reduce crypto amount)
        holding.setAmount(holding.getAmount().subtract(cryptoAmount));
        holdingRepository.save(holding);

        // Save and return transaction and publish audit event (non-blocking)
        CryptoTransaction savedTransaction = transactionRepository.save(transaction);
        //kafka event
        auditProducer.publish(savedTransaction);
        return savedTransaction;
    }

    private BigDecimal getCurrentPriceForSymbol(String symbol) {
        // Find the coin by symbol in cache
        List<Map<String, Object>> allCoins = coinsCacheService.getCachedCoinsDetails();
        
        if (allCoins == null || allCoins.isEmpty()) {
            throw new IllegalStateException("Crypto prices not available. Please try again later.");
        }

        return allCoins.stream()
                .filter(coin -> symbol.equalsIgnoreCase((String) coin.get("symbol")))
                .findFirst()
                .map(coin -> {
                    Object price = coin.get("current_price");
                    if (price instanceof Number) {
                        return new BigDecimal(price.toString());
                    }
                    throw new IllegalStateException("Invalid price data for " + symbol);
                })
                .orElseThrow(() -> new IllegalArgumentException("Price not found for symbol: " + symbol));
    }

    private void updateHolding(Long walletId, String symbol, BigDecimal amountToAdd) {
        CryptoHoldingId holdingId = new CryptoHoldingId(walletId, symbol);
        
        CryptoHolding holding = holdingRepository.findById(holdingId)
                .orElse(new CryptoHolding(walletId, symbol, BigDecimal.ZERO));
        
        holding.setAmount(holding.getAmount().add(amountToAdd));
        holdingRepository.save(holding);
    }
}