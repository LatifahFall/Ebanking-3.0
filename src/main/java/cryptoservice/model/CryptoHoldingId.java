package cryptoservice.model;

import java.io.Serializable;
import java.util.Objects;

public class CryptoHoldingId implements Serializable {
    private Long walletId;
    private String cryptoSymbol;

    public CryptoHoldingId() {
    }

    public CryptoHoldingId(Long walletId, String cryptoSymbol) {
        this.walletId = walletId;
        this.cryptoSymbol = cryptoSymbol;
    }

    public Long getWalletId() {
        return walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }

    public String getCryptoSymbol() {
        return cryptoSymbol;
    }

    public void setCryptoSymbol(String cryptoSymbol) {
        this.cryptoSymbol = cryptoSymbol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CryptoHoldingId that = (CryptoHoldingId) o;
        return Objects.equals(walletId, that.walletId) && Objects.equals(cryptoSymbol, that.cryptoSymbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(walletId, cryptoSymbol);
    }
}
