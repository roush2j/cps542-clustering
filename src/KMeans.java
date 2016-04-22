import java.util.Random;

public class KMeans extends Clustering {

    private final int[]    mapping;

    private final double[] newCenters;

    private final int[]    newCounts;

    private final int      clusterCnt;

    private final String   desc;

    public KMeans(DataSet data, int clusterCnt, double[] centers) {
        super(data);
        final int attrc = data.attrCnt, tupc = data.tupleCnt;
        assert (centers.length >= clusterCnt * attrc);
        this.desc = String.format("%s(clusterCnt=%d)", getClass()
                .getSimpleName(), clusterCnt);

        this.clusterCnt = clusterCnt;
        this.mapping = new int[tupc];
        this.newCenters = new double[clusterCnt * attrc];
        this.newCounts = new int[clusterCnt];
        for (boolean changed = true; changed;) {
            changed = false;

            // assign tuples to clusters and calculate new centers
            for (int t = 0; t < tupc; t++) {
                double min_d2 = Double.POSITIVE_INFINITY;
                int min_c = 0;
                for (int c = 0; c < clusterCnt; c++) {
                    double d2 = data.distSq(t, centers, attrc * c);
                    if (d2 >= min_d2) continue;
                    min_d2 = d2;
                    min_c = c;
                }
                if (mapping[t] != min_c) {
                    changed = true;
                    mapping[t] = min_c;
                }
                for (int k = 0; k < attrc; k++) {
                    newCenters[min_c * attrc + k] += data.getAttr(t, k);
                }
                newCounts[min_c]++;
            }

            // calculate new cluster centers
            for (int c = 0; c < clusterCnt; c++) {
                final int off = c * attrc;
                for (int k = 0; k < attrc; k++) {
                    centers[off + k] = newCenters[off + k] / newCounts[c];
                    newCenters[off + k] = 0;
                }
                newCounts[c] = 0;
            }
        }
    }

    public KMeans(DataSet data, int clusterCnt, Random rand) {
        this(data, clusterCnt, initCenters(data, clusterCnt, rand));
    }

    /** Find random starting points for clusters */
    public static double[] initCenters(DataSet data, int clCnt, Random rand) {
        final int attrc = data.attrCnt;
        double[] centers = new double[attrc * clCnt];
        for (int c = 0; c < clCnt; c++) {
            int t = rand.nextInt(data.tupleCnt);
            System.arraycopy(data.flatData, t * attrc, centers, c * attrc,
                    attrc);
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
