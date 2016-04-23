import java.util.Arrays;
import java.util.Random;

public class kMedoids extends Clustering {

    private final int        clusterCnt;

    private final int[]      _medoids;

    private final double[]   _tmperrs;

    private final double[][] _dists;

    private final int[]      mapping;

    private final String     desc;

    public kMedoids(DataSet data, int clusterCnt, int[] centers) {
        super(data);
        final int clusc = this.clusterCnt = clusterCnt, tupc = data.tupleCnt;
        assert (centers.length >= clusc);
        this.desc = String.format("%s(clusterCnt=%d)", getClass()
                .getSimpleName(), clusc);

        final int[] medoids = this._medoids = Arrays.copyOf(centers, clusc);
        this._tmperrs = new double[clusc];

        // initialize distance matrix
        final double[][] dists = this._dists = new double[clusc + 2][tupc];
        for (int t = 0; t < tupc; t++) {
            for (int c = 0; c < clusc; c++) {
                dists[c][t] = data.dist(medoids[c], t);
            }
        }

        // For each iteration, we find a cluster c and non-medoid q such that
        // replacing medoids[c] -> q yields the greatest reduction in total error.
        while (true) {
            int swapq = -1, swapc = -1;
            double swapErr = 0;
            double[] swapDist = this._dists[clusc];

            // loop over each non-medoid tuple and find the one with
            // largest benefit when swapping with one of the medoids
            for (int q = 0; q < tupc; q++) {

                // reject q if it is already a medoid
                boolean isMedoid = false;
                for (int c = 0; !isMedoid && c < clusc; c++) {
                    isMedoid = (medoids[c] == q);
                }
                if (isMedoid) continue;

                // loop over each tuple to calculate the benefit
                // of swapping medoids[c] -> q for each cluster c
                //  tmperrs[c]  is running sum of change-in-err
                //  tmpdist[t]  is distance from t to q
                double[] tmperrs = this._tmperrs;
                Arrays.fill(tmperrs, 0);
                double[] tmpdist = this._dists[clusc + 1];
                for (int t = 0; t < tupc; t++) {

                    // find closest and second-closest cluster medoids
                    int closest = -1, secondClosest = -1;
                    double closestD = Double.POSITIVE_INFINITY;
                    double secondClosestD = closestD;
                    for (int c = 0; c < clusc; c++) {
                        double d = dists[c][t];
                        if (d < closestD) {
                            secondClosestD = closestD;
                            secondClosest = closest;
                            closestD = d;
                            closest = c;
                        } else if (d < secondClosestD) {
                            secondClosestD = d;
                            secondClosest = c;
                        }
                    }

                    // compare to proposed swap point q
                    double swapD = tmpdist[t] = data.dist(q, t);
                    if (swapD < closestD) {
                        // q is closer than current closest medoid
                        // swapping q<->c will reduce the error equally for all c
                        for (int c = 0; c < clusc; c++) {
                            tmperrs[c] += swapD - closestD;
                        }
                    } else {
                        // q is farther than current closest medoid
                        // error will be changed (increased) only if we 
                        // swap q <-> closest medoid
                        secondClosestD = Math.min(swapD, secondClosestD);
                        tmperrs[closest] += secondClosestD - closestD;
                    }
                }

                // check if there is any current cluster c such that
                // swapping medoids[c] -> q beats the current best swap
                for (int c = 0; c < clusc; c++) {
                    if (tmperrs[c] >= swapErr) continue;
                    swapErr = tmperrs[c];
                    swapq = q;
                    swapc = c;
                }
                if (swapq == q) {
                    double[] t = swapDist;
                    swapDist = this._dists[clusc] = tmpdist;
                    tmpdist = this._dists[clusc + 1] = t;
                }
            }

            // perform swap
            if (swapErr >= 0) {
                // If no swap is found that reduces total error, 
                // the algorithm has terminated
                break;
            }
            medoids[swapc] = swapq;
            double[] t = dists[swapc];
            dists[swapc] = swapDist;
            swapDist = this._dists[clusc] = t;
        }

        // calculate mapping
        final int[] mapping = this.mapping = new int[tupc];
        for (int t = 0; t < tupc; t++) {
            double minD = Double.POSITIVE_INFINITY;
            for (int c = 0; c < clusc; c++) {
                double d = dists[c][t];
                if (d >= minD) continue;
                minD = d;
                mapping[t] = c;
            }
        }
    }

    public kMedoids(DataSet data, int clusterCnt, Random rand) {
        this(data, clusterCnt, initCenters(data, clusterCnt, rand));
    }

    /** Find random starting points for clusters */
    public static int[] initCenters(DataSet data, int clCnt, Random rand) {
        int[] centers = new int[clCnt];
        for (int c = 0; c < clCnt; c++) {
            centers[c] = rand.nextInt(data.tupleCnt);
        }
        return centers;
    }

    @Override public int tupleCount() {
        return mapping.length;
    }

    @Override public int clusterCount() {
        return clusterCnt;
    }

    @Override public int clusterID(int tuple) {
        return mapping[tuple] + 1;
    }

    @Override public String toString() {
        return desc;
    }
}
