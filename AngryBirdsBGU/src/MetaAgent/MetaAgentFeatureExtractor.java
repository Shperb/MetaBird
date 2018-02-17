package MetaAgent;

import java.util.ArrayList;

public class MetaAgentFeatureExtractor extends MetaAgent {
    @Override
    protected String getAlgorithmName() {
        return null;
    }

    @Override
    protected String[] GetNewAgentAndLevel() throws Exception {
        return new String[0];
    }

    @Override
    protected boolean shouldStartNewGame() {
        return false;
    }

    @Override
    protected boolean shouldExit() {
        return false;
    }

    @Override
    protected ArrayList<String> getLevelsList() {
        return null;
    }

    public MetaAgentFeatureExtractor(int pTimeConstraint, String[] pAgents) {
        super(pTimeConstraint, pAgents);
    }
}
