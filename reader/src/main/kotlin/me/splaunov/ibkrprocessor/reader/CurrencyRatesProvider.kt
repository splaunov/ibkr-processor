package me.splaunov.ibkrprocessor.reader

import io.micronaut.core.convert.format.Format
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client
import java.time.LocalDate
import jakarta.inject.Singleton

@Client("http://www.cbr.ru/scripts/")
interface CentralBankHttpClient {
    @Get("XML_daily.asp")
    fun getRates(@Format("dd.MM.yyyy") @QueryValue("date_req") date: LocalDate): CurrenciesRates
}

@Singleton
class CurrencyRatesProvider(private val httpClient: CentralBankHttpClient) {
    private val centralBankRates = mutableMapOf<LocalDate, List<CurrencyRate>>()

    fun getRate(date: LocalDate, symbol: String = "USD") =
        getRates(date).first { it.charCode == symbol }.value

    private fun getRates(date: LocalDate): List<CurrencyRate> =
        centralBankRates.getOrPut(date) { httpClient.getRates(date).rates }
}