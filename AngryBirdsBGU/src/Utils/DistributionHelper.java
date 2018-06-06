package Utils;

public class DistributionHelper {
    public static final double[] BucketBoundaries = {0, 0, 0.2, 0.45, 0.65, 0.8, 1};

    public static double getProbabilityAboveRelativeScore(double[] bucketsProbability, double relativeScore) {
        double complementProbability = 0;
        for (int i = 0; i < BucketBoundaries.length - 1; i++) {
            if (BucketBoundaries[i + 1] <= relativeScore) {
                complementProbability += bucketsProbability[i];
            } else {
                double bucketSize = BucketBoundaries[i + 1] - BucketBoundaries[i];
                double belowScoreBucketSize = relativeScore - BucketBoundaries[i];
                complementProbability += (belowScoreBucketSize / bucketSize) * bucketsProbability[i];
                return 1 - complementProbability;
            }
        }

        return 0;
    }

    public static double getExpectationAboveRelativeScore(double[] bucketsProbability, double relativeScore, double probabilityAboveRelativeScore) {
        double expectation = 0;
        double bucketSize;
        double aboveScoreBucketSize;
        double bucketMedian;
        for (int i = 0; i < BucketBoundaries.length - 1; i++) {
            if (BucketBoundaries[i + 1] <= relativeScore) {
                continue;
            }

            if(inBucket(i, relativeScore)){
                bucketSize = BucketBoundaries[i + 1] - BucketBoundaries[i];
                aboveScoreBucketSize = BucketBoundaries[i + 1] - relativeScore;
                bucketMedian = (relativeScore + BucketBoundaries[i + 1]) / 2;
                expectation += bucketMedian * (aboveScoreBucketSize / bucketSize) * (bucketsProbability[i] / probabilityAboveRelativeScore);
                continue;
            }

            bucketMedian = (BucketBoundaries[i + 1] + BucketBoundaries[i]) / 2;
            expectation += bucketMedian * (bucketsProbability[i] / probabilityAboveRelativeScore);
        }

        return expectation;
    }

    private static boolean inBucket(int bucket, double relativeScore) {
        return relativeScore >= BucketBoundaries[bucket] && relativeScore <= BucketBoundaries[bucket + 1];
    }
}
