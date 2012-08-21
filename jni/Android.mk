LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := libprotobuf
LOCAL_SRC_FILES := libprotobuf.so

include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE := opencv_nonfree
LOCAL_SRC_FILES := libopencv_nonfree.a

include $(PREBUILT_STATIC_LIBRARY)


#include protobuf/Android.mk #comment all other lines and run ndk to recompile protobuf native share library

include $(CLEAR_VARS)
#LOCAL_PATH := $(call my-dir)
LOCAL_PATH := /home/fede/workspace2/SfMpipeline/jni

OPENCV_CAMERA_MODULES:=off

OPENCV_MK_PATH:=/home/fede/apps/opencvAndroid-2.4.2/sdk/native/jni/OpenCV.mk
#OPENCV_MK_PATH:=/home/fede/AndroidSDK/OpenCV-2.4.1-android/OpenCV-2.4.1/share/opencv/OpenCV.mk

include $(OPENCV_MK_PATH)

LOCAL_MODULE    := pipeline_native
LOCAL_SRC_FILES := frame.cpp data.pb.cpp Matcher.cpp matches.pb.cpp SfMmatcher_wrap.cpp
LOCAL_CFLAGS    := -frtti -g
LOCAL_STATIC_LIBRARIES := opencv_nonfree
LOCAL_LDLIBS +=  -llog -ldl jni/libprotobuf.so
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../protobuf/src
APP_OPTIM = debug

include $(BUILD_SHARED_LIBRARY)
