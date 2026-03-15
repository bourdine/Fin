cmake_minimum_required(VERSION 3.18.1)
project("lottttto-miner")

# Путь к библиотекам (если они будут добавлены позже)
set(LIBS_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../../libs)

# Добавляем флаги для arm64-v8a
if(CMAKE_ANDROID_ARCH_ABI STREQUAL "arm64-v8a")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -march=armv8-a+fp+simd")
endif()

# Базовая оптимизация
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3 -fomit-frame-pointer -funroll-loops")

# Создаём нашу библиотеку-обёртку
add_library(miner_jni SHARED native-lib.cpp)

find_library(log-lib log)

# Проверяем наличие библиотеки XMRig (реальная интеграция)
if(EXISTS ${LIBS_DIR}/${CMAKE_ANDROID_ARCH_ABI}/libxmrig.so)
    message(STATUS "Found libxmrig.so at: ${LIBS_DIR}/${CMAKE_ANDROID_ARCH_ABI}/libxmrig.so")
    add_library(xmrig SHARED IMPORTED)
    set_target_properties(xmrig PROPERTIES IMPORTED_LOCATION ${LIBS_DIR}/${CMAKE_ANDROID_ARCH_ABI}/libxmrig.so)
    target_link_libraries(miner_jni xmrig ${log-lib})
    target_compile_definitions(miner_jni PRIVATE HAVE_XMRIG=1)
else()
    message(WARNING "libxmrig.so not found! Using mock implementation.")
    target_link_libraries(miner_jni ${log-lib})
    target_compile_definitions(miner_jni PRIVATE HAVE_XMRIG=0)
endif()
