#!/usr/bin/env python3
# license removed for brevity
import argparse
import base64
from datetime import datetime
import os
import shutil
import numpy as np
from PIL import Image
from io import BytesIO
from keras.models import load_model
import h5py
from keras import __version__ as keras_version
import rospy
from geometry_msgs.msg import Twist


def talker():
     i = 0
     images = []
     pub = rospy.Publisher('cmd_vel', Twist, queue_size=10)
     rospy.init_node('talker', anonymous=True)
     rate = rospy.Rate(30)
     f = h5py.File('/home/wukefu/catkin_ws/src/car_controll/scripts/model.h5', mode='r')
     #model_version = f.attrs.get('keras_version')
     #keras_version = str(keras_version).encode('utf8')
     #if model_version != keras_version:
     #    print('You are using Keras version ', keras_version,
     #          ', but the model was built using ', model_version)
     model = load_model('/home/wukefu/catkin_ws/src/car_controll/scripts/model.h5')
     while i==0:
       
       if not os.listdir("/home/wukefu/img"):
         rate.sleep()
       else:
         for file in os.listdir("/home/wukefu/img"):
           img =  '/home/wukefu/img/' + file
           #print(img)
           f = open(img, 'rb')
           image = base64.b64encode(f.read())
           f = 0
           
           try:
             picture = Image.open(BytesIO(base64.b64decode(image)))
             image_array = np.asarray(picture)
             #print(picture)
             #print(image_array.shape)
             az = float(model.predict(image_array[None, :, :, :],            batch_size=1))
             cmd = Twist()
             cmd.angular.z = az
             lx=float(0.2)
             cmd.linear.x = lx
             rospy.loginfo(cmd)
             pub.publish(cmd)
             os.remove(img)
             az = 0
           except:
             os.remove(img)
             
           
           #image_array=np.zeros(shape);
           #del image_array[:,:,:,:] 
     #rate = rospy.Rate(10) # 10hz
     #while not rospy.is_shutdown():
#     while i<len(images):
         #hello_str = "hello world %s" % rospy.get_time()
#         picture = Image.open(BytesIO(base64.b64decode(images[i])))
#         image_array = np.asarray(picture)
#         az = float(model.predict(image_array[None, :, :, :],            batch_size=1))
#         cmd = Twist()
#         cmd.angular.z = az
#         lx=float(0.5)
#         cmd.linear.x = lx
#         rospy.loginfo(cmd)
#         pub.publish(cmd)
#         i = i + 1
         #rate.sleep()

if __name__ == '__main__':
    try:
        talker()
    except rospy.ROSInterruptException:
        pass
