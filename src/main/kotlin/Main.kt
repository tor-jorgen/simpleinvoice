import invoice.Invoice
import invoice.InvoiceBatchConfig
import invoice.InvoiceConfig
import java.nio.file.Files
import java.nio.file.Path
import java.util.logging.LogManager
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    try {
        if (args.isEmpty() || args[0] == "-h" || args[0] == "--help") {
            help()
            return
        }

        // Clear logging from OdfXMLFactory
        LogManager.getLogManager().reset()

        when (args[0]) {
            "invoice" -> invoice(args)
            else -> help()
        }
    } catch (e: Exception) {
        println("SimpleInvoice exited because of failure!")
        exitProcess(1)
    }
}

private fun help() {
    println(
        """Usage: java -jar [path]simpleinvoice-<version>-all.jar invoice <invoice config> <batch config>
            |Run Simple Invoice.
            |
            |Arguments:
            |invoice: Create invoices based on the configuration files:
            |  invoice config: Path to general configuration files for all invoices
            |  batch config:   Path to configuration file for this invoice batch
        """.trimMargin()
    )
}

private fun invoice(args: Array<String>) {
    if (args.size != 3) {
        help()
        return
    }

    assertFileExists(args[1])
    assertFileExists(args[2])

    val config = InvoiceConfig.fromYaml(args[1])
    val invoiceConfig = InvoiceBatchConfig.fromYaml(args[2])
    val invoice = Invoice(config, invoiceConfig)
    invoice.runBatch()
}

private fun assertFileExists(path: String) {
    if (!Files.exists(Path.of(path))) {
        val message = "File '$path' does not exist!"
        println(message)
        throw RuntimeException(message)
    }
}
