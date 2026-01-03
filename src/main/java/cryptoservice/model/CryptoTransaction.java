package cryptoservice.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "crypto_transactions")
public class CryptoTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "wallet_id", nullable = false)
    private Long walletId;

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Column(name = "crypto_symbol", nullable = false, length = 10)
    private String cryptoSymbol;

    @Column(name = "crypto_amount", nullable = false, precision = 20, scale = 8)
    private BigDecimal cryptoAmount;

    @Column(name = "eur_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal eurAmount;

    @Column(name = "eur_price_per_unit", nullable = false, precision = 15, scale = 2)
    private BigDecimal eurPricePerUnit;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal fee = new BigDecimal("0.00");

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TransactionStatus status = TransactionStatus.COMPLETED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public CryptoTransaction() {
    }

    public CryptoTransaction(Long walletId, TransactionType type, String cryptoSymbol,
                            BigDecimal cryptoAmount, BigDecimal eurAmount,
                            BigDecimal eurPricePerUnit, BigDecimal fee) {
        this.walletId = walletId;
        this.type = type;
        this.cryptoSymbol = cryptoSymbol;
        this.cryptoAmount = cryptoAmount;
        this.eurAmount = eurAmount;
        this.eurPricePerUnit = eurPricePerUnit;
        this.fee = fee;
        this.status = TransactionStatus.COMPLETED;
    }

    // JPA Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = TransactionStatus.COMPLETED;
        }
        if (fee == null) {
            fee = new BigDecimal("0.00");
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWalletId() {
        return walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public String getCryptoSymbol() {
        return cryptoSymbol;
    }

    public void setCryptoSymbol(String cryptoSymbol) {
        this.cryptoSymbol = cryptoSymbol;
    }

    public BigDecimal getCryptoAmount() {
        return cryptoAmount;
    }

    public void setCryptoAmount(BigDecimal cryptoAmount) {
        this.cryptoAmount = cryptoAmount;
    }

    public BigDecimal getEurAmount() {
        return eurAmount;
    }

    public void setEurAmount(BigDecimal eurAmount) {
        this.eurAmount = eurAmount;
    }

    public BigDecimal getEurPricePerUnit() {
        return eurPricePerUnit;
    }

    public void setEurPricePerUnit(BigDecimal eurPricePerUnit) {
        this.eurPricePerUnit = eurPricePerUnit;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "CryptoTransaction{" +
                "id=" + id +
                ", walletId=" + walletId +
                ", type=" + type +
                ", cryptoSymbol='" + cryptoSymbol + '\'' +
                ", cryptoAmount=" + cryptoAmount +
                ", eurAmount=" + eurAmount +
                ", eurPricePerUnit=" + eurPricePerUnit +
                ", fee=" + fee +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}
