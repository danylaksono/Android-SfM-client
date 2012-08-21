%module sfmpipeline

%{
#include "frame.h"
%}

%typemap(jni) signed char *getFrame "jbyteArray"
%typemap(jtype) signed char *getFrame "byte[]"
%typemap(jstype) signed char *getFrame "byte[]"
%typemap(javaout) signed char *getFrame {
  return $jnicall;
}
%typemap(in,numinputs=0,noblock=1) size_t *len { 
  size_t length=0;
  $1 = &length;
}
%typemap(out) signed char *getFrame {
  $result = JCALL1(NewByteArray, jenv, length);
  JCALL4(SetByteArrayRegion, jenv, $result, 0, length, $1);
}


%include "frame.h"