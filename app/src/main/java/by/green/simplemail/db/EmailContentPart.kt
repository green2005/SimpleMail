package by.green.simplemail.db

public enum class EmailContentType { HTML, TXT, FILE }

public class EmailContentPart(val content: String, val contentType: EmailContentType)