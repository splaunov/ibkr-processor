@file:Suppress("SpellCheckingInspection")

object Versions {
    const val MICRONAUT = "3.4.2"
    const val LOGBACK_ENCODER = "6.6"
    const val LOGBOOK = "2.13.0"
    const val MOCKK = "1.12.0"
    const val KOTEST = "4.6.3"
    const val KOTEST_WIRE_MOCK = "1.0.3"
    const val STRIKT = "0.32.0"
    const val TEST_CONTAINERS = "1.16.3"
    const val HIBERNATE_TYPES = "2.12.1"
    const val KOTLIN_LOGGING = "2.0.10"
    const val RESILIENCE_4_J = "1.7.1"
    const val CAFFEINE = "3.0.4"
    const val PICOCLI = "4.6.3"
    const val OPENCSV = "5.5.2"
    const val KOTLIN_CSV = "1.2.0"
    const val POI_OOXML = "5.2.2"
}

object PluginVersions {
    const val KOTLIN = "1.6.10"
    const val SHADOW = "7.1.0"
    const val MICRONAUT = "3.3.2"
    const val GIT_PROPERTIES = "2.3.2"
    const val KT_LINT = "10.2.0"
    const val VERSIONS = "0.39.0"
}

object Micronaut {
    const val bom = "io.micronaut:micronaut-bom:${Versions.MICRONAUT}"
    const val runtime = "io.micronaut:micronaut-runtime"
    const val kotlinRuntime = "io.micronaut.kotlin:micronaut-kotlin-runtime"
    const val inject = "io.micronaut:micronaut-inject-java"
    const val hibernateJpa = "io.micronaut.sql:micronaut-hibernate-jpa"
    const val jdbcHikari = "io.micronaut.sql:micronaut-jdbc-hikari"
    const val dataJdbc = "io.micronaut.data:micronaut-data-jdbc"
    const val flyway = "io.micronaut.flyway:micronaut-flyway"
    const val reactor = "io.micronaut.reactor:micronaut-reactor"
    const val extensionFunctions = "io.micronaut.kotlin:micronaut-kotlin-extension-functions"

    const val dataProcessor = "io.micronaut.data:micronaut-data-processor"
    const val dataJpa = "io.micronaut.data:micronaut-data-hibernate-jpa"
    const val security = "io.micronaut.security:micronaut-security"
    const val securityAnnotation = "io.micronaut.security:micronaut-security-annotations"
    const val securityJwt = "io.micronaut.security:micronaut-security-jwt"
    const val validation = "io.micronaut:micronaut-validation"
    const val httpClient = "io.micronaut:micronaut-http-client"
    const val reactorHttpClient = "io.micronaut.reactor:micronaut-reactor-http-client"
    const val openApi = "io.micronaut.openapi:micronaut-openapi"
    const val tracing = "io.micronaut:micronaut-tracing"
    const val management = "io.micronaut:micronaut-management"
    const val kotest = "io.micronaut.test:micronaut-test-kotest"
    const val cache = "io.micronaut.cache:micronaut-cache-caffeine"
    const val picoCli = "io.micronaut.picocli:micronaut-picocli"
    const val viewsHandlebars = "io.micronaut.views:micronaut-views-handlebars"
    const val micrometerPrometheus = "io.micronaut.micrometer:micronaut-micrometer-registry-prometheus"
    const val jacksonXml = "io.micronaut.xml:micronaut-jackson-xml"
}

object Kotlin {
    const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8"
    const val reflect = "org.jetbrains.kotlin:kotlin-reflect"
}

object Resilience {
    const val micronaut = "io.github.resilience4j:resilience4j-micronaut:${Versions.RESILIENCE_4_J}"
    const val circuitBreaker = "io.github.resilience4j:resilience4j-circuitbreaker:${Versions.RESILIENCE_4_J}"
    const val consumer = "io.github.resilience4j:resilience4j-consumer:${Versions.RESILIENCE_4_J}"
}

object Database {
    const val mysqlConnector = "mysql:mysql-connector-java"
    const val hibernateTypes = "com.vladmihalcea:hibernate-types-55:${Versions.HIBERNATE_TYPES}"
}

object Serialization {
    const val jackson = "com.fasterxml.jackson.module:jackson-module-kotlin"
    const val jacksonDataTypes = "com.fasterxml.jackson.datatype:jackson-datatype-jsr310"
}

object Logging {
    const val logback = "ch.qos.logback:logback-classic"
    const val kotlinLogging = "io.github.microutils:kotlin-logging-jvm:${Versions.KOTLIN_LOGGING}"
    const val logstashEncoder = "net.logstash.logback:logstash-logback-encoder:${Versions.LOGBACK_ENCODER}"
}

object Logbook {
    const val core = "org.zalando:logbook-core:${Versions.LOGBOOK}"
    const val json = "org.zalando:logbook-json:${Versions.LOGBOOK}"
    const val netty = "org.zalando:logbook-netty:${Versions.LOGBOOK}"
    const val logstash = "org.zalando:logbook-logstash:${Versions.LOGBOOK}"
}

object Kotest {
    const val wireMock = "io.kotest.extensions:kotest-extensions-wiremock:${Versions.KOTEST_WIRE_MOCK}"
    const val runner = "io.kotest:kotest-runner-junit5-jvm:${Versions.KOTEST}"
    const val assertions = "io.kotest:kotest-assertions-core:${Versions.KOTEST}"
}

object Testcontainers {
    const val bom = "org.testcontainers:testcontainers-bom:${Versions.TEST_CONTAINERS}"
    const val testContainers = "org.testcontainers:testcontainers"
    const val mysql = "org.testcontainers:mysql"
}

object JUnit {
    const val jupiter = "org.testcontainers:junit-jupiter"
    const val jupiterParams  = "org.junit.jupiter:junit-jupiter-params"
}

object Other {
    const val annotationApi = "javax.annotation:javax.annotation-api"
    const val swaggerAnnotation = "io.swagger.core.v3:swagger-annotations"
    const val mockk = "io.mockk:mockk:${Versions.MOCKK}"
    const val strikt = "io.strikt:strikt-core:${Versions.STRIKT}"
    const val caffeine = "com.github.ben-manes.caffeine:caffeine:${Versions.CAFFEINE}"
    const val h2Database = "com.h2database:h2"
    const val picoCli = "info.picocli:picocli"
    const val picoCliCodeGen = "info.picocli:picocli-codegen:${Versions.PICOCLI}"
    const val openCsv = "com.opencsv:opencsv:${Versions.OPENCSV}"
    const val kotlinCsv = "com.github.doyaaaaaken:kotlin-csv-jvm:${Versions.KOTLIN_CSV}"
    const val poiOoxml = "org.apache.poi:poi-ooxml:${Versions.POI_OOXML}"
}
