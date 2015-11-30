/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
#include <string.h>
#include <jni.h>
#include <assert.h>
//#include <android/log.h>
//#include <stdio.h>
//#include <assert.h>
//#include <limits.h>
//#include <unistd.h>
//#include <fcntl.h>
//
//#include <jni.h>
//#include <JNIHelp.h>
//#include <AndroidRuntime.h>

#include "g726.h"

//#define LOG_TAG "sky"
//#define  LOGE(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
//#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
//#define LOG_NDEBUG 0


/* This is a trivial JNI example where we use a native method
 * to return a new VM String. See the corresponding Java source
 * file located at:
 *
 *   apps/samples/hello-jni/project/src/com/example/HelloJni/HelloJni.java
 */

static g726_state_t *mG726 = NULL;
static int mBitrate = 0;

static void c_g726_init(JNIEnv *env, jobject thiz, jint bitRate)
{
	//LOGE("g726_init %d\n", bitRate);
	if( mG726 == NULL ){
		mBitrate = bitRate;
		mG726 = (g726_state_t *)malloc(sizeof(g726_state_t));
		g726_init(mG726, mBitrate);
	}else{
		//LOGE("g726_init exist !\n");
		if( mBitrate != bitRate ){
			//LOGE("g726_init reinit !\n");
			free(mG726);
			mBitrate = bitRate;
			mG726 = (g726_state_t *)malloc(sizeof(g726_state_t));
			g726_init(mG726, mBitrate);
		}
	}
	//LOGE("g726_init end!\n");
}

static void c_g726_deinit(JNIEnv *env, jobject thiz)
{
	//LOGE("g726_deinit start\n");
	if( mG726 != NULL ){
		free(mG726);
		mG726 = NULL;
	}
	//LOGE("g726_deinit end\n");
}

static jint c_g726_decode(JNIEnv *env, jobject thiz, jbyteArray arr, jint datalen, jbyteArray retArr)
{
	if( mG726 == NULL ){
		//LOGE("g726 not init!");
		return 0;
	}
	//获取数组的长度
	int len= env->GetArrayLength(arr);
	////LOGE("native_g726_decode LEN=%d datalen=%d\n",len, datalen);
	//获取short类型的数组
	jbyte * elems=env->GetByteArrayElements(arr,NULL);

//	jbyte * out=env->GetByteArrayElements(retArr,NULL);
	char out[1024*1024];
//	unsigned char in[1024*1024];
//	memcpy(in, elems, len);
//	env->ReleaseByteArrayElements( arr, elems, 0);

//	short out[1024*1024];

	int iRet = g726_decode(mG726, (short*)out, (unsigned char*)elems, datalen);
	env->ReleaseByteArrayElements( arr, elems, 0);
	////LOGE("g726_decode ret=%d\n",iRet);
//	jshortArray retArr = env->NewShortArray(iRet);
//	//把elems复制给arr,从第0位开始,长度位len
	env->SetByteArrayRegion(retArr,0,iRet*sizeof(short),(jbyte*)out);
//	env->ReleaseByteArrayElements( arr, out, 0);

//	jclass jc = env->GetObjectClass(thiz);
//	env->DeleteLocalRef(jc);

	return iRet*sizeof(short);
}

static jbyteArray c_g726_decode2(JNIEnv *env, jobject thiz, jshortArray arr)
{
	if( mG726 == NULL ){
		//LOGE("g726 not init!");
		return NULL;
	}
	//获取数组的长度
	int len= env->GetArrayLength(arr);
	//LOGE("native_g726_decode LEN=%d\n",len);
	//获取short类型的数组
	jshort * elems=env->GetShortArrayElements(arr,NULL);
	unsigned char in[1024*1024];
	memcpy(in, elems, len*sizeof(jshort));
	env->ReleaseShortArrayElements( arr, elems, 0);

	short out[1024*1024];

	int iRet = g726_decode(mG726, out, (unsigned char*)in, len);

	jbyteArray retArr = env->NewByteArray(iRet*sizeof(short));
	//把elems复制给arr,从第0位开始,长度位len
	env->SetByteArrayRegion(retArr,0,iRet*sizeof(short),(jbyte*)out);

	jclass jc = env->GetObjectClass(thiz);
		env->DeleteLocalRef(jc);
	return retArr;
}

