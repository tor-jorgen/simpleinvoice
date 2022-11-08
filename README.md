# SimpleInvoice

This is a very simple invoice application, originally made for sending out annual invoices to members of a
welfare organisation. It has no graphical user interface, and no database. All actions are executed from the command
line,
and information is stored in text files.

It can:

- Generate batches of invoices in ODT and/or PDF format
- Send out emails with invoices attached (optional)

## Dependencies

This program depends on [Odt2Pdf](https://github.com/tor-jorgen/odt2pdf) to generate PDF files.
Download, build, and publish artifacts for Odt2Pdf before you build SimpleInvoice.

## Build application

Execute the following from the command line to build the application:

    # Linux    
    ./gradlew clean build

    # Windows
    gradle clean build

## Configure application

You need to create:

* One or more invoice templates
* A general configuration file for all batches
* A configuration file for each batch
* One or more recipient files

### Invoice template

The invoice template can be any Open Office/Libre Office ODT file.
The following table contains the placeholders that can be used in the invoice template. They will be
replaced when invoices are generated:

| Placeholder  | Description                                  |
|--------------|----------------------------------------------|
| `_DATE_`     | Invoice date                                 |
| `_DUE_DATE_` | Due date                                     |
| `_NO_`       | Invoice number                               |
| `_NAME1_`    | Name of first recipient                      |
| `_NAME2_`    | Name of second recipient (if any)            |
| `_ADDRESS1_` | First address line of recipient(s)           |
| `_ADDRESS2_` | Second address line of recipient(s) (if any) |
| `_ADDRESS3_` | Third address line of recipient(s) (if any)  |
| `_ITEM_`     | Invoice item                                 |
| `_PRICE_`    | Price for invoice item                       |
| `_TOTAL_`    | Total price                                  |

#### Example

See [invoice.odt](src/test/resources/invoice.odt).

### General configuration file

This configuration will be used for all invoice batches. It must be defined using yaml format:

| Property           | Description                                                                                                           |
|--------------------|-----------------------------------------------------------------------------------------------------------------------|
| `invoiceDirectory` | Path to directory where generated invoices will be stored                                                             |
| `smtp.host`        | SMTP host to send emails from                                                                                         |
| `smtp.port`        | SMTP port to send emails from                                                                                         |
| `smtp.tls`         | `true` if TLS should be used, `false` else                                                                            |
| `smtp.username`    | Username to log in to the SMTP host                                                                                   |
| `smtp.password`    | Password to log in to the SMTP host                                                                                   |
| `smtp.senderEmail` | Email address of user that is sending the emails                                                                      |
| `smtp.senderName`  | Name of user that is sending the emails                                                                               |
| `smtp.characterSet`            | Character set used when sending emails. Default is `UTF-8`                                                            |
| `smtp.contentTransferEncoding`            | Default is `quoted-printable`. See [The Content-Transfer-Encoding Header Field](https://www.w3.org/Protocols/rfc1341/5_Content-Transfer-Encoding.html) for more information |

Notice that the two last settings may be overridden by the email client.

#### Sending E-mails

E-mails are sent using [SMTP](https://en.wikipedia.org/wiki/Simple_Mail_Transfer_Protocol).

If you have set up MFA on your E-mail account, you probably have to do something special to be able to send E-mails
with Simple Invoice:

* Gmail: See [Sign in with App Passwords](https://support.google.com/accounts/answer/185833?hl=en). Then, set
  `smtp.password` to your app password

#### Example

    invoiceDirectory: ~/invoices/generated

    smtp:
        host: smtp.gmail.com
        port: 587
        tls: true
        username: billy.boy@gmail.com
        password: PASSWORD
        senderEmail: billy.boy@gmail.com
        senderName: Billy Boy

Also see [invoice-config.yml](src/test/resources/invoice-config.yml).

### Batch configuration file

This configuration will be used for each invoice batch. It must be defined using yaml format:

| Property | Description                                                                                                                                                                                                    |
|---------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|`actions`| Action to perform when running SimpleInvoice. Possible actions:<br/>- `odt` to generate invoice in ODT format<br/>- `pdf`  to generate invoice in PDF format<br/>- `email` to send email with invoice attached |
| `template` | The path to invoice template                                                                                                                                                                                   |
| `invoiceName`    | Name of invoice file. The following placeholders are supported:<br/>`_NO_`, `_DATE_`, `_DUE_DATE_`, `_ADDRESS1_`, `_NAME1_`, `_ITEM_`<br/>See above for a description                                          |
|`recipientsFile`| The path to the file that contains the invoice receipients. See description of format below                                                                                                                    |
|`recipientsFileDelimiter`| Character used to delimit columns on each line of the recipient file. Default is `;`                                                                                                                           |
|`recipientsFileLinesToSkip`| Number of line to skip in the beginning of the recipient file. Default is `1`                                                                                                                                  |
|`startNumber`| The number of the first invoioce. It will be increased by one for each invoice                                                                                                                                 |
|`invoiceDate`| The date the invoice was sent                                                                                                                                                                                  |
|`dueDate`| The invoice due date                                                                                                                                                                                           |
|`item`| Ther text of the item to add to the invoice                                                                                                                                                                    |
|`price`| The price of the item                                                                                                                                                                                          |
|`emailSubject`| The subject of the email(s) that will be sent                                                                                                                                                                  |
|`emailText`| The text to add to the email(s)                                                                                                                                                                                |

#### Example

    actions:
      - pdf
      - email
    template: ~/invoices/invoice.odt
    invoiceName: _NO_-Invoice-_NAME1_
    recipientsFile: ~/invoices/members.csv
    startNumber: 198
    invoiceDate: 2022-11-10
    dueDate: 2022-12-10
    item: Annual fee for Singing Housemothers Association 2022
    price: 20 USD
    emailSubject: Annual fee for Singing Housemothers Association 2022
    emailText: Please find the invoice for 2022 attached.

Also see [batch-config.yml](src/test/resources/batch-config.yml).

### Recipients file

This file will be used to define the invoice recipients. It is a simple text files with one recipient on each
line. Each line consists of columns separated by a character (se `recipientsFileDelimiter` above).

The following columns must be defined:

| Column              | Description                                  |
|---------------------|----------------------------------------------|
| `1: Address line 1` | First address line of recipient           |
| `2: Address line 2` | Second address line of recipient (if any) |
| `2: Address line 3` | Third address line of recipient (if any)     |
| `3: Name`           | Name of recipient                            |
| `4: E-mail address` | E-mail address of recipient (if any)         |

One or two recipients can be defined per location. All recipients on the same location will receive the same invoice.

Eample:

    Address line 1;Address line 2;Address line 3;Name;E-mail address
    The Road 1;Duckburg;HOMELAND;Bob Kaare;bobka@gmail.com
    The Road 1;Duckburg;HOMELAND;Lilly Hammer;lillh@hotmail.com
    The Road 2;Duckburg;HOMELAND;Fan Dango;fanda@yahoo.com

Also see [members.csv](src/test/resources/members.csv).

## Run application

Note that a Java Runtime Environment (JRE) must be installed to be able to run this application.

Execute the following from the command line to run the application:

    java -jar [path]simpleinnvoice-<version>-all.jar invoice <path to invoice config file> <path to batch config file>

E.g.:

    java -jar simpleinvoice/build/simpleinnvoice-1.0-all.jar invoice ~/invoive/invoice-config.yml ~/batch-config.yml
