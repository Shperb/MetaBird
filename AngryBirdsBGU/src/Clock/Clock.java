package Clock;

import java.util.Date;

public abstract class Clock {

	public abstract Date getDate();

	public abstract long getTime();
	
	static Clock mClock = null;
	
	public static void setClock(Clock pClock) {
		mClock = pClock;
	}
	
	public static Clock getClock() {
		return mClock;
	}
}
