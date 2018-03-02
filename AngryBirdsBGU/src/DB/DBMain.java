package DB;

import Clock.Clock;
import Clock.SystemClock;
import MetaAgent.Constants;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class DBMain {

    public static void main(String[] args) throws IOException {
        // filterData();
        // filterLevels();
        return;
    }

    private static void filterLevels() throws IOException {
        File folder = new File(Constants.levelsDir);
        File[] listOfFiles = folder.listFiles();

        for (File file:
             listOfFiles) {
            String jsonTxt = DBHandler.readFile(file.toPath(), StandardCharsets.UTF_8);
            if(jsonTxt.contains("BIRD_GREEN") || jsonTxt.contains("BIRD_REDBIG")){
                System.out.println(file.getName());
                file.delete();
            }
        }
    }

    private static void filterData() throws IOException {
        Clock.setClock(new SystemClock());
        Data data = DBHandler.loadData();
        Data filteredData = new Data();

        File folder = new File(Constants.levelsDir);
        File[] listOfFiles = folder.listFiles();
        List<String> levels = Arrays.stream(listOfFiles)
                .map(f -> f.toPath().getFileName().toString().replace(".json", ""))
                .collect(toList());

        data.games.forEach(g -> {
            ArrayList<String> filteredLevels = getFilteredLevels(g, levels);
            if (!filteredLevels.isEmpty()) {
                Game game = new Game(g.algorithm, g.timeConstraint);
                game.agents = g.agents;
                game.levelNames = filteredLevels;
                game.levels = new Collection<>();

                g.levels.stream()
                        .filter(l ->
                                filteredLevels.contains(l.name) &&
                                        (l.state == LevelState.won || l.state == LevelState.lost)
                        )
                        .forEach(l -> game.levels.add(l));

                if(!game.levels.isEmpty()){
                    filteredData.games.add(game);
                }
            }
        });

        saveNewData(filteredData);
        return;
    }

    private static ArrayList<String> getFilteredLevels(Game g, List<String> levels) {
        ArrayList<String> filteredLevels = new ArrayList<>();
        if(g.levelNames == null){
            return filteredLevels;
        }
        g.levelNames.forEach(level -> {
            if (levels.contains(level)) {
                filteredLevels.add(level);
            }
        });

        return filteredLevels;
    }

    private static void saveNewData(Data data) throws IOException {
        String json = new Gson().toJson(data);
        Files.write(DBHandler.getFile("filteredDataAvinoam.json").toPath(), json.getBytes());
    }
}
