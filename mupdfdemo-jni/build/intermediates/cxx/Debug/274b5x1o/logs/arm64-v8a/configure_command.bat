@echo off
"C:\\Users\\codebuzz\\AppData\\Local\\Android\\Sdk\\ndk\\27.0.12077973\\ndk-build.cmd" ^
  "NDK_PROJECT_PATH=null" ^
  "APP_BUILD_SCRIPT=F:\\HiteshAndroid\\PdfReader\\mupdf-android-viewer-old\\jni\\Android.mk" ^
  "NDK_APPLICATION_MK=F:\\HiteshAndroid\\PdfReader\\mupdf-android-viewer-old\\jni\\Application.mk" ^
  "APP_ABI=arm64-v8a" ^
  "NDK_ALL_ABIS=arm64-v8a" ^
  "NDK_DEBUG=1" ^
  "APP_PLATFORM=android-24" ^
  "NDK_OUT=F:\\HiteshAndroid\\PdfReader\\mupdfdemo-jni\\build\\intermediates\\cxx\\Debug\\274b5x1o/obj" ^
  "NDK_LIBS_OUT=F:\\HiteshAndroid\\PdfReader\\mupdfdemo-jni\\build\\intermediates\\cxx\\Debug\\274b5x1o/lib" ^
  -j4 ^
  "APP_SHORT_COMMANDS=false" ^
  "LOCAL_SHORT_COMMANDS=false" ^
  -B ^
  -n
