import com.trade.repository.TradeRepository;
import com.trade.model.Trade;

public class Main {
    public static void main(String[] args) {
        TradeRepository repo = new TradeRepository();
        
        // Creating a sample trade (e.g., Buying 10 shares of RELIANCE at 2500)
        // Note: portfolioId 1 and strategyId 1 must exist in your DB
        Trade myTrade = new Trade("RELIANCE", "BUY", 10, 2500.00, 1);
        
        repo.saveTrade(myTrade, 1);
    }
}