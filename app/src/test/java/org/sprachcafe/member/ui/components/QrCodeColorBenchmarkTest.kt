package org.sprachcafe.member.ui.components

import org.junit.Test

class QrCodeColorBenchmarkTest {

    private fun simulateParseColor(colorString: String): Int {
        if (colorString[0] == '#') {
            var color = colorString.substring(1).toLong(16)
            if (colorString.length == 7) {
                color = color or -0x1000000L
            }
            return color.toInt()
        }
        throw IllegalArgumentException("Unknown color")
    }

    @Test
    fun benchmarkColorParsingInLoopVsOutside() {
        val width = 512
        val height = 512
        val iterations = 5

        // Warm up JVM
        var dummy = 0
        for (i in 0 until 100_000) {
            dummy += simulateParseColor("#1D1B1A")
        }

        // Benchmark inside loop
        var unoptimizedTimeTotal = 0L
        for (run in 0 until iterations) {
            val startTime = System.nanoTime()
            var sum = 0
            for (x in 0 until width) {
                for (y in 0 until height) {
                    val darkColor = simulateParseColor("#1D1B1A")
                    sum += darkColor
                }
            }
            val elapsedTime = System.nanoTime() - startTime
            unoptimizedTimeTotal += elapsedTime
            dummy += sum
        }
        val avgUnoptimizedMs = (unoptimizedTimeTotal / iterations) / 1_000_000.0

        // Benchmark outside loop
        var optimizedTimeTotal = 0L
        for (run in 0 until iterations) {
            val startTime = System.nanoTime()
            var sum = 0
            val darkColor = simulateParseColor("#1D1B1A")
            for (x in 0 until width) {
                for (y in 0 until height) {
                    sum += darkColor
                }
            }
            val elapsedTime = System.nanoTime() - startTime
            optimizedTimeTotal += elapsedTime
            dummy += sum
        }
        val avgOptimizedMs = (optimizedTimeTotal / iterations) / 1_000_000.0

        println("=== BENCHMARK RESULTS ===")
        println("Iterations per run: ${width * height} (${width}x${height})")
        println("Unoptimized (parse in loop): %.3f ms".format(avgUnoptimizedMs))
        println("Optimized (parse outside loop): %.3f ms".format(avgOptimizedMs))
        if (avgOptimizedMs > 0) {
            println("Speedup factor: %.2fx".format(avgUnoptimizedMs / avgOptimizedMs))
        }
    }
}
