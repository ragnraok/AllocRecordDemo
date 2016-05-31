#define TAG "AllocRecordJNI"

#include <stdio.h>
#include <jni.h>
#include <android/log.h>
#include <dlfcn.h>

#include <pthread.h>
#include "extra.h"
#include "Globals.h"

#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

#ifndef DALVIK_MAJOR_VERSION

#define DALVIK_MAJOR_VERSION    0
#define DALVIK_MINOR_VERSION    0
#define DALVIK_BUG_VERSION      0

/*
 * VM build number.  This must change whenever something that affects the
 * way classes load changes, e.g. field ordering or vtable layout.  Changing
 * this guarantees that the optimized form of the DEX file is regenerated.
 */
#define DALVIK_VM_BUILD         0

#endif

struct DvmGlobals * t_gDvm = NULL;
char buff[1024] = {0x00,};

static void dumpAllocRecordData() {
    if (t_gDvm) {
        snprintf(buff, 1024, "allocRecord is 0x%x", t_gDvm->allocRecords);
        LOGI("%s", buff);

        int headIndex = (t_gDvm->allocRecordHead+1 + t_gDvm->allocRecordMax - t_gDvm->allocRecordCount)
                                    & (t_gDvm->allocRecordMax-1);
        LOGI("headIndex: %d", headIndex);

        //LOGI("numLoadedClasses: %d", t_gDvm->numLoadedClasses);

        LOGI("allocRecordHead: %d", t_gDvm->allocRecordHead);
        LOGI("allocRecordCount is %d", t_gDvm->allocRecordCount);
        LOGI("allocRecordMax:%d", t_gDvm->allocRecordMax);

        //LOGI("heapStartingSize: %d", t_gDvm->heapStartingSize);
        //LOGI("heapMaximumSize: %d", t_gDvm->heapMaximumSize);

        if (t_gDvm->allocRecordCount > 0 && t_gDvm->allocRecords != NULL) {
            LOGI("size: %d", t_gDvm->allocRecords[t_gDvm->allocRecordCount-1].size);
            LOGI("threadId: %d", t_gDvm->allocRecords[t_gDvm->allocRecordCount-1].threadId);
            LOGI("class: %s",  t_gDvm->allocRecords[t_gDvm->allocRecordCount-1].clazz->descriptor);
            LOGI("first method: %s",  t_gDvm->allocRecords[t_gDvm->allocRecordCount-1].stackElem[0].method->name);
        }
    }
}

static JNINativeMethod gMethods[] = {
    {"dumpAllocRecordData","()V", (void*)dumpAllocRecordData},
};

JNIEXPORT jint JNICALL  JNI_OnLoad(JavaVM* vm, void* reserved) {
    LOGV("JNI_OnLoad is exec!!!!!!!!!!!");

   JNIEnv* env;
   if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        return JNI_ERR;
   }

    jclass testClass = env->FindClass("com/example/ragnarok/allocrecordtest/MainActivity");
    if (!testClass) {
        return JNI_ERR;
    }

    LOGI("vm version: %d %d %d %d", DALVIK_MAJOR_VERSION, DALVIK_MINOR_VERSION, DALVIK_BUG_VERSION, DALVIK_VM_BUILD);

    int ret = env->RegisterNatives(testClass, gMethods, sizeof(gMethods) / sizeof(gMethods[0]));

    if (ret != JNI_OK) {
        return JNI_ERR;
    }

    void * handle = dlopen("libdvm.so", RTLD_NOW);
    snprintf(buff, 1024, "handle is 0x%x", handle);
    LOGI("%s", buff);

    t_gDvm = (struct DvmGlobals *)dlsym(handle, "gDvm");

    if (t_gDvm) {
        LOGI("get DvmGlobals");
        dumpAllocRecordData();
    }

    if (t_gDvm->pBootLoaderAlloc) {
        LOGI("mapLength: %d", t_gDvm->pBootLoaderAlloc->mapLength);
    }

    return JNI_VERSION_1_6;
}


