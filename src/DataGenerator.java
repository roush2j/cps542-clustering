package semesterProject;

import java.io.*;
import java.util.*;

public class DataGenerator {
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
		if (noiseThreshold < 1) {
			throw new IllegalArgumentException();
		}
		
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
}