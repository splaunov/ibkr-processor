package me.splaunov.ibkrprocessor.data

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

fun Instant.toLocalDate(): LocalDate = LocalDate.ofInstant(this, ZoneId.of("UTC"))
