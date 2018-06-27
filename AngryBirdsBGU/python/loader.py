# Create first network with Keras
import json
import sys
from keras.models import load_model
import numpy

def load(filename):
        return load_model("models/" + filename)

class KerasPredict(object):

    modelnaive = load("kerasnaive.h5")
    modelihsev = load("kerasihsev.h5")
    modelplanA = load("kerasplanA.h5")
    modelAngryBER = load("kerasAngryBER.h5")

    def getmodel(self, x):
        return {
            'naive': self.modelnaive,
            'ihsev': self.modelihsev,
            'planA': self.modelplanA,
            'AngryBER': self.modelAngryBER,
        }.get(x, self.modelnaive)

    def predictWithModel (self, stringName, features):
        model = self.getmodel(stringName)
        features = numpy.array([features])
        prediction = model.predict(features)
        return prediction

predictor = KerasPredict()

s = sys.stdin.readline().strip()
while s not in ['break', 'quit']:
    agentAndFeatures = json.loads(s)
    agent = agentAndFeatures['agent']
    features = agentAndFeatures['features']
    prediction = predictor.predictWithModel(agent, features)[0]
    sys.stdout.write(json.dumps(prediction.tolist()) + '\n')
    sys.stdout.flush()
    s = sys.stdin.readline().strip()
