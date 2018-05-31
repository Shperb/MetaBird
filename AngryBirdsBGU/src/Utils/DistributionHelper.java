package Utils;

public class DistributionHelper {
    public static final double[] BucketBoundaries = {0, 0, 0.2, 0.45, 0.65, 0.8, 1};

    public static double getBucketsExpectation(double[] bucketsProbability) {
        double expectation = 0;
        for(int i = 0; i < BucketBoundaries.length - 1; i++){
            expectation += (BucketBoundaries[i + 1] - BucketBoundaries[i]) * bucketsProbability[i];
        }

        return expectation;
    }
}
