package me.splaunov.ibkrprocessor.reader

import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Test

import java.time.LocalDate

@MicronautTest
class CurrencyRatesProviderTest {

    @Inject
    lateinit var client: CentralBankHttpClient

    @Test
    fun getRate() {
        val provider = CurrencyRatesProvider(client)

        val res = provider.getRate(LocalDate.parse("2022-04-15"))

        res shouldBe 81.288f
    }
}