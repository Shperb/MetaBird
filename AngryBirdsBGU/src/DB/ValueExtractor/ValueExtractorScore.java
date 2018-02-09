package DB.ValueExtractor;

import DB.Level;

public class ValueExtractorScore extends ValueExtractor {
	@Override
	public int getValue(Level pLevel) {
		return pLevel.score;
	}
}