// C ABI zero-copy bufferů Rust → Metal (core/src/gpu.rs). Ruční hlavička, verze ELFALL-GPU-CHART-v1.
#pragma once
#include <stddef.h>
#include <stdint.h>

typedef struct ElfallGpuBuffer {
    uint8_t *ptr;      // stránkově zarovnaný, vlastní Rust
    size_t len;        // platná data (bajty)
    size_t capacity;   // násobek stránky -> délka pro makeBuffer(bytesNoCopy:)
} ElfallGpuBuffer;

size_t elfall_gpu_page_size(void);
// 0 = ok; záporné = -(1 + CoreError). Při chybě *out = {NULL,0,0}.
int32_t elfall_gpu_chart_from_values(const float *values, size_t n, uint32_t points, ElfallGpuBuffer *out);
int32_t elfall_gpu_chart_from_sealed(const uint8_t *key, size_t key_len, const uint8_t *field, size_t field_len,
                                     const uint8_t *sealed, size_t sealed_len, uint32_t points, ElfallGpuBuffer *out);
// Jediný povolený způsob uvolnění (volá deallocator MTLBufferu). Dvojí/cizí free -> záporné, nic se nestane.
int32_t elfall_gpu_buffer_free(void *ptr, size_t capacity);
size_t elfall_gpu_live_allocations(void);
