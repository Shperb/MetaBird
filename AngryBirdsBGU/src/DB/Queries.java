package DB;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import com.google.gson.JsonSyntaxException;

import DB.ValueExtractor.ValueExtractor;

public class Queries {
	
	public void getLevelsResults(Data pData) throws JsonSyntaxException, IOException {
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
	
	public ArrayList<String> getAllAgents(Data pData) {
		HashSet<String> levels = new HashSet<>();
		pData.games.forEach(game->{
			game.levels.forEach(level->{
				levels.add(level.agent);
			});
		});
		
		ArrayList<String> retVal = new ArrayList<>(levels);
		Collections.sort(retVal);
		
		return retVal ;
	}

	public void getSupportSize(Data pData, ValueExtractor pValueExtractor) {
		HashMap<String, HashMap<String, HashSet<Integer>>> results = new HashMap<>();
		getAllAgents(pData).forEach(agent->{
			results.put(agent, new HashMap<>());
			HashMap<String, HashSet<Integer>> agentResults = results.get(agent);
			getAllLevels(pData).forEach(level->{
				agentResults.put(level, new HashSet<>());
			});
		});

		pData.games.forEach(game->{
			game.levels.forEach(level->{
				results.get(level.agent).get(level.name).add(pValueExtractor.getValue(level));
			});
		});
		
		getAllAgents(pData).forEach(agent->{
			getAllLevels(pData).forEach(level->{
				System.out.println(agent + "\t" + level + "\t" + results.get(agent).get(level).size());
			});
		});
	}
}
