#include "com_free_aiassist_asr_sherpa_SherpaJNI.h"

#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <string>

#include "sherpa-ncnn/c-api/c-api.h"

#define SHERPA_TOKENS           "tokens.txt"
#define SHERPA_ENCODER_PARAM    "encoder_jit_trace-pnnx.ncnn.param"
#define SHERPA_ENCODER_BIN      "encoder_jit_trace-pnnx.ncnn.bin"
#define SHERPA_DECODER_PARAM    "decoder_jit_trace-pnnx.ncnn.param"
#define SHERPA_DECODER_BIN      "decoder_jit_trace-pnnx.ncnn.bin"
#define SHERPA_JOINER_PARAM     "joiner_jit_trace-pnnx.ncnn.param"
#define SHERPA_JOINER_BIN       "joiner_jit_trace-pnnx.ncnn.bin"

#define AUDIO_SAMPLE_RATE       16000

static SherpaNcnnRecognizerConfig config;
static SherpaNcnnRecognizer *recognizer;
static SherpaNcnnStream *s;

JNIEXPORT jboolean JNICALL Java_com_free_aiassist_asr_sherpa_SherpaJNI_init
  (JNIEnv *env, jobject obj, jstring modelDir) {
    const char *cmodel_dir = env->GetStringUTFChars(modelDir, 0);
    char model_path_base[7][256] = { "", "", "", "", "", "", ""};
    // strcat(model_path, cmodel_dir);
    for (int i = 0; i < 7; i++) {
        strcat(model_path_base[i], cmodel_dir);
    }
    memset(&config, 0, sizeof(config));

    config.model_config.tokens = strcat(model_path_base[0], SHERPA_TOKENS);
    config.model_config.encoder_param = strcat(model_path_base[1], SHERPA_ENCODER_PARAM);
    config.model_config.encoder_bin = strcat(model_path_base[2], SHERPA_ENCODER_BIN);
    config.model_config.decoder_param = strcat(model_path_base[3], SHERPA_DECODER_PARAM);
    config.model_config.decoder_bin = strcat(model_path_base[4], SHERPA_DECODER_BIN);
    config.model_config.joiner_param = strcat(model_path_base[5], SHERPA_JOINER_PARAM);
    config.model_config.joiner_bin = strcat(model_path_base[6], SHERPA_JOINER_BIN);

    config.model_config.num_threads = 4;
    config.model_config.use_vulkan_compute = 0;

    config.decoder_config.decoding_method = "greedy_search";

    config.decoder_config.num_active_paths = 4;
    config.enable_endpoint = 0;
    config.rule1_min_trailing_silence = 2.4;
    config.rule2_min_trailing_silence = 1.2;
    config.rule3_min_utterance_length = 300;

    config.feat_config.sampling_rate = 16000;
    config.feat_config.feature_dim = 80;

    recognizer = CreateRecognizer(&config);
    s = CreateStream(recognizer);
    return recognizer != nullptr && s != nullptr;
}

JNIEXPORT void JNICALL Java_com_free_aiassist_asr_sherpa_SherpaJNI_reset
  (JNIEnv *, jobject) {
    float tail_paddings[4800] = {0};  // 0.3 seconds at 16 kHz sample rate
    AcceptWaveform(s, 16000, tail_paddings, 4800);
    InputFinished(s);
    DestroyStream(s);
}

JNIEXPORT void JNICALL Java_com_free_aiassist_asr_sherpa_SherpaJNI_restart
  (JNIEnv *, jobject) {
    s = CreateStream(recognizer);
    Reset(recognizer, s);
}

JNIEXPORT jstring JNICALL Java_com_free_aiassist_asr_sherpa_SherpaJNI_fullTranscribe
  (JNIEnv *env, jobject obj, jfloatArray audios) {
    jsize length = env->GetArrayLength(audios);
    jfloat* elements = env->GetFloatArrayElements(audios, nullptr);
    if (elements == nullptr) {
        fprintf(stderr, "audio data is null");
        return env->NewStringUTF("");
    }
    AcceptWaveform(s, 16000, elements, length);
    while (IsReady(recognizer, s)) {
        Decode(recognizer, s);
    }

    bool is_endpoint = IsEndpoint(recognizer, s);
    if (is_endpoint) Reset(recognizer, s);

    SherpaNcnnResult *r = GetResult(recognizer, s);
    std::string txt = r->text;
    DestroyResult(r);
    return env->NewStringUTF(txt.c_str());
}

JNIEXPORT void JNICALL Java_com_free_aiassist_asr_sherpa_SherpaJNI_close
  (JNIEnv *env, jobject obj) {
    float tail_paddings[4800] = {0};  // 0.3 seconds at 16 kHz sample rate
    AcceptWaveform(s, 16000, tail_paddings, 4800);
    InputFinished(s);
    DestroyStream(s);
    DestroyRecognizer(recognizer);
}