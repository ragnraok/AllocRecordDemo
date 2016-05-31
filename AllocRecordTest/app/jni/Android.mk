
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_LDLIBS +=  -llog -ldl -Wl,--start-group -Wl,--end-group
LOCAL_MODULE    := test
LOCAL_SRC_FILES := test.cpp
//LOCAL_C_INCLUDES := /Users/ragnarok/Resource/android/AndroidSource/Android4.4/Platform4.4/system/core/include

include $(BUILD_SHARED_LIBRARY)
