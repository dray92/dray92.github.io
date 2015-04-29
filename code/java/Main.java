import java.io.*;
import java.util.*;

public class Main {
	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("Processing first data");
		Scanner getLength = new Scanner(new FileReader("data.txt"));
		Scanner inFile = new Scanner(new FileReader("data.txt"));
		int count = 0;
		while(getLength.hasNext()) {
			getLength.next();
			count++;
		}
		System.out.println(count);
		int[] accel = new int[count/2];
		int[] gyro = new int[count/2];
		String accelXs, accelYs, accelZs;

		String gyroXs, gyroYs, gyroZs;
		int i = 0;
		while(inFile.hasNext()) {
			accelXs = inFile.next();
			accel[i] = Integer.parseInt(accelXs);
			if (inFile.hasNext()) {
				accelYs = inFile.next();
				accel[i+1] = Integer.parseInt(accelYs);
			}
			if (inFile.hasNext()) {
				accelZs = inFile.next();
				accel[i+2] = Integer.parseInt(accelZs);
			}
			if (inFile.hasNext()) {
				gyroXs = inFile.next();
				gyro[i] = Integer.parseInt(gyroXs);
			}
			
			if (inFile.hasNext()) {
				gyroYs = inFile.next();
				gyro[i+1] = Integer.parseInt(gyroYs);
			}
			if (inFile.hasNext()) {
				gyroZs = inFile.next();
				gyro[i+2] = Integer.parseInt(gyroZs);
			}
		
			i+=3;
		}
		System.out.println(Arrays.toString(accel));

		System.out.println("Processing second data");
		Scanner getLength2 = new Scanner(new FileReader("data2.txt"));
		Scanner inFile2 = new Scanner(new FileReader("data2.txt"));
		int count2 = 0;
		while(getLength2.hasNext()) {
			getLength2.next();
			count2++;
		}
		System.out.println(count2);
		int[] accel2 = new int[count2/2];
		int[] gyro2 = new int[count2/2];
		String accelXs2, accelYs2, accelZs2;

		String gyroXs2, gyroYs2, gyroZs2;
		int i2 = 0;
		while(inFile2.hasNext()) {
			accelXs2 = inFile2.next();
			accel2[i2] = Integer.parseInt(accelXs2);
			if (inFile2.hasNext()) {
				accelYs2 = inFile2.next();
				accel2[i2+1] = Integer.parseInt(accelYs2);
			}
			if (inFile2.hasNext()) {
				accelZs2 = inFile2.next();
				accel2[i2+2] = Integer.parseInt(accelZs2);
			}
		
			if (inFile2.hasNext()) {
				gyroXs2 = inFile2.next();
				gyro2[i2] = Integer.parseInt(gyroXs2);
			}
			
			if (inFile2.hasNext()) {
				gyroYs2 = inFile2.next();
				gyro2[i2+1] = Integer.parseInt(gyroYs2);
			}
			if (inFile2.hasNext()) {
				gyroZs2 = inFile2.next();
				gyro2[i2+2] = Integer.parseInt(gyroZs2);
			}
		
			i2+=3;
		}
		System.out.println(Arrays.toString(accel2));
		
		
		double max = Math.pow(2, 16);
		System.out.println(max);
		int datapts = 0;
		if (count < count2) {
			datapts = count/2;
		} else {
			datapts = count2/2;
		}
		
		System.out.println(datapts);
		double sum = 0.0;
		
		for (int j = 0; j < datapts; j++) {
			sum += Math.pow((accel[j] - accel2[j])/max,2);
			sum += Math.pow((gyro[j]-gyro2[j])/max, 2);
			
		}
		
		double average = Math.sqrt(sum);
		System.out.println(average);
		
	}
	
}