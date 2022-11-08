package invoice

import util.yaml.YamlUtil

data class InvoiceBatchConfig(
    val actions: List<Action>,
    val template: String,
    val invoiceName: String,
    val recipientsFile: String,
    val recipientsFileDelimiter: String = ";",
    val recipientsFileLinesToSkip: Int = 1,
    val startNumber: Int,
    val invoiceDate: String,
    val dueDate: String,
    val item: String,
    val price: String,
    val emailSubject: String = "Change default emailSubject in configuration",
    val emailText: String = "Change default emailText in configuration"
) {
    companion object {
        fun fromYaml(path: String): InvoiceBatchConfig = YamlUtil.fromYaml(path)
    }

    fun generateOdt() = actions.contains(Action.GENERATE_ODT)

    fun generatePdf() = actions.contains(Action.GENERATE_PDF)

    fun sendEmail() = actions.contains(Action.SEND_EMAIL)
}
