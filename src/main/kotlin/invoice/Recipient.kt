package invoice

data class Recipient(
    val name: String,
    val addressLine1: String,
    val addressLine2: String,
    val addressLine3: String,
    val email: String
)
