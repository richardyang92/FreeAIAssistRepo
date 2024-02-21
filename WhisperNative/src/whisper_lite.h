#pragma once

#include "common.h"
#include "whisper.h"

#include <string>
#include <thread>

namespace whisper_lite {
    struct whisper_params {
        int32_t n_threads  = std::min(4, (int32_t) std::thread::hardware_concurrency());
        int32_t step_ms    = 0;
        int32_t length_ms  = 10000;
        int32_t keep_ms    = 200;
        int32_t capture_id = 0;
        int32_t max_tokens = 32;
        int32_t audio_ctx  = 0;

        float vad_thold    = 0.6f;
        float freq_thold   = 100.0f;

        bool speed_up      = false;
        bool translate     = false;
        bool no_fallback   = false;
        bool print_special = false;
        bool no_context    = true;
        bool no_timestamps = false;
        bool tinydiarize   = false;
        bool save_audio    = false; // save audio to wav file
        bool use_gpu       = true;

        std::string language  = "zh";
        std::string model     = "models/ggml-large-v2.bin";
        std::string fname_out;
    };

    enum whisper_reason {
        REASON_OK = 0,
        REASON_UNKNOWN_LANG = 1,
        REASON_CTX_INIT_FAILED = 2,
    };

    class whisper : public whisper_params {
    private:
        whisper_context_params  cparams;
        whisper_context         *ctx;
    public:
        int8_t      whisper_init(const char *model_path);
        bool        whisper_vad(float *audios, int len);
        std::string whisper_full_transcribe(float* audios, int len);
        void        whisper_close();
    };
};