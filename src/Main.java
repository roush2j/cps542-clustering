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
            return new kMedoids(data, clCnt, rand);
        });

        if (args.length == 0 || args[0].equalsIgnoreCase("perf")) {
            double maxPerfTime = 1.0;
            if (args.length > 1) maxPerfTime = Double.parseDouble(args[1]);
            tests.perfTests(maxPerfTime);
        }
        if (args.length == 0 || args[0].equalsIgnoreCase("quality")) {
            tests.qualityTests();
        }
        if (args.length == 0 || args[0].equalsIgnoreCase("testdata")) {
            int ptCnt = 1000;
            if (args.length > 1) ptCnt = Integer.parseInt(args[1]);
            tests.commonDataTest(ptCnt, 4);
        }
    }
}
