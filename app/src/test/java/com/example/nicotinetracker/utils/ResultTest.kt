package com.example.nicotinetracker.utils

import org.junit.Assert.*
import org.junit.Test

class ResultTest {

    @Test
    fun `success result contains correct data`() {
        val result = Result.Success(42)
        
        assertTrue(result.isSuccess())
        assertFalse(result.isError())
        assertFalse(result.isLoading())
        
        assertEquals(42, result.getOrNull())
        assertNull(result.exceptionOrNull())
    }

    @Test
    fun `error result contains correct error information`() {
        val testException = RuntimeException("Test error")
        val result = Result.Error(testException, "Something went wrong")
        
        assertFalse(result.isSuccess())
        assertTrue(result.isError())
        assertFalse(result.isLoading())
        
        assertNull(result.getOrNull())
        assertEquals(testException, result.exceptionOrNull())
    }

    @Test
    fun `map function transforms success result`() {
        val original = Result.Success(10)
        val mapped = original.map { it * 2 }
        
        assertTrue(mapped.isSuccess())
        assertEquals(20, (mapped as Result.Success).data)
    }

    @Test
    fun `map function preserves error result`() {
        val original = Result.Error(RuntimeException("Error"))
        val mapped = original.map { it.toString() }
        
        assertTrue(mapped.isError())
    }

    @Test
    fun `loading result behaves correctly`() {
        val result = Result.Loading
        
        assertFalse(result.isSuccess())
        assertFalse(result.isError())
        assertTrue(result.isLoading())
        
        assertNull(result.getOrNull())
        assertNull(result.exceptionOrNull())
    }
}
