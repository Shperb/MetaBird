package PlayingAgent;

import MetaAgent.MetaAgent;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;

public class RoundRobinAgent extends MetaAgent {
    private int currLevel = 0;
    private String[] levels = new String[]{"Level4-11", "Level4-4", "Level2-10", "Level14-14", "Level6-1", "Level7-5", "Level3-1", "Level1-5"};

    @Override
    protected String getAlgorithmName() {
        return null;
    }

    @Override
    protected String[] GetNewAgentAndLevel() throws Exception {
        String agent = getAgentsNames().get(0);
        String level = levels[currLevel];
        currLevel = (currLevel + 1) % levels.length;

        return new String[]{agent, level};
    }

    @Override
    protected boolean shouldStartNewGame() {
        return false;
    }

    @Override
    protected boolean shouldExit() {
        try {
            return getGame().getTimeElapsed() > getTimeConstraint();
        } catch (ParseException e) {
            return true;
        }
    }

    @Override
    protected ArrayList<String> getLevelsList() {
        return new ArrayList<String>(Arrays.asList(levels));
    }

    public RoundRobinAgent(int pTimeConstraint, String[] pAgents) {
        super(pTimeConstraint, pAgents, false);
    }
}
