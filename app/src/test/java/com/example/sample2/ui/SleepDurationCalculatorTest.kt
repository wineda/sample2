package com.example.sample2.ui

import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class SleepDurationCalculatorTest {
    @Test fun `22_00 to 06_00 is 480`() = assertEquals(480, computeDurationMinutes(LocalTime.of(22, 0), LocalTime.of(6, 0)))
    @Test fun `23_30 to 06_45 is 435`() = assertEquals(435, computeDurationMinutes(LocalTime.of(23, 30), LocalTime.of(6, 45)))
    @Test fun `03_00 to 11_00 is 480`() = assertEquals(480, computeDurationMinutes(LocalTime.of(3, 0), LocalTime.of(11, 0)))
    @Test fun `00_00 to 00_00 is 0`() = assertEquals(0, computeDurationMinutes(LocalTime.MIDNIGHT, LocalTime.MIDNIGHT))
}
