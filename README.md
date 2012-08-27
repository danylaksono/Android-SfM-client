Android-SfM-client
==================

Full documentation at https://wiki.cvg.ethz.ch/doku.php?id=sfm:android 


Application to leverage a Structure from Motion pipeline by extracting features and matching frames onboard using OpenCV 2.4.2. The data gathered along with gravity sensor data is broadcasted via UDP serialized using Google's Protobuff. Multithreaded application using native C++ openCV code for SURF detection/extraction, creating and using C++  objects from Java using SWIG.

Compilation Instructions
========================

In order to be able to compile the native code found in the jni/ folder, the Android NDK framework must be present, as well as OpenCV4Android > 2.4.2, on the target and host.
The JNI makefile (Android.mk) has a machine-specific path for the OpenCV location, please change that line to match the location of the required file in your machine. Additionally,
since starting from OpenCV4Android 2.4.2 the nonfree module is no longer included, you must add the header files manually to your OpenCV4Android header files. The compilation will
refer to those files for compilation of the extraction and detection part in "frame.cpp". The actual static library is already included in the jni/ folder (libopencv_nonfree.a). This
was compiled for armv7 architecture, so if you have a different architecture please recompile the OpenCV nonfree module for your specific target. To do so, download the latest OpenCV
tarball for Linux, go into the android/ folder and follow the installation instructions there.