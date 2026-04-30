package com.example.sample2.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class StepClampTest {
    @Test fun `clamps low to zero`() = assertEquals(0, applyStepDelta(0, -500))
    @Test fun `clamps high to max`() = assertEquals(999_999, applyStepDelta(999_500, 1_000))
    @Test fun `adds normal delta`() = assertEquals(5_500, applyStepDelta(5_000, 500))
}
