package Clock;

import java.util.Date;

public class ManualClock extends Clock {

	private long mTime = 0;


	@Override
	public Date getDate() {
		return new Date(mTime);
	}

	@Override
	public long getTime() {
		return mTime;
	}

	
	public void proceed(long pMilis) {
		mTime  += pMilis;
	}

}
