PREBUILT=/home/fede/AndroidSDK/android-ndk-r8/toolchains/arm-linux-androideabi-4.4.3

PLATFORM=/home/fede/AndroidSDK/android-ndk-r8/platforms/android-9/arch-arm

export CC="/home/fede/AndroidSDK/android-ndk-r8/toolchains/arm-linux-androideabi-4.4.3/prebuilt/linux-x86/bin/arm-linux-androideabi-gcc"

export CFLAGS="-fPIC -DANDROID -nostdlib"

export ANDROID_ROOT="/home/fede/AndroidSDK/android-ndk-r8"

export LDFLAGS="-Wl,-rpath-link=$ANDROID_ROOT/platforms/android-9/arch-arm/usr/lib/ -L$ANDROID_ROOT/platforms/android-9/arch-arm/usr/lib/"

export CPPFLAGS="-I$ANDROID_ROOT/platforms/android-9/arch-arm/usr/include/"

export LIBS="-lc "

./configure --host=arm-eabi
