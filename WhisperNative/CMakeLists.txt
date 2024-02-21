cmake_minimum_required(VERSION 3.5)

project(WhisperLite VERSION 1.0.0)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED true)

add_library(whisper_jni SHARED
            "src/whisper_jni.cpp"
            "src/whisper_lite.cpp"
            "src/common.cpp")

include_directories(
    "src/ggml"
    "/Users/yangyang/Library/Java/JavaVirtualMachines/openjdk-20.0.1/Contents/Home/include"
    "/Users/yangyang/Library/Java/JavaVirtualMachines/openjdk-20.0.1/Contents/Home/include/darwin")

find_library(whisper
            NAMES libwhisper.dylib
            PATHS lib
            NO_DEFAULT_PATH)

set_target_properties(whisper_jni
                    PROPERTIES
                    LIBRARY_OUTPUT_DIRECTORY
                    "/Users/yangyang/Projects/FreeAIAssistRepo/FreeAIAssist")

target_link_libraries(whisper_jni ${whisper})