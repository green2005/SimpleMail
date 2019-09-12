package by.green.simplemail.db

enum class EmailDomains(
    val domain: String,
    val incomingServer: String,
    val incomingPort: String,
    val outServer: String,
    val outPort: String
) {

    GMAIL(
        domain = "gmail.com",
        incomingServer = "imap.gmail.com",
        incomingPort = "993",
        outServer = "smtp.gmail.com",
        outPort = "465"
    ),
    TUT_BY(
        domain = "tut.by",
        incomingServer = "imap.yandex.ru",
        incomingPort = "993",
        outServer = "smtp.yandex.com",
        outPort = "587"
    ),
    YANDEX_RU(
        domain = "yandex.ru",
        incomingServer = "imap.yandex.ru",
        incomingPort = "993",
        outServer = "smtp.yandex.com",
        outPort = "587"
    )
}