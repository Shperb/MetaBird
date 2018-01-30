package DB;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import MetaAgent.MyLogger;

public class Event {
	String beginTime = null;
	String endTime = null;
	
	public Event() {
		Date date = new Date();
		beginTime = getSimpleDateFormat().format(date);
	}
	
	public void setEndTime() {
		Date date = new Date();
		endTime = getSimpleDateFormat().format(date);
	}
	
	SimpleDateFormat getSimpleDateFormat() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}

	public long getTimeElapsed() throws ParseException {
		long begin = getSimpleDateFormat().parse(beginTime).getTime();
		long now = new Date().getTime();
		return (now - begin) / 1000;
	}
	
	public Integer getTimeTaken() {
		Integer retVal = null;
		try {
			long begin = getSimpleDateFormat().parse(beginTime).getTime();
			long end = endTime !=null? getSimpleDateFormat().parse(endTime).getTime() : 0;
			retVal = (int) ((end - begin) / 1000);
		} catch (ParseException e) {
			e.printStackTrace();
			MyLogger.log(e);
		}
		retVal = Math.max(retVal, 1);// avoid stack overflow in the dynamic programming algorithm
		return retVal;
	}
	
	public long getBeginTime() throws ParseException {
		long begin = getSimpleDateFormat().parse(beginTime).getTime();
		return begin;		
	}

	public Long getEndTime() throws ParseException {
		if (endTime == null) {
			return null;
		}
		else {
			long end = getSimpleDateFormat().parse(endTime).getTime();
			return end;
		}
	}
	
	public boolean isFinished() {
		return endTime != null;
	}
}
