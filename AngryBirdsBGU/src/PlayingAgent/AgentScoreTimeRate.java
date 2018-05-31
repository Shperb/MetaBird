package PlayingAgent;

import java.util.Comparator;

public class AgentScoreTimeRate {
    private String level;

    private String agent;
    private double scoreTimeRate;
    public AgentScoreTimeRate(String level, String agent, double scoreTimeRate) {
        this.level = level;
        this.agent = agent;
        this.scoreTimeRate = scoreTimeRate;
    }

    public String getLevel() {
        return level;
    }

    public String getAgent() {
        return agent;
    }

    public double getScoreTimeRate() {
        return scoreTimeRate;
    }

    public static class AgentScoreTimeRateComparator implements Comparator<AgentScoreTimeRate>{
        @Override
        public int compare(AgentScoreTimeRate o1, AgentScoreTimeRate o2) {
            return (o1.getScoreTimeRate() - o2.getScoreTimeRate()) < 0 ? -1 : 1;
        }
    }
}
