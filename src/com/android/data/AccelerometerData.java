package com.android.data;

public class AccelerometerData {
	private float accelerationX;
	private float accelerationY;
	private float accelerationZ;
	
	
	public AccelerometerData(){}
	
	public void setAccelerationX(float newX){
		accelerationX = newX;
	}
	
	public float getAccelerationX(){
		return accelerationX;
	}
	
	public void setAccelerationY(float newY){
		accelerationY = newY;
	}
	
	public float getAccelerationY(){
		return accelerationY;
	}
	
	public void setAccelerationZ(float newZ){
		accelerationZ = newZ;
	}
	
	public float getAccelerationZ(){
		return accelerationZ;
	}
	
}
