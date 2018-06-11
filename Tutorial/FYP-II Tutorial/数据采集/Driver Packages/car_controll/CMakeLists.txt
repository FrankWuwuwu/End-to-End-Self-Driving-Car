cmake_minimum_required(VERSION 2.8.3)
project(car_controll)

find_package(catkin REQUIRED COMPONENTS
  roscpp
  rospy
  std_msgs
  sensor_msgs
  cv_bridge
  dynamic_reconfigure
  message_generation
  tf
  rosbag
)

generate_messages()

catkin_package(CATKIN_DEPENDS roscpp tf message_runtime)

include_directories(${catkin_INCLUDE_DIRS})

add_executable(sync src/sync.cpp)
add_dependencies(sync ${${PROJECT_NAME}_EXPORTED_TARGETS} ${catkin_EXPORTED_TARGETS} ${PROJECT_NAME}_gencfg)
target_link_libraries(sync ${catkin_LIBRARIES})

add_executable(drive src/drive.cpp)
add_dependencies(drive ${${PROJECT_NAME}_EXPORTED_TARGETS} ${catkin_EXPORTED_TARGETS} ${PROJECT_NAME}_gencfg)
target_link_libraries(drive ${catkin_LIBRARIES})

