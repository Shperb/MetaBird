package PlayingAgent;

import DB.Features;
import Utils.JsonUtils;
import org.json.simple.JSONObject;

import java.io.*;

public class ScorePredictionModel {
    private static final String pythonLoaderPath = "loader.py";
    private static final String cmd = "\\venv\\Scripts\\python.exe " + pythonLoaderPath;
    private static ScorePredictionModel instance;

    private BufferedReader inp;
    private BufferedWriter out;

    static {
        try {
            instance = new ScorePredictionModel();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ScorePredictionModel() throws IOException {
        Process pythonProcess = startPythonScript();
        inp = new BufferedReader(new InputStreamReader(pythonProcess.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(pythonProcess.getOutputStream()));
    }

    public double[] predict(String agent, Features levelFeatures) {
        String kerasRequest = createKerasRequest(agent, levelFeatures);
        String kerasResponse = pipe(kerasRequest);

        return convertResponse(kerasResponse);
        // TODO: Take the features, parse into input vector and use the model to predict output vector
//        return new double[]{0.4, 0.1, 0.2, 0.1, 0.1, 0.1};
    }

    private double[] convertResponse(String kerasResponse) {
        return JsonUtils.parseToDoubleArray(kerasResponse);
    }

    private String createKerasRequest(String agent, Features levelFeatures) {
        JSONObject agentAndFeatures = new JSONObject();
        agentAndFeatures.put("agent", agent);
        agentAndFeatures.put("features", levelFeatures.getFeatureAsList());
        return agentAndFeatures.toJSONString();
    }

    private static Process startPythonScript() throws IOException {
        File workingDir = new File("./python");
        String fullCmd = workingDir.getAbsolutePath() + cmd;
        return Runtime.getRuntime().exec(fullCmd, null, workingDir);
    }

    public static ScorePredictionModel getInstance() {
        return instance;
    }

    private String pipe(String msg) {
        String ret;

        try {
            out.write(msg + "\n");
            out.flush();
            ret = inp.readLine();
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
