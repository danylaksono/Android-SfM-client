%module sfmmatcher

%{

#include "Matcher.h"
#include "frame.h"
%}

%typemap(jni) signed char *getMatches "jbyteArray"
%typemap(jtype) signed char *getMatches "byte[]"
%typemap(jstype) signed char *getMatches "byte[]"
%typemap(javaout) signed char *getMatches {
  return $jnicall;
}
%typemap(in,numinputs=0,noblock=1) size_t *len { 
  size_t length=0;
  $1 = &length;
}
%typemap(out) signed char *getMatches {
  $result = JCALL1(NewByteArray, jenv, length);
  JCALL4(SetByteArrayRegion, jenv, $result, 0, length, $1);
}

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

%include "Matcher.h"
%include "frame.h"