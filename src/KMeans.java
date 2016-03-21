public class KMeans extends Clustering {

    private final int[]    mapping;

    private final double[] newCenters;

    private final int[]    newCounts;

    private final int      clusterCnt;

    public KMeans(DataSet data, int clusterCnt, double[] centers) {
        super(data);
        final int attrc = data.attrCnt, tupc = data.tupleCnt;
        
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
                    newCenters[min_c * attrc + k] += data.flatData[attrc * t + k];
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

    @Override public int clusterCount() {
        return clusterCnt;
    }

    @Override public int clusterID(int tuple) {
        return mapping[tuple];
    }
}
