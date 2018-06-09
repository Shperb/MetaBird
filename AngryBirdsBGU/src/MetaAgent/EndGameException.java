package MetaAgent;

public class EndGameException extends RuntimeException{
    private final GameResult gameResult;

    public EndGameException(GameResult gameResult) {
        this.gameResult = gameResult;
    }

    public GameResult getGameResult() {
        return gameResult;
    }
}
