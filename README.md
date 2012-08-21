Android-SfM-client
==================

Application to leverage a Structure from Motion pipeline by extracting features and matching frames onboard using OpenCV 2.4.2. The data gathered along with gravity sensor data is broadcasted via UDP serialized using Google's Protobuff. Multithreaded application using native C++ openCV code for SURF detection/extraction, creating and using C++  objects from Java using SWIG. 