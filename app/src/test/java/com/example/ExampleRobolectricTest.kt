package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.DateUtils
import com.example.data.LockoutDatabase
import com.example.data.LockoutRepository
import com.example.data.SupplementLogEntity
import com.example.domain.LockoutDomainEngine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var database: LockoutDatabase
    private lateinit var repository: LockoutRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, LockoutDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = LockoutRepository(database.lockoutDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun appName_isLockout() {
        assertEquals("LOCKOUT", context.getString(R.string.app_name))
    }

    @Test
    fun roomSeed_isIdempotentAndPopulatesAllTables() = runBlocking {
        repository.ensureSeeded()
        val firstExercises = repository.allExercises.first().size
        val firstSupps = repository.allSupplements.first().size
        val firstHabits = repository.allHabits.first().size
        val firstVault = repository.allVaultItems.first().size

        // Re-running ensureSeeded must not duplicate rows
        repository.ensureSeeded()
        assertTrue(firstExercises >= 20)
        assertTrue(firstSupps >= 6)
        assertEquals(7, firstHabits)
        assertTrue(firstVault >= 10)
        assertEquals(firstExercises, repository.allExercises.first().size)
        assertEquals(firstHabits, repository.allHabits.first().size)
    }

    @Test
    fun supplementDaysTakenAndAlarmSchedule_persistCorrectlyInRoom() = runBlocking {
        repository.ensureSeeded()
        val sup = repository.allSupplements.first().first()
        val initialServings = sup.servingsLeft

        // Log past 2 days + today
        val dao = database.lockoutDao()
        dao.markSupplementTaken(SupplementLogEntity(supplementId = sup.id, dateIso = DateUtils.daysAgoIso(2)))
        dao.markSupplementTaken(SupplementLogEntity(supplementId = sup.id, dateIso = DateUtils.daysAgoIso(1)))
        repository.toggleSupplementForToday(sup, currentlyTaken = false)

        val updatedSup = repository.allSupplements.first().first { it.id == sup.id }
        assertEquals(initialServings - 1, updatedSup.servingsLeft)

        val dates = repository.allSupplementLogs.first()
            .filter { it.supplementId == sup.id }
            .map { it.dateIso }
            .toSet()
        val stats = LockoutDomainEngine.computeAdherenceStats(dates)
        assertEquals(3, stats.totalDays)
        assertEquals(3, stats.currentStreakDays)
        assertTrue(stats.last7DaysMask.last())

        // Persist dose alarm time
        repository.updateSupplementReminder(updatedSup, enabled = true, hour = 7, minute = 45)
        val armedSup = repository.allSupplements.first().first { it.id == sup.id }
        assertTrue(armedSup.reminderEnabled)
        assertEquals(7, armedSup.reminderHour)
        assertEquals(45, armedSup.reminderMinute)
    }

    @Test
    fun dailyHabits_toggleAndTrackPerDate() = runBlocking {
        repository.ensureSeeded()
        val habit = repository.allHabits.first().first()

        repository.toggleHabitForToday(habit.id, currentlyDone = false)
        assertTrue(repository.todayHabitLogs().first().any { it.habitId == habit.id })

        repository.toggleHabitForToday(habit.id, currentlyDone = true)
        assertFalse(repository.todayHabitLogs().first().any { it.habitId == habit.id })
    }
}
