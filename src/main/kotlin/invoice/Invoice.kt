package invoice

import org.odftoolkit.simple.TextDocument
import org.odt2pdf.PDFConverter
import org.w3c.dom.Node
import util.smtp.SmtpClient
import java.io.ByteArrayOutputStream
import java.io.File

private const val ADDRESS_LINE_1_POS = 0
private const val ADDRESS_LINE_2_POS = 1
private const val ADDRESS_LINE_3_POS = 2
private const val NAME_POS = 3
private const val EMAIL_POS = 4

private const val INVOICE_NO = "_NO_"
private const val INVOICE_DATE = "_DATE_"
private const val DUE_DATE = "_DUE_DATE_"
private const val ADDRESS_LINE_1 = "_ADDRESS1_"
private const val ADDRESS_LINE_2 = "_ADDRESS2_"
private const val ADDRESS_LINE_3 = "_ADDRESS3_"
private const val NAME_1 = "_NAME1_"
private const val NAME_2 = "_NAME2_"
private const val ITEM = "_ITEM_"
private const val ITEM_PRICE = "_PRICE_"
private const val TOTAL_PRICE = "_TOTAL_"

class Invoice(private val config: InvoiceConfig, private val invoiceConfig: InvoiceBatchConfig) {
    private val recipientsPerAddress = readRecipients()

    fun runBatch() {
        File(config.invoiceDirectory).mkdirs()
        createInvoice()
    }

    private fun readRecipients(): MutableMap<String, MutableList<Recipient>> {
        val result = mutableMapOf<String, MutableList<Recipient>>()
        var lineNo = 0
        File(invoiceConfig.recipientsFile).forEachLine {
            if (lineNo >= invoiceConfig.recipientsFileLinesToSkip) {
                val parts = it.split(invoiceConfig.recipientsFileDelimiter)
                val recipient = Recipient(
                    parts[NAME_POS],
                    parts[ADDRESS_LINE_1_POS],
                    parts[ADDRESS_LINE_2_POS],
                    parts[ADDRESS_LINE_3_POS],
                    parts[EMAIL_POS]
                )
                val address = "${parts[ADDRESS_LINE_1_POS]}-${parts[ADDRESS_LINE_2_POS]}-${parts[ADDRESS_LINE_3_POS]}"
                if (result[address] == null) {
                    result[address] = ArrayList<Recipient>()
                }
                result[address]?.add(recipient)
            }
            lineNo++
        }
        return result
    }

    private fun createInvoice() {
        var invoiceNo: Int = invoiceConfig.startNumber
        val pdfConverter = initPdfConverter()
        val smtpClient = initSmtpClient()
        recipientsPerAddress.forEach { (_, recipients: List<Recipient>) ->
            TextDocument.loadDocument(File(invoiceConfig.template)).use { document ->
                traverse(document.contentRoot, recipients, invoiceNo)
                traverse(document.header.odfElement, recipients, invoiceNo)
                traverse(document.footer.odfElement, recipients, invoiceNo)
                val invoiceName = getInvoiceName(invoiceNo, recipients)
                val invoicePath = "${config.invoiceDirectory}/$invoiceName.odt"
                val outPath = invoicePath.replace("odt", "pdf")
                generateOdf(document, invoicePath)
                generatePdf(document, outPath, pdfConverter)
                println("Invoice generated for ${recipients[0].addressLine1}")
                sendEmail(smtpClient, recipients, invoiceName, invoicePath, outPath)
                invoiceNo++
            }
        }
    }

    private fun getInvoiceName(invoiceNo: Int, recipients: List<Recipient>): String {
        var invoiceName = invoiceConfig.invoiceName
        invoiceName = invoiceName.replace(INVOICE_NO, invoiceNo.toString())
        invoiceName = invoiceName.replace(INVOICE_DATE, invoiceConfig.invoiceDate)
        invoiceName = invoiceName.replace(DUE_DATE, invoiceConfig.dueDate)
        invoiceName =
            invoiceName.replace(ADDRESS_LINE_1, if (recipients.isNotEmpty()) recipients[0].addressLine1 else "")
        invoiceName = invoiceName.replace(NAME_1, if (recipients.isNotEmpty()) recipients[0].name else "")
        invoiceName = invoiceName.replace(ITEM, invoiceConfig.item)

        return invoiceName
    }

    private fun initPdfConverter(): PDFConverter? =
        if (invoiceConfig.generatePdf()) PDFConverter() else null

    private fun initSmtpClient(): SmtpClient? =
        if (invoiceConfig.sendEmail()) SmtpClient(config.smtp).open() else null

    private fun generatePdf(document: TextDocument, outPath: String, pdfConverter: PDFConverter?) {
        if (invoiceConfig.generatePdf()) {
            val out = ByteArrayOutputStream()
            document.save(out)
            pdfConverter!!.fromOdf(out.toByteArray(), outPath)
        }
    }

    private fun generateOdf(document: TextDocument, invoicePath: String) {
        if (invoiceConfig.generateOdt()) {
            document.save(File(invoicePath))
        }
    }

    private fun traverse(node: Node, recipients: List<Recipient>, invoiceNo: Int) {
        if (node.textContent.isNotBlank()) {
            replace(node, recipients, invoiceNo)
        } else {
            for (i in 0 until node.childNodes.length) {
                traverse(node.childNodes.item(i), recipients, invoiceNo)
            }
        }
    }

    private fun replace(node: Node, recipients: List<Recipient>, invoiceNo: Int) {
        when (node.textContent) {
            INVOICE_DATE -> node.textContent = invoiceConfig.invoiceDate
            DUE_DATE -> node.textContent = invoiceConfig.dueDate
            INVOICE_NO -> node.textContent = invoiceNo.toString()
            NAME_1 -> node.textContent = if (recipients.isNotEmpty()) recipients[0].name else ""
            NAME_2 -> node.textContent = if (recipients.size > 1) recipients[1].name else ""
            ADDRESS_LINE_1 ->
                node.textContent =
                    if (recipients.isNotEmpty()) recipients[0].addressLine1 else ""

            ADDRESS_LINE_2 ->
                node.textContent =
                    if (recipients.isNotEmpty()) recipients[0].addressLine2 else ""

            ADDRESS_LINE_3 ->
                node.textContent =
                    if (recipients.isNotEmpty()) recipients[0].addressLine3 else ""

            ITEM -> node.textContent = invoiceConfig.item
            ITEM_PRICE, TOTAL_PRICE -> node.textContent = invoiceConfig.price
        }
    }

    private fun sendEmail(
        smtpClient: SmtpClient?,
        recipients: List<Recipient>,
        invoiceName: String,
        odtPath: String,
        pdfPath: String
    ) {
        if (!invoiceConfig.sendEmail()) {
            return
        }

        val recipient1 = recipients[0].email
        if (recipient1.isBlank()) {
            println("Cannot send e-mail to ${recipients[0].name}, because e-mail address is missing.")
            return
        }

        val invoicePath = if (invoiceConfig.generatePdf()) pdfPath else odtPath
        val invoiceFileName = "$invoiceName.${if (invoiceConfig.generatePdf()) "pdf" else "odt"}"
        val recipient2 = if (recipients.size > 1) recipients[1].email else null
        smtpClient!!.send(
            invoiceConfig.emailSubject,
            invoiceConfig.emailText,
            recipient1,
            recipient2,
            invoicePath,
            invoiceFileName
        )
    }
}
