package strategy;

public class Player {
    private Strategy strategy;
    private String name;

    public Player(String name,Strategy strategy) {
        this.name = name;
        this.strategy = strategy;

    }
    public Hand nextHand(){
        return strategy.nextHand();
    }
}
