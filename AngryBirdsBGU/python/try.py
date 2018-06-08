# Create first network with Keras
from keras.models import Sequential
from keras.layers import Dense
from keras.layers import Dropout
from keras.optimizers import Adam
import os
import numpy
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '2'


def get_model(outputsize):
    model = Sequential()
    model.add(Dense(256, activation='sigmoid', input_shape=(28,)))
    model.add(Dropout(0.3))
    model.add(Dense(128, activation='tanh'))
    model.add(Dropout(0.5))
    model.add(Dense(outputsize, activation='softmax'))
    model.compile(optimizer='Adam',
                  loss='categorical_crossentropy',
                  metrics=['accuracy'])
    return model


# fix random seed for reproducibility
# load pima indians dataset
X = numpy.loadtxt("compiledFeatures.csv", delimiter=",")
Y = numpy.loadtxt("planA_compiledData.csv", delimiter=",")

#
# # create model
# model = Sequential()
# model.add(Dense(256, input_dim=28,, activation='tanh'))
# model.add(Dense(512, init='normal', activation='sigmoid'))
# model.add(Dense(6, init='normal', activation='softmax'))
#
#

# Compile model
model = get_model(6);
# Fit the model
model.fit(X, Y, nb_epoch=700,  validation_split = 0.15)
# calculate predictions
predictions = model.predict(X)

numpy.savetxt("results.csv", predictions, delimiter=",")

model.save('keras1modelplanA.h5')