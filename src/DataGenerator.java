
import java.io.*;
import java.util.*;

public class DataGenerator {
    public static void test() {
        final int dims = 2, clcnt = 10, ptcnt = 10, clns = 1;
        final double cldist = 3.0, clrad = 1.0;

        double[][] centers = DataGenerator.generateCenters(clcnt, dims, cldist,
                2 * cldist);
        double[][] points = DataGenerator.generatePoints(centers, clrad, ptcnt,
                clns);

        try {
            DataGenerator.writeToFile(points, "datagen.out", "\t", true);
            new PrintStream("datagen.meta").format("set title '"
                    + "%s: Random Clusters (%dD, centers min %.2f - max %.2f, "
                    + "%dpts @rad=%.2f, noise=%d)'", DataGenerator.class, dims,
                    cldist, 2 * cldist, ptcnt, clrad, clns);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
	/**
	 * Creates a collection of n-dimensional points that are normally distributed around the 
	 * specified cluster centers within a specified distance of the cluster center.
	 * @param centers The centers around which to cluster the points.
	 * @param radius The maximum distance from each point to its cluster center.
	 * @param pointCount The number of points contained in each cluster.
	 * @param noiseThreshold The standard deviation value that indicates whether a point should
	 * be considered a cluster point or a noise point.
	 * @return An array containing the following values:  the cluster number to which the point
	 * belongs, 0.0 if the point is a cluster point or 1.0 if the point is a noise point, plus
	 * a random value for each dimension of the n-dimensional space.
	 */
	public static double[][] generatePoints(double[][] centers, double radius, double pointCount, int noiseThreshold) {
		if (noiseThreshold < 1) throw new IllegalArgumentException();
		
		ArrayList<double[]> points = new ArrayList<>();
		Random rng = new Random();
		double maxRandom = Double.MAX_VALUE / radius * (double)noiseThreshold;
		double minRandom = maxRandom * -1;
		
		for (int centerIndex = 0; centerIndex < centers.length; centerIndex++) {
			double[] center = centers[centerIndex];
			
			for (int pointNumber = 0; pointNumber < pointCount; pointNumber++) {
				double[] values = new double[center.length + 2];
				boolean noisePoint = false;
				
				// First two elements are for cluster ID and noise flag.
				for (int valueIndex = 2; valueIndex < values.length; valueIndex++) {
					double random = rng.nextGaussian();
					
					// Make sure random number isn't an edge case that will cause an exception.
					if (random < minRandom || random > maxRandom) {
						System.out.println("Out of range.");
						pointNumber--;
					} else {
						values[valueIndex] = center[valueIndex - 2] + (random / noiseThreshold * radius);
						
						if (random >= noiseThreshold) {
							noisePoint = true;
							pointNumber--; // Create a new cluster point to replace this one.
						}
					}
				}
				
				values[0] = centerIndex + 1;
				values[1] = noisePoint ? 1 : 0;
				points.add(values);
			}
		}

		return points.toArray(new double[1][1]);
	}
	
	/**
	 * Writes the specified points to the specified file.
	 * @param points The points to write to the file.
	 * @param filePath The file's path.
	 * @param delimiter The character to use to separate the values.
	 * @param includeMetaData Indicates whether the point's cluster and noise status should be
	 * written to the file.
	 * @throws Exception If the points cannot be written to the file.
	 */
	public static void writeToFile(double[][] points, String filePath, String delimiter, boolean includeMetaData) throws Exception {
		if (points.length > 0) {
			PrintWriter writer = new PrintWriter(new File(filePath));
			
			// Write the header.
			if (includeMetaData) {
				writer.write("Cluster" + delimiter + "Noise" + delimiter);
			}
			
			for (int i = 2; i < points[0].length; i++) {
				writer.write("V" + (i - 1));
				if (i < points[0].length - 1) writer.write(delimiter);
				else writer.write(System.lineSeparator());
			}
			
			// Write each point.
			for (double[] point : points) {
				if (includeMetaData) {
					writer.write(String.format("%d%s%d%s", (int)point[0], delimiter, (int)point[1], delimiter));
				}
				
				for (int i = 2; i < point.length; i++) {
					writer.write(String.format("%f", point[i]));
					
					if (i < point.length - 1) writer.write(delimiter);
					else writer.write(System.lineSeparator());
				}
			}
			
			writer.flush();
			writer.close();
		}
	}
	
	/**
	 * Generates the specified number of n-dimensional clusters with each cluster spaced within
	 * the specified distance range from its nearest neighbor.
	 * @param number The number of centers to generate.
	 * @param dimensions The number of dimensions in each point.
	 * @param minDistance The minimum distance from the nearest neighbor.
	 * @param maxDistance The maximum distance from the nearest neighbor.
	 * @return A collection of n-dimensional cluster center points.
	 */
	public static double[][] generateCenters(int number, int dimensions, double minDistance, double maxDistance) {
		ArrayList<double[]> centers = new ArrayList<>();
		double[] lastCenter = new double[dimensions];
		Random rng = new Random();
		
		for (int centerId = 0; centerId < number; centerId++) {
			double[] newCenter = new double[dimensions];
			
			for (int attribute = 0; attribute < dimensions; attribute++) {
				double delta = (1 - rng.nextDouble()) * (maxDistance - minDistance) + minDistance;
				
				lastCenter[attribute] += delta;
				newCenter[attribute] = lastCenter[attribute];
			}
			
			centers.add(newCenter);
		}
		
		return centers.toArray(new double[1][1]);
	}
	
	/**
	 * Normalizes a collection of points so that each point's attributes fall within the specified
	 * range.
	 * @param points The points to normalize.
	 * @param newMin The minimum value in the target range.
	 * @param newMax The maximum value in the target range.
	 */
	public static void normalizePoints(double[][] points, double newMin, double newMax) {
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		double newRange = newMax - newMin;
		
		for (double[] point : points) {
			for (double value : point) {
				if (value < min) min = value;
				if (value > max) max = value;
			}
		}
		
		for (double[] point : points) {
			for (int attrIdx = 0; attrIdx < point.length; attrIdx++) {
				point[attrIdx] = (point[attrIdx] - min) * newRange + newMin;
			}
		}
	}
}