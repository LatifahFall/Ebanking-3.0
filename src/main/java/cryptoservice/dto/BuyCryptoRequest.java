// /workspaces/Ebanking-3.0/src/main/java/cryptoservice/dto/BuyCryptoRequest.java
package cryptoservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class BuyCryptoRequest {

    @NotBlank(message = "Symbol is required")
    private String symbol;

    @NotNull(message = "EUR amount is required")
    @DecimalMin(value = "10.00", message = "Minimum EUR amount is 10.00")
    private BigDecimal eurAmount;

    // Constructors
    public BuyCryptoRequest() {
    }

    public BuyCryptoRequest(String symbol, BigDecimal eurAmount) {
        this.symbol = symbol;
        this.eurAmount = eurAmount;
    }

    // Getters and Setters
    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public BigDecimal getEurAmount() {
        return eurAmount;
    }

    public void setEurAmount(BigDecimal eurAmount) {
        this.eurAmount = eurAmount;
    }
}