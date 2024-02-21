cmake_minimum_required(VERSION 3.5)

project(SherpaLite VERSION 1.0.0)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED true)

set(SHERPA_STATIC_LIB "/tmp/sherpa-ncnn/static/lib")

add_library(sherpa_jni
            SHARED
            "sherpa_jni.cpp")

include_directories(
    "/tmp/sherpa-ncnn/static/include"
    "/Users/yangyang/Library/Java/JavaVirtualMachines/openjdk-20.0.1/Contents/Home/include"
    "/Users/yangyang/Library/Java/JavaVirtualMachines/openjdk-20.0.1/Contents/Home/include/darwin")

add_library(sherpa-ncnn-c-api
            STATIC
            IMPORTED)
set_target_properties(sherpa-ncnn-c-api
                    PROPERTIES
                    IMPORTED_LOCATION
                    ${SHERPA_STATIC_LIB}/libsherpa-ncnn-c-api.a)

add_library(sherpa-ncnn-core
            STATIC
            IMPORTED)
set_target_properties(sherpa-ncnn-core
                    PROPERTIES
                    IMPORTED_LOCATION
                    ${SHERPA_STATIC_LIB}/libsherpa-ncnn-core.a)

add_library(kaldi-native-fbank-core
            STATIC
            IMPORTED)
set_target_properties(kaldi-native-fbank-core
                    PROPERTIES
                    IMPORTED_LOCATION
                    ${SHERPA_STATIC_LIB}/libkaldi-native-fbank-core.a)

add_library(ncnn
            STATIC
            IMPORTED)
set_target_properties(ncnn
                    PROPERTIES
                    IMPORTED_LOCATION
                    ${SHERPA_STATIC_LIB}/libncnn.a)

set_target_properties(sherpa_jni
                    PROPERTIES
                    LIBRARY_OUTPUT_DIRECTORY
                    "/Users/yangyang/Projects/FreeAIAssistRepo/FreeAIAssist")

target_link_libraries(sherpa_jni
                    ncnn
                    sherpa-ncnn-c-api
                    sherpa-ncnn-core
                    kaldi-native-fbank-core)