// /workspaces/Ebanking-3.0/src/main/java/cryptoservice/dto/SellCryptoRequest.java
package cryptoservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class SellCryptoRequest {

    @NotBlank(message = "Symbol is required")
    private String symbol;

    @NotNull(message = "Crypto amount is required")
    @DecimalMin(value = "0.0001", message = "Minimum crypto amount is 0.0001")
    private BigDecimal cryptoAmount;

    // Constructors
    public SellCryptoRequest() {
    }

    public SellCryptoRequest(String symbol, BigDecimal cryptoAmount) {
        this.symbol = symbol;
        this.cryptoAmount = cryptoAmount;
    }

    // Getters and Setters
    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public BigDecimal getCryptoAmount() {
        return cryptoAmount;
    }

    public void setCryptoAmount(BigDecimal cryptoAmount) {
        this.cryptoAmount = cryptoAmount;
    }
}