import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {

        AlgorithmTests tests = new AlgorithmTests();
        tests.addAlgorithm("kmeans", (data, rand, clCnt) -> {
            return new KMeans(data, clCnt, rand);
        });
        tests.addAlgorithm("dbscan", (data, rand, clCnt) -> {
            return new DBSCAN(data, rand);
        });
        tests.addAlgorithm("cmeans", (data, rand, clCnt) -> {
            // cheap hack - the cmeans implementation will just use the
            // first clCnt tuples as the initial centers
            return new C_means2(data, clCnt, data.flatData);
        });
        tests.addAlgorithm("kmedoids", (data, rand, clCnt) -> {
            return new kMedoids(data, clCnt);
        });

        //tests.perfTests();
        //tests.qualityTests();
        tests.commonDataTest();
    }
}
