#include "rosbag/bag.h"
#include <message_filters/subscriber.h>
#include <message_filters/synchronizer.h>
#include <message_filters/sync_policies/approximate_time.h>
#include <sensor_msgs/Image.h>
#include "image_transport/image_transport.h"
#include "ros/ros.h"
#include "nav_msgs/Odometry.h"
#include "ctime"
#include "time.h"
/*
ros::Publisher img_pub;
ros::Publisher Odom_pub;
ros::Publisher Kinect2camera_info_pub;*/
ros::Publisher pub_img;
ros::Publisher pub_odom;
rosbag::Bag bag_record;
using namespace std;
string int2string(int value)
{
    stringstream ss;
    ss<<value;
    return ss.str();
}

void callback(const sensor_msgs::ImageConstPtr& img_msg,
              const nav_msgs::OdometryConstPtr& odom_msg)
{
    ROS_INFO("Enter Publish");

    //bag_record.write("message_filter/usb_cam/image_raw",img_msg->header.stamp.now(),*img_msg);
    //bag_record.write("message_filter/wheel_odom",odom_msg->header.stamp.now(),*odom_msg);
    pub_img.publish(img_msg);
    pub_odom.publish(odom_msg);

}
int main(int argc, char** argv)
{
  ros::init(argc, argv, "message_filter_node");
  ros::Time::init();
  ros::NodeHandle nh;
  ROS_INFO("start message filter");
  time_t t=std::time(0);
  struct tm * now = std::localtime( & t );
  //string file_name;
  //the name of bag file is better to be determined by the system time
  //file_name="output-test.bag";
  //bag_record.open(file_name,rosbag::bagmode::Write);

  
  pub_img = nh.advertise<sensor_msgs::Image>("sync_img", 1);
  pub_odom = nh.advertise<nav_msgs::Odometry>("sync_odom", 1);

  message_filters::Subscriber<sensor_msgs::Image> img_sub(nh,"/usb_cam/image_raw" , 1);//订阅全向视觉Topic
  message_filters::Subscriber<nav_msgs::Odometry> odom_sub(nh,"/wheel_odom" , 1);//订阅里程计的Topic

  typedef message_filters::sync_policies::ApproximateTime<
          sensor_msgs::Image,
          nav_msgs::Odometry> MySyncPolicy;
  // ApproximateTime takes a queue size as its constructor argument, hence MySyncPolicy(10)
  message_filters::Synchronizer<MySyncPolicy> sync(MySyncPolicy(20),
                                                   img_sub,
                                                   odom_sub);
  sync.registerCallback(boost::bind(&callback, _1, _2));
  ros::spin();
  //bag_record.close();
  ROS_INFO("write finish");
  return 0;
}
