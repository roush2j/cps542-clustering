import java.util.Arrays;
import java.util.Random;

public class kMedoids extends Clustering {

    private final int      clusterCnt;

    private final int[]    _medoids;

    private final double[] _tmperrs;

    private final int[]    mapping;

    private final int[]    _mappingx1, _mappingx2;

    private final String   desc;

    public kMedoids(DataSet data, int clusterCnt, int[] centers) {
        super(data);
        final int clusc = this.clusterCnt = clusterCnt, tupc = data.tupleCnt;
        assert (centers.length >= clusc);
        this.desc = String.format("%s(clusterCnt=%d)", getClass()
                .getSimpleName(), clusc);

        final int[] medoids = this._medoids = Arrays.copyOf(centers, clusc);
        final int[] mapping = this.mapping = new int[tupc];
        this._tmperrs = new double[clusc];
        this._mappingx1 = new int[tupc];
        this._mappingx2 = new int[tupc];

        // initialize mappings
        for (int t = 0; t < tupc; t++) {
            double minD2 = Double.POSITIVE_INFINITY;
            for (int c = 0; c < clusc; c++) {
                double d2 = data.distSq(t, medoids[c]);
                if (d2 >= minD2) continue;
                minD2 = d2;
                mapping[t] = c;
            }
        }

        // For each iteration, we find a cluster c and non-medoid q such that
        // replacing medoids[c] -> q yields the greatest reduction in total error.
        while (true) {
            int swapq = -1, swapc = -1;
            double swapErr = 0;
            int[] swapmap = this._mappingx1;
            double[] tmperrs = this._tmperrs;
            int[] tmpmap = this._mappingx2;

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
                //  tmpmap[t]   is a code for the new cluster ID (see below)
                Arrays.fill(tmperrs, 0);
                for (int t = 0; t < tupc; t++) {
                    int closest = mapping[t];
                    double closestD2 = data.distSq(t, medoids[closest]);
                    double swapD2 = data.distSq(t, q);
                    if (swapD2 < closestD2) {

                        // q is closer than current closest medoid
                        // swapping q<->c will reduce the error for *any* c
                        double derr = Math.sqrt(swapD2) - Math.sqrt(closestD2);
                        for (int c = 0; c < clusc; c++) {
                            tmperrs[c] += derr;
                        }
                        // new clusterID is whatever q gets swapped with 
                        tmpmap[t] = -1;

                    } else {

                        // q is farther than current closest medoid
                        // find second-closest medoid
                        int secondClosest = -1;
                        double secondClosestD2 = Double.POSITIVE_INFINITY;
                        for (int c = 0; c < clusc; c++) {
                            if (c == closest) continue;
                            double d2 = data.distSq(t, medoids[c]);
                            if (d2 >= secondClosestD2) continue;
                            secondClosestD2 = d2;
                            secondClosest = c;
                        }
                        // error will be increased if we swap q <-> closest medoid
                        double d = Math.sqrt(closestD2);
                        if (swapD2 < secondClosestD2) {
                            // q is closer than second closest medoid
                            // new clusterID is always same as old cluster ID
                            tmperrs[closest] += Math.sqrt(swapD2) - d;
                            tmpmap[t] = closest;
                        } else {
                            // q is farther than second closest medoid
                            // new clusterID is old cluster ID or second-closest
                            // cluster ID, depending on what q replaces
                            tmperrs[closest] += Math.sqrt(secondClosestD2) - d;
                            tmpmap[t] = secondClosest;
                        }
                    }
                }

                // check if there is any current cluster c such that
                // swapping medoids[c] -> q beats the current best swap
                for (int c = 0; c < clusc; c++) {
                    if (tmperrs[c] >= swapErr) continue;
                    swapErr = tmperrs[c];
                    swapq = q;
                    swapc = c;
                    int[] t = swapmap;
                    swapmap = tmpmap;
                    tmpmap = t;
                }
            }

            // perform swap
            if (swapErr >= 0) {
                // If no swap is found that reduces total error, 
                // the algorithm has terminated
                break;
            }
            medoids[swapc] = swapq;

            // update mapping
            for (int t = 0; t < tupc; t++) {
                if (swapmap[t] < 0) {
                    // closest medoid is now q
                    mapping[t] = swapc;
                } else if (swapc == mapping[t]) {
                    // we've replaced previous closest medoid
                    mapping[t] = swapmap[t];
                } else {
                    // no change in closest medoid
                }
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