static jint c_g726_encode(JNIEnv *env, jobject thiz, jbyteArray arr, jint datalen, jbyteArray retArr)
{
	if( mG726 == NULL ){
		//LOGE("g726 not init!");
		return 0;
	}
	//获取数组的长度
	int len=env->GetArrayLength(arr);
	//LOGE("g726_encode LEN=%d datalen=%d\n",len,datalen);
	//获取short类型的数组
	jbyte * elems = env->GetByteArrayElements(arr,NULL);

//	jbyte * out = env->GetByteArrayElements(retArr,NULL);

//	short in[512*1024];
//	memcpy(in, elems, len*sizeof(jshort));
//	env->ReleaseShortArrayElements( arr, elems, 0);
	//LOGE("g726_encode 111111111\n");
	char out[320];

	int iRet = g726_encode(mG726, (unsigned char*)out, (short*)elems, datalen/(sizeof(short)));
	env->ReleaseByteArrayElements( arr, elems, 0);
	//LOGE("g726_encode ret=%d\n",iRet);
//	jbyteArray retArr = env->NewByteArray(iRet);
	//把elems复制给arr,从第0位开始,长度位len
	env->SetByteArrayRegion(retArr,0,iRet,(jbyte*)out);
//	env->ReleaseByteArrayElements( arr, out, 0);
	jclass jc = env->GetObjectClass(thiz);
		env->DeleteLocalRef(jc);
	//LOGE("g726_encode 222222222\n");
	return iRet;
}

// ----------------------------------------------------------------------------
static JNINativeMethod gMethods[] = {
	{"native_g726_init",        "(I)V",                             (void *)c_g726_init},
	{"native_g726_deinit",      "()V",                              (void *)c_g726_deinit},
	{"native_g726_decode",      "([BI[B)I", 						(void *)c_g726_decode},
	{"native_g726_encode",      "([BI[B)I",                         (void *)c_g726_encode},
};

static const char* const kClassPathName = "net/sunniwell/sz/flycam/util/EncG726";

// This function only registers the native methods
static int register_g726(JNIEnv *env)
{
	jclass clazz;
	/* look up the class */
	clazz = env->FindClass( kClassPathName );
	//clazz = env->FindClass(env, className);
	if (clazz == NULL) {
	    //LOGE("Can't find class %s\n", kClassPathName);
	    return -1;
	}

	//LOGE("register native methods");

	/* register all the methods */
	if (env->RegisterNatives( clazz, gMethods, sizeof(gMethods) / sizeof(gMethods[0])) != JNI_OK)
	//if (env->RegisterNatives(env, clazz, gMethods, sizeof(gMethods) / sizeof(gMethods[0])) != JNI_OK)
	{
	    //LOGE("Failed registering methods for %s\n", kClassPathName);
	    return -1;
	}

	/* fill out the rest of the ID cache */
	return 0;
//    return registerNativeMethods(env,
//                "com/tutk/sample/AVAPI/nativeG726", gMethods, sizeof(gMethods) / sizeof(gMethods[0])));
}

jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    JNIEnv* env = NULL;
    jint result = -1;
    //LOGE("JNI_OnLoad start\n");
    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        //LOGE("ERROR: GetEnv failed\n");
        goto bail;
    }
    assert(env != NULL);

    if (register_g726(env) < 0) {
        //LOGE("ERROR: G726 native registration failed\n");
        goto bail;
    }

    /* success -- return valid version number */
    result = JNI_VERSION_1_4;
    //LOGE("JNI_OnLoad end\n");
bail:
    return result;
}
