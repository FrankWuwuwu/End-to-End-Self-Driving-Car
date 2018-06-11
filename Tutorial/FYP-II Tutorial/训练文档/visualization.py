import argparse
import base64
from datetime import datetime
import csv
import os
import shutil
import numpy as np
from PIL import Image
from flask import Flask
from io import BytesIO
from keras.models import load_model
import h5py
from keras import __version__ as keras_version
from quiver_engine import server


model = None


parser = argparse.ArgumentParser(description='Remote Driving')
parser.add_argument(
    'model',
    type=str,
    help='Path to model h5 file. Model should be on the same path.'
)
parser.add_argument(
    'image_folder',
    type=str,
    nargs='?',
    default='',
    help='Path to image folder. This is where the images from the run will be saved.'
)
args = parser.parse_args()
#print(args)
# check that model Keras version is same as local Keras version
f = h5py.File(args.model, mode='r')
model_version = f.attrs.get('keras_version')
keras_version = str(keras_version).encode('utf8')

if model_version != keras_version:
    print('You are using Keras version ', keras_version,
          ', but the model was built using ', model_version)

model = load_model(args.model)

server.launch(model,temp_folder='features',input_folder='img',port=5000)
