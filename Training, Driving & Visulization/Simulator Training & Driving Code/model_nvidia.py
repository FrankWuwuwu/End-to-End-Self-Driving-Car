# this is the nvidia network model
import csv
import cv2
import numpy as np

lines = []
with open('../Map2/driving_log.csv') as csvfile:
    reader = csv.reader(csvfile)
    for line in reader:
        lines.append(line)

images = []
measurements = []
steering_correction = 0.2
for line in lines:
    for i in range(3): 				#i = 0:read center image, i = 1 read left image, i = 2 read right image
        source_path = line[i]
        filename = source_path.split('\\')[-1]
        current_path =  filename
        #current_path = '../Driving-Data/IMG/' + filename
        #print(current_path)
        image = cv2.imread(current_path)
        images.append(image)
        measurement = float(line[3])
        if (i==1):
            measurement = measurement + steering_correction
        if (i==2):
            measurement = measurement - steering_correction
        measurements.append(measurement)

X_train = np.array(images)
y_train = np.array(measurements)
#print(X_train)
#print(y_train)

from keras.models import Sequential, Model
from keras.layers import Flatten, Dense, Lambda,Cropping2D,Dropout
from keras.layers.convolutional import Conv2D
from keras.layers.pooling import MaxPooling2D

model = Sequential()
model.add(Lambda(lambda x: x/255.0, input_shape=(160,320,3)))
model.add(Cropping2D(cropping=((70,25),(0,0))))
model.add(Conv2D(24, (5, 5), activation="relu", strides=(2, 2)))
model.add(Dropout(0.5))
model.add(Conv2D(36, (5, 5), activation="relu", strides=(2, 2)))
model.add(Conv2D(48, (5, 5), activation="relu", strides=(2, 2)))
model.add(Conv2D(64, (3, 3), activation="relu"))
model.add(Conv2D(64, (3, 3), activation="relu"))
model.add(Flatten())
model.add(Dense(100))
model.add(Dense(50))
model.add(Dense(10))
model.add(Dense(1))

model.compile(loss='mse',optimizer='adam')
model.fit(X_train, y_train, epochs = 8, validation_split = 0.2, shuffle=True, initial_epoch = 0)

model.save('model.h5')
