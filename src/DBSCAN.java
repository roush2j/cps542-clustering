public class DBSCAN extends Clustering {

    private final int[]  mapping;

    private final int[]  tupqueue;

    private final int    clusterCnt;

    private final String desc;

    public DBSCAN(DataSet data, double eps, int minPts) {
        super(data);
        final int tupc = data.tupleCnt;
        final double epssq = eps * eps;
        this.desc = String.format("%s(eps=%.3f, minpts=%d)", getClass()
                .getSimpleName(), eps, minPts);

        this.mapping = new int[tupc];
        this.tupqueue = new int[tupc];
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

        this.clusterCnt = clustercnt;
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
