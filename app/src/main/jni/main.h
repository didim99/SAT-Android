//
// Created by didim99 on 29.08.17.
//

#ifndef SAT_MAIN_H
#define SAT_MAIN_H

#include <jni.h>
#include <android/log.h>
#include "crypto.h"

#define LOG_TAG "SAT_log_NativeCryptoLib"
#define LOG_D(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOG_V(...) __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, __VA_ARGS__)

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jlong JNICALL
Java_com_didim99_sat_core_sbxconverter_SbxConverter_compressFile
    (JNIEnv *env, jclass type, jstring _fileName, jint verCode);

JNIEXPORT jlong JNICALL
Java_com_didim99_sat_core_sbxconverter_SbxConverter_uncompressFile(
    JNIEnv *env, jclass type, jstring _fileName);

JNIEXPORT jlong JNICALL
Java_com_didim99_sat_core_resconverter_TexConverter_compressTexture(
    JNIEnv *env, jclass type, jstring _name, jint sizeX, jint sizeY);

JNIEXPORT jintArray JNICALL
Java_com_didim99_sat_core_resconverter_TexConverter_uncompressTexture(
    JNIEnv *env, jclass type, jstring _name);

#ifdef __cplusplus
}
#endif

#endif //SAT_MAIN_H
