package util.smtp

data class SmtpConfig(
    val host: String,
    val port: Int,
    val tls: Boolean,
    val username: String,
    val password: String,
    val senderEmail: String,
    val senderName: String,
    val characterSet: String = "UTF-8",
    val contentTransferEncoding: String = "quoted-printable"
)
