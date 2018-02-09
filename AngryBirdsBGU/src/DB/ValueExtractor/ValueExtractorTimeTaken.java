package DB.ValueExtractor;

import DB.Level;

public class ValueExtractorTimeTaken extends ValueExtractor {

	@Override
	public int getValue(Level pLevel) {
		return pLevel.getTimeTaken();
	}
}