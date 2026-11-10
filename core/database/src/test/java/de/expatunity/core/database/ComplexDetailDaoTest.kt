package de.expatunity.core.database

import androidx.room.Room
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import androidx.test.core.app.ApplicationProvider
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ComplexDetailDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: ComplexDetailDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.complexDetailDao()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun `replaceAll removes old images and apartments`() = runTest {
        dao.replaceAll(CachedComplexDetail(
            detail("1", "en"),
            listOf(image("1", "en", 0), image("1", "en", 1), image("1", "en", 2)),
            listOf(apartment("1", "en", "a1"), apartment("1", "en", "a2")),
        ))

        dao.replaceAll(CachedComplexDetail(
            detail("1", "en", name = "Updated"),
            listOf(image("1", "en", 0)),
            listOf(apartment("1", "en", "a3")),
        ))

        val cached = dao.getCached("1", "en")!!
        assertEquals("Updated", cached.detail.name)
        assertEquals(listOf(0), cached.images.map { it.position })
        assertEquals(listOf("a3"), cached.apartments.map { it.id })
    }

    @Test
    fun `locales do not mix`() = runTest {
        dao.replaceAll(CachedComplexDetail(
            detail("1", "en", name = "Park"),
            listOf(image("1", "en", 0)),
            listOf(apartment("1", "en", "a1")),
        ))
        dao.replaceAll(CachedComplexDetail(
            detail("1", "de", name = "Park DE"),
            listOf(image("1", "de", 0), image("1", "de", 1)),
            emptyList(),
        ))

        val en = dao.getCached("1", "en")!!
        val de = dao.getCached("1", "de")!!
        assertEquals("Park", en.detail.name)
        assertEquals(1, en.images.size)
        assertEquals(1, en.apartments.size)
        assertEquals("Park DE", de.detail.name)
        assertEquals(2, de.images.size)
        assertTrue(de.apartments.isEmpty())
    }

    @Test
    fun `getCached returns null when missing`() = runTest {
        assertNull(dao.getCached("missing", "en"))
    }

    private fun detail(id: String, locale: String, name: String = "Park") =
        ComplexDetailEntity(id, locale, name, "Munich", null, "AVAILABLE", null, false)

    private fun image(complexId: String, locale: String, position: Int) =
        ComplexImageEntity(complexId, locale, position, "https://img/$position.jpg")

    private fun apartment(complexId: String, locale: String, id: String) =
        ApartmentEntity(complexId, locale, id, "Apt $id", 50.0, 300_000.0, null, "AVAILABLE")
}