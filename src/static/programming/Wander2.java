package vagnerr;
import robocode.*;
import java.awt.Color;
import java.lang.*;


/**
 * Test1 - a robot by (your name here)
 */
public class Wander2 extends AdvancedRobot
{
	/**
	 * run: Test1's default behavior
	 */
	boolean targeted = false;
	public void run() {
		// After trying out your robot, try uncommenting the import at the top,
		// and the next line:
		setColors(Color.red,Color.blue,Color.green);
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);
		
		addCustomEvent(
			new Condition("triggergunscold"){
				public boolean test(){
					return (getGunHeat() == 0 && targeted && getGunTurnRemaining() ==0);
				};
			}
		);
				
		while(true) {
//			setTurnLeft(180);
//			setMaxVelocity(5);
//			setTurnRadarRight(36000);
			back(100);
			turnRadarRight(360);
			turnLeft(45);
			//execute();
		}
	}

	public void onWin(WinEvent e){
		for (int i = 0; i< 50; i++)
		{
			turnRight(30);
			turnGunLeft(30);
		}
	}
	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		if(targeted) return;	// we are busy firing at something else
		targeted = true;
		
		double angleB = getHeading() + e.getBearing() - e.getHeading() - 180.0;
		
		//the constantin this equation should be 11 (the speed of a bullet) but for some reason 16
		//seems a little more accurate. Go figure :-}
		double angleA = Math.toDegrees(Math.asin( (e.getVelocity() * Math.sin(Math.toRadians(angleB)) ) / 16.0 ));
		double turnangle = getRelAngle( e.getBearing() + angleA + getHeading() - getGunHeading() );

				turnGunRight(turnangle);
		setTurnRadarRight(90);
		//fire(3);
		scan();
	}

	public double getRelAngle(double angle){
		if (angle > -180 && angle <=  180)
			return angle;
		double newangle = angle;
		while(newangle <= -180)
			newangle +=360;
		while (newangle > 180)
			newangle -= 360;
		return newangle;
	}
	/**
	 * onHitByBullet: What to do when you're hit by a bullet
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		//turnLeft(90 - e.getBearing());
	}
	public void onCustomEvent(CustomEvent e)
	{
		// If our custom event "triggerhit" went off,
		if (e.getCondition().getName().equals("triggergunscold"))
		{
			fire(3);
			targeted = false;
		}
	}
}
