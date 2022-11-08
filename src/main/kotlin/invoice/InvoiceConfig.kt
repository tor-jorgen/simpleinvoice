package invoice

import util.smtp.SmtpConfig
import util.yaml.YamlUtil

data class InvoiceConfig(
    val invoiceDirectory: String,
    val smtp: SmtpConfig
) {
    companion object {
        fun fromYaml(path: String): InvoiceConfig = YamlUtil.fromYaml(path)
    }
}
