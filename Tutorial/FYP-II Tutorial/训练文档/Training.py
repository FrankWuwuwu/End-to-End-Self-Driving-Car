# this is the nvidia network model
import csv
import cv2
import os
import numpy as np

lines = []
with open('2018-05-17-15-40-15.csv') as csvfile:
    reader = csv.reader(csvfile)
    for line in reader:
        lines.append(line)

images = []
measurements = []


for file in os.listdir("/media/yongsheng/SSD SOFTWARE/data/combine/img"):
    print(file)
    img =  '/media/yongsheng/SSD SOFTWARE/data/combine/img/' + file
    image = cv2.imread(img)
    images.append(image)


for line in lines:				
    measurement = float(line[53])
    measurements.append(measurement)

X_train = np.array(images)
y_train = np.array(measurements)
images = []
measurements = []
print(X_train.shape)

#y_train = y_train.reshape(1, 442)
print(y_train.shape)

from keras.models import Sequential, Model
from keras.layers import Flatten, Dense, Lambda,Cropping2D,Dropout, ELU
from keras.layers.convolutional import Conv2D
from keras.layers.pooling import MaxPooling2D

model = Sequential()
model.add(Lambda(lambda x: x/255.0, input_shape=(240,320,3)))
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

model.fit(X_train, y_train, epochs = 20, validation_split = 0.2, shuffle=True, initial_epoch = 0)

model.save('model.h5')
