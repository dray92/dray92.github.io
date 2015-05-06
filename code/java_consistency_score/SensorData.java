
public class SensorData {
	public int accelX, accelY, accelZ;
	public int gyroX, gyroY, gyroZ;
	
	
	public SensorData(int accelX, int accelY, int accelZ, int gyroX, int gyroY, int gyroZ) {
		this.accelX = accelX;
		this.accelY = accelY;
		this.accelZ = accelZ;
		this.gyroX = gyroX;
		this.gyroY = gyroY;
		this.gyroZ = gyroZ;
	}
	public String toString() {
		return this.accelX + ", " + this.accelY + ", " + this.accelZ + ", " + this.gyroX + ", " + this.gyroY + ", " + this.gyroZ;
		
	}
}
