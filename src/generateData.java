import java.io.FileWriter;
import java.util.Random;
import java.io.IOException;
import java.io.PrintWriter;

public class generateData 
{
	/**
	 * Generate's "random" points from min-max that are in a non-spherical shape
	 * @param maxRange defines the max value to use
	 * @param minRange defines the min value to use
	 * @param numAttributes number of attributes that need to be generated
	 * @param numTuples number of tuples that need to be generated
	 * @return a matrix of the data
	 */
	public double[][] generate(int maxRange, int minRange, int numAttributes, int numTuples, double noiseProb)
	{
		//data structure to hold the generated points
		double[][] points = new double[numTuples][numAttributes];
		//Object used to create random numbers
		Random random = new Random();
		//points must lay with this range
		int totalRange = maxRange - minRange;
		//for the number of tuples
		for(int i=0; i<numTuples; i++)
		{
			//for the number of attributes
			for(int j=0; j<numAttributes; j++)
			{
				//generate data
				if(noise(random, noiseProb))
					points[i][j] = pointGenerator(random, totalRange);
				else
					points[i][j] = noiseGenerator(random, totalRange);
			}
		}
		return points;
	}
	
	/**
	 * writes the non-spherical data points to file for further analysis
	 * @param points set of data points
	 * @param includeMetaData boolean of whether or not to include metadata
	 * @param numAttributes the number of attributes required
	 * @param numTuples the number of tuples required
	 */
	public void writeToFile(double[][] points, boolean includeMetaData, int numAttributes, int numTuples)
	{
		try {
			//handle to write output to a csv file
			PrintWriter writer = new PrintWriter(new FileWriter("nonSpherical.csv"));
			//write meta data to file if required
			if(includeMetaData)
			{
				writer.append("Data Point");
				for(int i=0; i<numAttributes; i++)
				{
					writer.append(",");
					writer.append("Attribute" + i);
				}
				writer.append("\n");
			}
			//write the data to the file
			for(int i=0; i<numTuples; i++)
			{
				writer.append(String.valueOf(i));
				writer.append(",");
				for(int j=0; j<numAttributes; j++)
				{
					writer.append(String.valueOf(points[i][j]));
					writer.append(",");
				}
				
				if(i+1 == numTuples)
				{
					writer.append("\n");
				}
				else
				{
					writer.write(System.lineSeparator());
				}
			}
			writer.flush();
			writer.close();
			System.out.println("Complete");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * method to generate random points
	 * utilizes sin to generate random number
	 * @param r represents random number object
	 * @param range for use with normalizing the data to a specific range
	 * @return range * Math.sin(r.nextDouble())
	 */
	private static double pointGenerator(Random r, int range)
	{
		return range * Math.sin(r.nextDouble());
	}
	
	/**
	 * determines if point generated should be noise or an actual point
	 * @param r
	 * @param probability
	 * @return
	 */
	private static boolean noise(Random r, double probability)
	{
		double x = r.nextDouble();
		if(x <= probability)
			return true;
		else
			return false;
	}
	
	private static double noiseGenerator(Random r, int range)
	{
		return (range * 100) * r.nextDouble();
	}

}
