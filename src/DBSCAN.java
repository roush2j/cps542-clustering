public class DBSCAN extends Clustering {

    private final int[] mapping;
    
    private final int[] tupstack;

    private final int   clusterCnt;

    public DBSCAN(DataSet data, double eps, int minPts) {
        super(data);
        final int tupc = data.tupleCnt;
        final double epssq = eps * eps;
        
        this.mapping = new int[tupc];
        this.tupstack = new int[tupc];
        int clustercnt = 0;
        for (int i = 0; i < tupc; i++) {
            if (mapping[i] != 0) continue;
            
            tupstack[0] = i;
            int clpop = 0, stacktop = 1;
            int clid = clustercnt + 1;
            while (stacktop > 0) {
                int j = tupstack[--stacktop];
                mapping[j] = clid;
                
                // push all unvisited neighboring points onto the stack
                int neighborpop = 0;
                for (int k = 0; k < tupc; k++) {
                    if (mapping[k] == 0 && data.distSq(j, k) <= epssq) {
                        tupstack[stacktop++] = k;
                        neighborpop++;
                    } else if (mapping[k] == clid) {
                        neighborpop++;
                    }
                }

                // only include neighbors of core points
                if (neighborpop < minPts) stacktop -= neighborpop;
                else clpop += neighborpop;
            }
            
            // clusters can only be seeded by core points
            if (clpop < minPts) mapping[i] = 0;
            else clustercnt++;
        }
        
        this.clusterCnt = clustercnt;
    }

    @Override public int clusterCount() {
        return clusterCnt;
    }

    @Override public int clusterID(int tuple) {
        return mapping[tuple];
    }
}
