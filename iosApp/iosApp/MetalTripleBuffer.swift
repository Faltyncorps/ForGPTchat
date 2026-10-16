import Foundation
import Metal
import simd

/// Počet rámců „in flight“ – Apple Best Practices: triple buffering (CPU píše N+2 zatímco GPU čte N).
enum MetalFramePacing {
    static let maxFramesInFlight = 3
    /// Preferovaný FPS: ProMotion až 120; divisor maxima displeje (Apple Frame Rate guide).
    static let preferredFPS = 120
    static let minimumFPS = 30
}

/// Kruhový pool sdílených MTLBufferů pro dynamická data (CPU cesta grafu / uniforms).
/// Semafor brání přepsání bufferu, který GPU ještě čte. Žádná alokace každý snímek.
final class MetalTripleBufferRing {
    private let buffers: [MTLBuffer]
    private let semaphore: DispatchSemaphore
    private var index = 0
    private let lock = NSLock()

    /// - Parameters:
    ///   - device: MTLDevice
    ///   - bytesPerSlot: velikost jednoho slotu (zarovnaná na 256 B pro setVertexBuffer offsety)
    init?(device: MTLDevice, bytesPerSlot: Int, label: String = "elfall-triple") {
        let n = MetalFramePacing.maxFramesInFlight
        guard bytesPerSlot > 0 else { return nil }
        let aligned = (bytesPerSlot + 255) / 256 * 256
        var made: [MTLBuffer] = []
        made.reserveCapacity(n)
        for i in 0..<n {
            guard let b = device.makeBuffer(length: aligned, options: [.storageModeShared]) else { return nil }
            b.label = "\(label)-\(i)"
            made.append(b)
        }
        buffers = made
        semaphore = DispatchSemaphore(value: n)
    }

    /// Počet bajtů na slot (po zarovnání).
    var slotLength: Int { buffers.first?.length ?? 0 }

    /// Počká na volný slot (CPU–GPU sync), vrátí buffer + index. Po `commit` vždy zavolej `signalOnComplete`.
    func acquire() -> (buffer: MTLBuffer, index: Int) {
        semaphore.wait()
        lock.lock()
        let i = index
        index = (index + 1) % buffers.count
        let b = buffers[i]
        lock.unlock()
        return (b, i)
    }

    /// Zaregistruj na command buffer: po dokončení GPU uvolní slot.
    func signalOnComplete(_ commandBuffer: MTLCommandBuffer) {
        commandBuffer.addCompletedHandler { [weak self] _ in
            self?.semaphore.signal()
        }
    }

    /// Okamžité uvolnění, pokud se buffer necommitl (chyba enkodéru).
    func releaseWithoutGPU() { semaphore.signal() }
}

/// Volitelný MTLSharedEvent pro synchronizaci blit / compute → render (Apple Silicon).
/// Nepoužívá se pro každý UI snímek; připraveno pro budoucí MPS / blit cestu.
enum MetalSharedSync {
    static func makeEvent(device: MTLDevice, label: String = "elfall-shared") -> MTLSharedEvent? {
        let e = device.makeSharedEvent()
        e?.label = label
        return e
    }
}

/// Kontrola stránkového zarovnání pro `makeBuffer(bytesNoCopy:)` (iOS arm64 = 16 KiB).
enum MetalNoCopyAlign {
    static var pageSize: Int {
        #if canImport(Darwin)
        let p = Int(getpagesize())
        return max(p > 0 ? p : 16 * 1024, 16 * 1024)
        #else
        return 16 * 1024
        #endif
    }
    static func isAligned(_ ptr: UnsafeRawPointer) -> Bool {
        Int(bitPattern: ptr) % pageSize == 0
    }
    static func roundUp(_ n: Int) -> Int {
        let p = pageSize
        return (n + p - 1) / p * p
    }
}
