#include "com_free_aiassist_asr_whisper_WhisperJNI.h"
#include "whisper_lite.h"

static whisper_lite::whisper gWhisper;

JNIEXPORT jboolean JNICALL Java_com_free_aiassist_asr_whisper_WhisperJNI_init
  (JNIEnv *env, jobject obj, jstring modelPath) {
    const char *cpath = env->GetStringUTFChars(modelPath, 0);
    if (cpath == nullptr) return false;
    int8_t ret = gWhisper.whisper_init(cpath);
    if (ret != whisper_lite::REASON_OK) {
        fprintf(stderr, "failed to init whisper model: %d", ret);
    }
    return ret == whisper_lite::REASON_OK;
}

JNIEXPORT jstring JNICALL Java_com_free_aiassist_asr_whisper_WhisperJNI_fullTranscribe
(JNIEnv *env, jobject obj, jfloatArray audios) {
    jsize length = env->GetArrayLength(audios);
    jfloat* elements = env->GetFloatArrayElements(audios, nullptr);
    if (elements == nullptr) {
        fprintf(stderr, "audio data is null");
        return env->NewStringUTF("");
    }

    std::string result = gWhisper.whisper_full_transcribe(elements, length);
    return env->NewStringUTF(result.c_str());
}

JNIEXPORT jboolean JNICALL Java_com_free_aiassist_asr_whisper_WhisperJNI_vadSimple
  (JNIEnv *env, jobject obj, jfloatArray audios) {
    jsize length = env->GetArrayLength(audios);
    jfloat* elements = env->GetFloatArrayElements(audios, nullptr);
    if (elements == nullptr) {
        fprintf(stderr, "audio data is null");
        return true;
    }
    return gWhisper.whisper_vad(elements, length);
}

JNIEXPORT void JNICALL Java_com_free_aiassist_asr_whisper_WhisperJNI_close
  (JNIEnv *env, jobject obj) {
    gWhisper.whisper_close();
}