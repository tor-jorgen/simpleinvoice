package invoice

import com.fasterxml.jackson.annotation.JsonProperty

enum class Action() {
    @JsonProperty("odt")
    GENERATE_ODT,

    @JsonProperty("pdf")
    GENERATE_PDF,

    @JsonProperty("email")
    SEND_EMAIL
}
