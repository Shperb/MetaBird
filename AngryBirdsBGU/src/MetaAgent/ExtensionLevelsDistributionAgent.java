package MetaAgent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ExtensionLevelsDistributionAgent extends MetaAgentDistributionSampling{
    private static final String levelsPath = "C:\\temp\\ABoffline\\Load custom Angry Birds levels\\Installing custom levels\\Past competition levels\\";


    public ExtensionLevelsDistributionAgent(int pTimeConstraint, String[] pAgents) {
        super(pTimeConstraint, pAgents);
    }

    @Override
    protected ArrayList<String> getLevelsList() {
        try {
            List<String> pathStream = Files.find(
                    Paths.get(levelsPath),
                    999,
                    (p, bfa) -> bfa.isRegularFile())
                    .map(p -> {
                        return p.toAbsolutePath().toString().replace(levelsPath, "");
                    })
                    .filter(l -> isRequired(l))
                    .collect(Collectors.toList());
            return new ArrayList<>(pathStream.subList(0, Math.min(pathStream.size(), 19)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }
}
