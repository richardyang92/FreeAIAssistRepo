/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_free_aiassist_asr_whisper_WhisperJNI */

#ifndef _Included_com_free_aiassist_asr_whisper_WhisperJNI
#define _Included_com_free_aiassist_asr_whisper_WhisperJNI
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_free_aiassist_asr_whisper_WhisperJNI
 * Method:    init
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_free_aiassist_asr_whisper_WhisperJNI_init
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_free_aiassist_asr_whisper_WhisperJNI
 * Method:    fullTranscribe
 * Signature: ([F)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_free_aiassist_asr_whisper_WhisperJNI_fullTranscribe
  (JNIEnv *, jobject, jfloatArray);

/*
 * Class:     com_free_aiassist_asr_whisper_WhisperJNI
 * Method:    vadSimple
 * Signature: ([F)Z
 */
JNIEXPORT jboolean JNICALL Java_com_free_aiassist_asr_whisper_WhisperJNI_vadSimple
  (JNIEnv *, jobject, jfloatArray);

/*
 * Class:     com_free_aiassist_asr_whisper_WhisperJNI
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_free_aiassist_asr_whisper_WhisperJNI_close
  (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif
