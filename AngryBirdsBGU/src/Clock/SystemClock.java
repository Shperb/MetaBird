package Clock;

import java.util.Date;

public class SystemClock extends Clock {

	@Override
	public Date getDate() {
		return new Date();
	}

	@Override
	public long getTime() {
		return System.currentTimeMillis();
	}

}
