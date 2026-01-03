package cryptoservice.model;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum SupportedCryptoCoins {

    BITCOIN("bitcoin", "BTC", "Bitcoin"),
    ETHEREUM("ethereum", "ETH", "Ethereum"),
    TETHER("tether", "USDT", "Tether"),
    XRP("ripple", "XRP", "XRP"),
    BNB("binancecoin", "BNB", "BNB"),
    USDC("usd-coin", "USDC", "USD Coin"),
    SOLANA("solana", "SOL", "Solana"),
    STETH("staked-ether", "STETH", "Lido Staked Ether"),
    TRON("tron", "TRX", "TRON"),
    DOGECOIN("dogecoin", "DOGE", "Dogecoin"),
    FIGURE_HELOC("figure-heloc", "FIGR", "Figure Heloc"),
    CARDANO("cardano", "ADA", "Cardano"),
    WSTETH("wrapped-steth", "WSTETH", "Wrapped stETH"),
    WHITEBIT("whitebit", "WBT", "WhiteBIT Coin"),
    BITCOIN_CASH("bitcoin-cash", "BCH", "Bitcoin Cash"),
    WRAPPED_BITCOIN("wrapped-bitcoin", "WBTC", "Wrapped Bitcoin"),
    WBETH("wrapped-beacon-eth", "WBETH", "Wrapped Beacon ETH"),
    WEETH("wrapped-eeth", "WEETH", "Wrapped eETH"),
    CHAINLINK("chainlink", "LINK", "Chainlink"),
    USDS("usds", "USDS", "USDS");

    private final String coingeckoId;
    private final String symbol;
    private final String displayName;

    SupportedCryptoCoins(String coingeckoId, String symbol, String displayName) {
        this.coingeckoId = coingeckoId;
        this.symbol = symbol;
        this.displayName = displayName;
    }

    public String getCoingeckoId() {
        return coingeckoId;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** Useful for building API queries */
    public static String coingeckoIdsCsv() {
        return Stream.of(values())
                .map(SupportedCryptoCoins::getCoingeckoId)
                .collect(Collectors.joining(","));
    }

    /** Validation helper */
    public static boolean isSupported(String symbol) {
        return Stream.of(values())
                .anyMatch(c -> c.symbol.equalsIgnoreCase(symbol));
    }
}

/* Example usage:
String ids = SupportedCryptoCoins.coingeckoIdsCsv();

//full details
String url =
    "https://api.coingecko.com/api/v3/coins/markets?vs_currency=eur&ids=" + ids;

//just prices
String url2 =
    "https://api.coingecko.com/api/v3/simple/price" +
    "?ids=" + ids +
    "&vs_currencies=eur";

*/