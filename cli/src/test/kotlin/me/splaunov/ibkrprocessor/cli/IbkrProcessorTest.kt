package me.splaunov.ibkrprocessor.cli
import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import javax.inject.Inject

@MicronautTest
class IbkrProcessorTest {

    @Inject
    lateinit var application: EmbeddedApplication<*>

    @Test @Disabled
    fun testItWorks() {
        Assertions.assertTrue(application.isRunning)
    }

}
