import kotlin.system.exitProcess
import com.github.kittinunf.fuel.Fuel
import java.lang.Thread.sleep
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sin


fun exitWithError(message: String, code: Int = -1): Nothing {
    println(message)
    exitProcess(code)
}

fun main(args: Array<String>) {
    val address = args.getOrElse(0) { exitWithError("Address not specified", -2) }
    val discrFrequency =
        args.getOrElse(1) { "128" }.toIntOrNull() ?: exitWithError("Discretization frequency is not a number")
    val batchSize = args.getOrElse(2) { "128" }.toIntOrNull() ?: exitWithError("Batch size is not a number")

    val timeStep = 1.0 / discrFrequency

    var id = 0
    while (true) {
        Fuel.post(address)
            .header(
                Pair("DF", discrFrequency),
                Pair("BS", batchSize),
                Pair("TS", System.currentTimeMillis()),
                Pair("ID", id++)
            )
            .body(
                run {
                    val shorts = ShortArray(batchSize) {
                        val pt = it * timeStep * 6.28

                        ((-0.3 * sin(pt - 2.44)
                                - 0.2 * sin(2 * pt - 1.07)
                                + 0.07 * sin(4 * pt + 0.87)
                                ) * 1000).toShort()
                    }
                    val bytes = ByteArray(batchSize * 2){0}
                    ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(shorts)

                    bytes
                }
            )
            .also { println(it) }
            .response()

        sleep((timeStep * batchSize).toLong())
    }
}