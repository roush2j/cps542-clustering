import java.util.Arrays;
import java.util.Random;

public class DBSCAN extends Clustering {

    private final int[]  mapping;

    private final int[]  tupqueue;

    private final int    clusterCnt;

    private final String desc;

    public DBSCAN(DataSet data, double eps, int minPts) {
        super(data);
        this.mapping = new int[data.tupleCnt];
        this.tupqueue = new int[data.tupleCnt];
        this.clusterCnt = dbscan(data, eps, minPts);
        this.desc = String.format("%s(eps=%.3f, minpts=%d)", getClass()
                .getSimpleName(), eps, minPts);
    }

    public DBSCAN(DataSet data, Random rng) {
        super(data);
        this.mapping = new int[data.tupleCnt];
        this.tupqueue = new int[data.tupleCnt];

        final int maxn = 30;
        final int tupx = Math.min(data.tupleCnt, 100);
        final int samp = Math.min(data.tupleCnt, maxn * 20);

        double[] avgdist = new double[maxn];
        double[] dist = new double[maxn];
        for (int i = 0; i < tupx; i++) {
            Arrays.fill(dist, Double.POSITIVE_INFINITY);
            final int t = rng.nextInt(data.tupleCnt);
            for (int j = 0; j < samp; j++) {
                final int q = rng.nextInt(data.tupleCnt);
                double d2 = data.distSq(t, q);
                int ins = Arrays.binarySearch(dist, d2);
                if (ins < 0) ins = -ins - 1;
                if (ins >= maxn) continue;
                System.arraycopy(dist, ins, dist, ins + 1, maxn - ins - 1);
                dist[ins] = d2;
            }
            for (int k = 0; k < maxn; k++) {
                avgdist[k] += Math.sqrt(dist[k]);
            }
        }

        double maxacc = Double.NEGATIVE_INFINITY;
        int maxk = 0;
        for (int k = 2; k < maxn - 1; k++) {
            // second-order finite difference of avgDist to k nearest neighbors
            double acc = avgdist[k - 1] - 2 * avgdist[k] + avgdist[k + 1];
            if (acc > maxacc) {
                maxacc = acc;
                maxk = k;
            }
        }
        if (Double.isInfinite(maxacc)) {
            // sane defaults in case of failure
            this.clusterCnt = dbscan(data, 0.5, 10);
            this.desc = String.format("%s(auto FAILED)", getClass()
                    .getSimpleName());
        } else {
            double eps = avgdist[maxk] / tupx;
            int minpts = (int) Math.ceil(maxk * 0.15 * data.tupleCnt / tupx);
            this.clusterCnt = dbscan(data, eps, minpts);
            this.desc = String.format("%s(auto eps=%.3f, minpts=%d)",
                    getClass().getSimpleName(), eps, minpts);
        }
    }

    private int dbscan(DataSet data, double eps, int minPts) {
        final int tupc = data.tupleCnt;
        final double epssq = eps * eps;
        assert (mapping.length >= tupc);
        assert (tupqueue.length >= tupc);

        int clustercnt = 0;
        for (int i = 0; i < tupc; i++) {
            if (mapping[i] != 0) continue;

            int qbot = 0, qtop = 1;
            tupqueue[0] = i;
            mapping[i] = ++clustercnt;
            while (qbot < qtop) {
                // pop next point off of queue
                int j = tupqueue[qbot++];

                // find all neighbors of this point
                int qneigh = qtop, dupNeighbors = 0;
                for (int k = 0; k < tupc; k++) {
                    if (mapping[k] == 0 && data.distSq(j, k) <= epssq) {
                        // newly reachable neighbor
                        tupqueue[qtop++] = k;
                        mapping[k] = clustercnt;
                    } else if (mapping[k] == clustercnt
                            && data.distSq(j, k) <= epssq) {
                        // previously reached neighbor
                        dupNeighbors++;
                    }
                }
                if (dupNeighbors + (qtop - qneigh) < minPts) {
                    // not enough neighbors to be a core point, 
                    // drop neighbors from queue
                    for (int q = qneigh; q < qtop; q++) {
                        mapping[tupqueue[q]] = 0;
                    }
                    qtop = qneigh;
                }
            }

            // clusters can only be seeded by core points
            if (qtop < minPts) {
                for (int q = 0; q < qtop; q++) {
                    mapping[tupqueue[q]] = 0;
                }
                clustercnt--;
            }
        }

        return clustercnt;
    }

    @Override public int tupleCount() {
        return mapping.length;
    }

    @Override public int clusterCount() {
        return clusterCnt;
    }

    @Override public int clusterID(int tuple) {
        return mapping[tuple];
    }

    @Override public String toString() {
        return desc;
    }
}
