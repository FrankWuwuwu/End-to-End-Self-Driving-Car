# this is the nvidia network model
import csv
import cv2
import os
import numpy as np

lines = []
with open('CSV/2017_11_03_10_23_49.csv') as csvfile:
    reader = csv.reader(csvfile)
    for line in reader:
        lines.append(line)

images = []
measurements = []


for file in os.listdir("/media/yongsheng/MEDIA/11.3Data/image"):
    print(file)
    img =  '/media/yongsheng/MEDIA/11.3Data/image/' + file
    image = cv2.imread(img)
    images.append(image)




for line in lines:				
    measurement = float(line[2])
    measurements.append(measurement)

X_train = np.array(images)
y_train = np.array(measurements)
print(X_train)
print(y_train)

from keras.models import Sequential, Model
from keras.layers import Flatten, Dense, Lambda,Cropping2D,Dropout
from keras.layers.convolutional import Conv2D
from keras.layers.pooling import MaxPooling2D

model = Sequential()
model.add(Lambda(lambda x: x/255.0, input_shape=(360,540,3)))   #Give Tensorflow experssion
model.add(Cropping2D(cropping=((70,25),(0,0))))                 #Resize the input
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
model.fit(X_train, y_train, epochs = 10, validation_split = 0.2, shuffle=True, initial_epoch = 0)

model.save('Real-Model.h5')
