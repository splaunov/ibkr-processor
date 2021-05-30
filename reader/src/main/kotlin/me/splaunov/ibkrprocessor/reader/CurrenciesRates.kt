package me.splaunov.ibkrprocessor.reader

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName = "ValCurs")
data class CurrenciesRates(
    @JacksonXmlProperty(localName = "Date", isAttribute = true)
    val date: String,
    @JacksonXmlProperty(localName = "Valute")
    @JacksonXmlElementWrapper(useWrapping = false)
    val rates: List<CurrencyRate>
)

data class CurrencyRate(
    @JacksonXmlProperty(localName = "NumCode")
    val numCode: String,
    @JacksonXmlProperty(localName = "CharCode")
    val charCode: String,
    @JsonDeserialize(using = FlexibleFloatDeserializer::class)
    @JacksonXmlProperty(localName = "Value")
    val value: Float
)

class FlexibleFloatDeserializer : JsonDeserializer<Float>() {
    override fun deserialize(parser: JsonParser, context: DeserializationContext): Float {
        return parser.text.replace(",", ".").toFloat()
    }
}