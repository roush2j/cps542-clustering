public class BCubed {

    public final double precision;

    public final double recall;

    public final int    tupCnt;

    public BCubed(Clustering truth, Clustering cl) {
        this.tupCnt = Math.min(truth.tupleCount(), cl.tupleCount());

        // build confusion matrix
        final int trcnt = truth.clusterCount() + 1;
        final int clcnt = cl.clusterCount() + 1;
        int[] mat = new int[trcnt * clcnt];
        for (int i = 0; i < tupCnt; i++) {
            int l = truth.clusterID(i);
            int c = cl.clusterID(i);
            if (c >= clcnt || l >= trcnt) System.out.println(c + ">?" + clcnt + " " + l + ">?" + trcnt);
            mat[c + l * clcnt]++;   // truth l and clustering c
        }

        // precision
        double prec = 0;
        for (int c = 0; c < clcnt; c++) {
            long clPrec = 0;
            long clPop = -1;
            for (int l = 0; l < trcnt; l++) {
                long count = mat[c + l * clcnt];
                clPop += count;
                clPrec += (count - 1) * count;
            }
            if (clPop > 0) prec += clPrec / (double) clPop;
        }
        this.precision = prec / tupCnt;

        // recall
        double recall = 0;
        for (int l = 0; l < trcnt; l++) {
            long trRecall = 0;
            long trPop = -1;
            for (int c = 0; c < clcnt; c++) {
                long count = mat[c + l * clcnt];
                trPop += count;
                trRecall += (count - 1) * count;
            }
            if (trPop > 0) recall += trRecall / (double) trPop;
        }
        this.recall = recall / tupCnt;
    }
}
