package util.smtp

import java.util.*
import javax.activation.DataHandler
import javax.activation.DataSource
import javax.activation.FileDataSource
import javax.mail.Authenticator
import javax.mail.BodyPart
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.Multipart
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

class SmtpClient(val config: SmtpConfig) {

    private var session: Session? = null

    fun open(): SmtpClient {
        val properties = Properties()
        properties["mail.smtp.auth"] = true
        properties["mail.smtp.starttls.enable"] = config.tls
        properties["mail.smtp.host"] = config.host
        properties["mail.smtp.port"] = config.port
        properties["mail.smtp.ssl.trust"] = config.host
        session = Session.getInstance(
            properties,
            object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(config.username, config.password)
                }
            }
        )

        return this
    }

    fun send(
        subject: String,
        text: String,
        toEmail1: String,
        toEmail2: String?,
        invoicePath: String,
        invoiceName: String
    ) {
        try {
            val msg = MimeMessage(session)
            msg.addHeader("Content-type", "text/HTML; charset=${config.characterSet}")
            msg.addHeader("Content-Transfer-Encoding", config.contentTransferEncoding)

            msg.setFrom(InternetAddress(config.senderEmail, config.senderName))
            msg.setSubject(subject, config.characterSet)
            msg.sentDate = Date()
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail1, false))
            if (toEmail2 != null) {
                msg.addRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail2, false))
            }

            var messageBodyPart: BodyPart = MimeBodyPart()
            messageBodyPart.setText(text)
            val multipart: Multipart = MimeMultipart()
            multipart.addBodyPart(messageBodyPart)

            messageBodyPart = MimeBodyPart()
            val source: DataSource = FileDataSource(invoicePath)
            messageBodyPart.setDataHandler(DataHandler(source))
            messageBodyPart.setFileName(invoiceName)
            multipart.addBodyPart(messageBodyPart)

            msg.setContent(multipart)
            Transport.send(msg)
            println("* e-mail sent to $toEmail1 ${if (toEmail2 != null) " and $toEmail2}" else ""}")
        } catch (e: MessagingException) {
            println("* ERROR: Could not sent e-mail to $toEmail1 ${if (toEmail2 != null) " and $toEmail2}" else ""}")
            e.printStackTrace()
        }
    }
}
