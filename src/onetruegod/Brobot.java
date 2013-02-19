package onetruegod;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;
import robocode.WinEvent;
import robocode.util.Utils;


public class Brobot extends AdvancedRobot {

	boolean reverse = false;
	double enemy; // enemy bearing in rads
	double eDistance; // enemy's distance from you
	double velocity; // enemy's bearing in rads
	double offset;
	private static ArrayList<Color> colorAr;
	private static Random r;
	private static int shotCount = 2;
	private int missedShot = 0;

	public void run() {
		setAdjustRadarForGunTurn(true);
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForRobotTurn(true);

		setTurnRadarRightRadians(40);
		initColorAr();

		while(true) {

			// The most important part of brobot.
			randColors();

			double gunTurn = getHeadingRadians() + enemy -
					getGunHeadingRadians();

			if(gunTurn > 6) { 
				gunTurn*=0.1;
			}

			initOffset();

			turnGunRightRadians(Utils.normalRelativeAngle(1*gunTurn+offset));

			/*
			 * The following helps in avoiding walls. Need to fix issue
			 * where robot rotates in wrong direction and sticking to the wall
			 * for too long under certain circumstances.
			 */
			if(getX() <= 50) {
				if((getHeadingRadians()>=3.14/2)&&(getHeadingRadians()<=1.5*3.14)) {
					reverse=true;
					setTurnRight(90);
				}
			}
			if(getX() >= getBattleFieldWidth()-50) { 
				if((getHeadingRadians()<=3.14/2)||(getHeadingRadians()>=1.5*3.14)) {
					reverse=true;
					setTurnRight(90);
				}
			}

			if(getY() >= getBattleFieldHeight()-50) { 
				reverse=true;
				setTurnRight(90);
			}

			if(getY() <= 100) {
				reverse=true;
				setTurnRight(50);
			}


			if(reverse){
				ahead(-100);
			} else {
				setAhead(100);
			}

			// The only movement code that isn't wall avoiding related. Yup.
			setTurnRight(enemy*2.5+3);

			reverse = false;
			execute();
		}
	}


	public void onScannedRobot(ScannedRobotEvent e) {

		double a = 0.1;
		enemy = e.getBearingRadians();


		double distance = e.getDistance();

		// Changes damage according to range, closer range = higher damage
		if (distance <100) {
			a= 3;
		} else if(distance < 400 || e.getVelocity() == 0){
			a=2;
		} else if (distance <= 700 && getEnergy() >= 50) {
			distance=700;
			a = (1-distance/700) * 3;
		} else {
			a=0.1;
		}


		// 
		double radarTurn = getHeadingRadians() + e.getBearingRadians()-
				getRadarHeadingRadians();

		setTurnRadarRightRadians(Utils.normalRelativeAngle(1*radarTurn));

		eDistance = distance;
		velocity=e.getVelocity();

		int i, j, k;
		i = 50;
		j = 25;
		k = 40;


		/*
		 * Controls firerate a different distances. Maxmium firerate
		 * when really fast or if target isn't moving (our targeting system
		 * is great for when targets aren't moving).
		 * This was essentially a last minute dirtycode quickfix to prevent
		 * our mediocre targeting system from wasting too much energy on missed shots
		 * at long range/against hard to hit opponents.
		 */
		if(distance < 300 || e.getVelocity() == 0) {
			setFire(a);
		}  else {
			if(missedShot >= 7) {
				i *= 2;
				j *= 2;
				k *= 2;
			}

			shotCount++;
			shotCount %= i;
			if(shotCount == j-1 && distance <= 500) {
				setFire(a);
				shotCount= 0 ;
			}

			if (shotCount == k && distance <= 700){
				setFire(a);
				shotCount= 0 ;
			}

			if (shotCount == i-1){
				setFire(a);
				shotCount= 0 ;
			}



		}
	}

	public void onHitWall(HitWallEvent e) {
		reverse=true;
		setTurnRight(90);
	}

	public void onBulletMissed(BulletMissedEvent e) {
		missedShot++;
	}

	public void onBulletHit(BulletHitEvent e) {
		missedShot /= 2;
	}

	public void onWin(WinEvent e) {
		turnRight(720);

	}


	// Provides very rough estimation for offset to attempt to hit opponent at long ranges
	private void initOffset(){
		double maxDistance = Math.sqrt(Math.pow(getBattleFieldWidth(),2) + Math.pow(getBattleFieldHeight(),2));

		if(eDistance < 100) { 
			offset = 0;
		} else if (eDistance <=700){
			if((getHeadingRadians()-enemy)<-(3.14/2) && (enemy-getHeadingRadians())>= (3.14/2)) {

				offset=eDistance / maxDistance*0.4;
			} else {
				offset=-eDistance / maxDistance*0.4;
			}
			offset*=velocity/8;
		}
		else {
			if((getHeadingRadians()-enemy)<-(3.14/2) && (enemy-getHeadingRadians())>= (3.14/2)) {
				offset = eDistance / maxDistance*0.4;
			} else {
				offset = -eDistance / maxDistance*0.4;
			}
			offset*=velocity/4;
		}
	}

	/*
	 * The random color generating stuff. pfahaha
	 */
	
	private void initColorAr() {
		colorAr = new ArrayList<Color>();
		colorAr.add(Color.black);
		colorAr.add(Color.green);
		colorAr.add(Color.red);
		colorAr.add(Color.blue);
		colorAr.add(Color.white);
		colorAr.add(Color.orange);
		colorAr.add(Color.pink);
		colorAr.add(Color.cyan);
		colorAr.add(Color.darkGray);
		colorAr.add(Color.gray);
		colorAr.add(Color.magenta);
		colorAr.add(Color.yellow);
		r = new Random();
	}

	private void randColors() {
		Color first = colorAr.get(r.nextInt(colorAr.size()));
		Color second = colorAr.get(r.nextInt(colorAr.size()));
		Color third = colorAr.get(r.nextInt(colorAr.size()));
		Color fourth = colorAr.get(r.nextInt(colorAr.size()));
		Color fifth = colorAr.get(r.nextInt(colorAr.size()));
		setColors(first, second, third, fourth, fifth);
	}
}


