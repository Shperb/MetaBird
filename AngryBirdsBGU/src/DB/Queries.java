package DB;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import com.google.gson.JsonSyntaxException;

public class Queries {
	
	public void getLevelResults(Data pData) throws JsonSyntaxException, IOException {
		HashMap<String, Integer> total = new HashMap<>();
		HashMap<String, Integer> finished = new HashMap<>();
		HashMap<String, Integer> won = new HashMap<>();
		
		getAllLevels(pData).forEach(level->{
			total.put(level, 0);
			finished.put(level, 0);
			won.put(level, 0);
		});
		
		pData.games.forEach(game->{
			game.levels.forEach(level->{
				total.put(level.name, total.get(level.name) + 1); 
				if (level.isFinished()) {
					finished.put(level.name, finished.get(level.name) + 1); 
				}
				if (level.state == LevelState.won) {
					won.put(level.name, won.get(level.name) + 1); 
				}
			});
		});
		
		getAllLevels(pData).forEach(level->{
			System.out.println(level + "\t" + total.get(level) + "\t" + finished.get(level) + "\t" + won.get(level));
		});

	}
	
	public ArrayList<String> getAllLevels(Data pData) {
		HashSet<String> levels = new HashSet<>();
		pData.games.forEach(game->{
			game.levels.forEach(level->{
				levels.add(level.name);
			});
		});
		
		ArrayList<String> retVal = new ArrayList<>(levels);
		Collections.sort(retVal);
		
		return retVal ;
	}
}
