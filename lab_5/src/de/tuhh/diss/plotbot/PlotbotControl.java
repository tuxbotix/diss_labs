package de.tuhh.diss.plotbot;
import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.LightSensor;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.TouchSensor;
import lejos.util.Delay;

/**
 * This Class is used for control of the plotting robot. A great amount of time should spend for controlling the robot.
 * Add a suitable constructor and add further methods you need for driving the motors, evaluating the sensors etc.
 * 
 * Hint: Create an instance of PlotBotControl inside the class Plotbot and  * pass the PlotBotControl when calling the
 * method plot of the selected shape.
 */
public class PlotbotControl {
	
	
	private LightSensor lightSensor;
	private TouchSensor limitSwitch;
	private NXTRegulatedMotor wheelMotor;
	private NXTRegulatedMotor armMotor;
	private NXTRegulatedMotor penMotor;
	
	private int swivelArmMaxRange; 
	
	private int globalXCoord;
	private int globalYCoord;
	
	private final int LIGHT_THRESHOLD = 500;
	
	public PlotbotControl(){
		lightSensor= new LightSensor(SensorPort.S3,true);
		wheelMotor= Motor.C;
		armMotor=Motor.A;
		penMotor=Motor.B;
		limitSwitch= new TouchSensor(SensorPort.S1);
		swivelArmMaxRange=0;
	}
	
	public boolean calibrationRoutine(){
		LCD.drawString("Press Enter", 0, 0);
		LCD.drawString("to Start Calib.", 0, 1);
		Button.ENTER.waitForPressAndRelease();
		
		return calibrateArm();
	}
	private boolean calibrateArm(){
		LCD.drawString("Starting arm calibration", 0, 0);
		LCD.clear(1);
		
		LCD.drawString("Starting arm swing", 0, 1);
		
//		armMotor.resetTachoCount();
		int swivelArmInitialPos =armMotor.getPosition();
		
		while(!limitSwitch.isPressed()){	
//			LCD.drawString("Swinging the arm", 0,1);
			armMotor.backward();
		}
		armMotor.stop();
		swivelArmMaxRange = Math.abs(armMotor.getPosition() - swivelArmInitialPos) *2;
		
		LCD.drawString("Max Range "+swivelArmMaxRange, 0,0);
		
//		int rotatePolarity = Math.abs(armMotor.getPosition() - swivelArmInitialPos)/(armMotor.getPosition() - swivelArmInitialPos);
		armMotor.rotate((swivelArmMaxRange)/2);
		
//		LCD.clear();
		LCD.drawString("Going to initial Pos", 0,1);
		
		wheelMotor.backward();
		//LCD.drawInt(lightSensor.getNormalizedLightValue(), 4, 0, 1);
		
		while(lightSensor.getNormalizedLightValue() > LIGHT_THRESHOLD){	
			LCD.drawInt(lightSensor.getNormalizedLightValue(), 4, 0, 1);
		}
		
		Motor.C.stop();
		
		/**
		 * Arm swing calibration
		 * Assume the arm is straight on start
		 */
		
		LCD.clear();
		
//		//now bring the robot front so that thelight sensor is at starting edge.
		wheelMotor.forward();
		//LCD.drawInt(lightSensor.getNormalizedLightValue(), 4, 0, 1);
		int previousValue= lightSensor.getNormalizedLightValue();
		while(!(previousValue < LIGHT_THRESHOLD && lightSensor.getNormalizedLightValue() > LIGHT_THRESHOLD)){	
			LCD.drawInt(lightSensor.getNormalizedLightValue(), 4, 0, 1);
		}
		
		Motor.C.stop();
//		
		globalXCoord=0;
		globalYCoord=0;

		LCD.drawString("Calibration Done", 0,1);
		return true;
	}
	private void moveArmRightPos(int position){
		
	}
}
