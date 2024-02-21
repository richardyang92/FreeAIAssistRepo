#include <cassert>
#include <cstdio>
#include <vector>
#include <fstream>

#include "whisper_lite.h"

static std::vector<whisper_token> prompt_tokens;

int8_t whisper_lite::whisper::whisper_init(const char *model_path) {
    fprintf(stdout, "whisper lite init start");
    this->model = model_path;
    this->keep_ms = std::min(this->keep_ms, this->keep_ms);
    this->length_ms = std::max(this->length_ms, this->step_ms);

    if (this->language != "auto" && whisper_lang_id(this->language.c_str()) == -1) {
        fprintf(stderr, "error: unknown language '%s'\n", this->language.c_str());
        return REASON_UNKNOWN_LANG;
    }
    this->cparams.use_gpu = this->use_gpu;
    this->ctx = whisper_init_from_file_with_params(this->model.c_str(), this->cparams);

    if (this->ctx == nullptr) return REASON_CTX_INIT_FAILED;
    fprintf(stdout, "whisper lite init end");
    return REASON_OK;
}

bool whisper_lite::whisper::whisper_vad(float *audios, int len) {
    std::vector<float> pcmf32(len, 0.0f);
    memcpy(pcmf32.data(), audios, len);
    return vad_simple(pcmf32, WHISPER_SAMPLE_RATE, 1000, this->vad_thold, this->freq_thold , false);
}

std::string whisper_lite::whisper::whisper_full_transcribe(float* audios, int len) {
    std::string ret;
    whisper_full_params wparams = whisper_full_default_params(WHISPER_SAMPLING_BEAM_SEARCH);
    wparams.print_progress   = false;
    wparams.print_special    = this->print_special;
    wparams.print_realtime   = false;
    wparams.print_timestamps = !this->no_timestamps;
    wparams.translate        = this->translate;
    wparams.single_segment   = false;
    wparams.max_tokens       = this->max_tokens;
    wparams.language         = this->language.c_str();
    wparams.n_threads        = this->n_threads;

    wparams.audio_ctx        = this->audio_ctx;
    wparams.speed_up         = this->speed_up;

    wparams.tdrz_enable      = this->tinydiarize; // [TDRZ]

    // disable temperature fallback
    //wparams.temperature_inc  = -1.0f;
    wparams.temperature_inc  = this->no_fallback ? 0.0f : wparams.temperature_inc;

    wparams.prompt_tokens    = this->no_context ? nullptr : prompt_tokens.data();
    wparams.prompt_n_tokens  = this->no_context ? 0       : prompt_tokens.size();

    if (whisper_full(this->ctx, wparams, audios, len) != 0) {
        fprintf(stderr, "%s: failed to process audio\n", this->model.c_str());
        return ret;
    }

    const int n_segments = whisper_full_n_segments(this->ctx);
    for (int i = 0; i < n_segments; i++) {
        const char *text = whisper_full_get_segment_text(ctx, i);
        ret.append(text);
    }
    return ret;
}

void whisper_lite::whisper::whisper_close() {
    whisper_free(this->ctx);
}