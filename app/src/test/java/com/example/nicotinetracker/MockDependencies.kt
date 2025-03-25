package com.example.nicotinetracker

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.nicotinetracker.data.PouchDatabase
import org.mockito.Mockito

class MockDependencies {
    // Mock databáze pro testování
    fun provideTestDatabase(): PouchDatabase {
        return Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PouchDatabase::class.java
        )
        .allowMainThreadQueries()
        .build()
    }

    // Mock kontextu
    fun provideTestContext(): Context {
        return ApplicationProvider.getApplicationContext()
    }

    // Generické mockování závislostí
    inline fun <reified T> mockDependency(): T {
        return Mockito.mock(T::class.java)
    }
}

// Rozšíření pro snadné testování asynchronních operací
suspend fun <T> runBlockingTest(block: suspend () -> T): T {
    return kotlinx.coroutines.test.runBlockingTest { block() }
}
