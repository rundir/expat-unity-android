package de.expatunity.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FavoritesDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: FavoritesDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.favoritesDao()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun `toggle adds then removes`() = runTest {
        dao.toggle("1")
        assertTrue(dao.contains("1"))

        dao.toggle("1")
        assertFalse(dao.contains("1"))
    }

    @Test
    fun `observeIds emits on changes`() = runTest {
        dao.observeIds().test {
            assertEquals(emptyList<String>(), awaitItem())

            dao.toggle("1")
            assertEquals(listOf("1"), awaitItem())

            dao.toggle("2")
            assertEquals(setOf("1", "2"), awaitItem().toSet())

            dao.toggle("1")
            assertEquals(listOf("2"), awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }
}