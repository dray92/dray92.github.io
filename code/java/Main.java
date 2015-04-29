import java.io.*;
import java.util.*;

public class Main {
	public static void main(String[] args) throws FileNotFoundException {
		
		SensorData[] data1 = getData("data.txt");
		System.out.println(Arrays.toString(data1));
		SensorData[] data2 = getData("data2.txt");
		System.out.println(Arrays.toString(data2));
		
		

		
		double max = Math.pow(2, 16);
		System.out.println("Max: " + max);
		int datapts = 0;
		if (data1.length < data2.length) {
			datapts = data1.length;
		} else {
			datapts = data2.length;
		}
		
		System.out.println("Size of comparisons: " + datapts);
		double sum = 0.0;
		
		for (int j = 0; j < datapts; j++) {
			sum += Math.pow((data1[j].accelX - data2[j].accelX)/max, 2);
			sum += Math.pow((data1[j].accelY - data2[j].accelY)/max, 2);
			sum += Math.pow((data1[j].accelZ - data2[j].accelZ)/max, 2);
			sum += Math.pow((data1[j].gyroX -data2[j].gyroX)/max, 2);
			sum += Math.pow((data1[j].gyroY -data2[j].gyroY)/max, 2);
			sum += Math.pow((data1[j].gyroZ -data2[j].gyroZ)/max, 2);
			
		}
		
		double average = Math.sqrt(sum);
		System.out.println(average);
		
	}
	
	public static SensorData[] getData(String datafile) throws FileNotFoundException {
		System.out.println("Processing data:");
		
		
		
		Scanner getLength = new Scanner(new FileReader(datafile));
		Scanner inFile = new Scanner(new FileReader(datafile));
		int count = 0;
		while(getLength.hasNextInt()) {
			getLength.nextInt();
			count++;
		}
		
		
		
		System.out.println("Data samples: " + count/6);
		SensorData[] dataSet = new SensorData[count/6];
		
		
		int accelXs = 0;
		int accelYs = 0;
		int accelZs = 0;
		int gyroXs = 0;
		int gyroYs = 0;
		int gyroZs = 0;
		int i = 0;
		while(inFile.hasNextInt()) {
			accelXs = inFile.nextInt();
			
			if (inFile.hasNextInt()) {
				accelYs = inFile.nextInt();
				
			}
			if (inFile.hasNextInt()) {
				accelZs = inFile.nextInt();
		
			}
			if (inFile.hasNextInt()) {
				gyroXs = inFile.nextInt();
		
			}
			
			if (inFile.hasNextInt()) {
				gyroYs = inFile.nextInt();
			
			}
			if (inFile.hasNextInt()) {
				gyroZs = inFile.nextInt();
				
			}
			SensorData d = new SensorData(accelXs,accelYs,accelZs,gyroXs,gyroYs, gyroZs);
			dataSet[i] = d;
			i+=1;
			
		}
		
		return dataSet;

	}
	
}