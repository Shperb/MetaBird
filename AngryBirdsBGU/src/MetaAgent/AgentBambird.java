package MetaAgent;
import java.io.IOException;

import ab.vision.GameStateExtractor.GameState;

public class AgentBambird extends Agent{
	
	private boolean mShouldReturnLoading;

	public AgentBambird(String pExecutableFileName, MetaAgent pMetaAgent) {
		super(pExecutableFileName, pMetaAgent);
	}

//	@Override
//	protected byte getState() throws IOException {
//		byte retVal;
//		if(mShouldReturnLoading) {
//			retVal = (byte) GameState.LOADING.ordinal();
//			mShouldReturnLoading = false;
//		}
//		else {
//			retVal = super.getState();
//		}
//		return retVal;		
//	}
	
	@Override
	public void loadLevel() {
		mShouldReturnLoading = true;
	}
}
