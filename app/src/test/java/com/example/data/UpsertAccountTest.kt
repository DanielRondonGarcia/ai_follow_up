package com.example.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric Room tests for the (provider, userId) upsert path.
 *
 * Covers the spec scenarios:
 * - Re-auth same (provider, userId) updates the row in place (same id, no
 *   duplicate, updated fields).
 * - First auth inserts a new row.
 * - Two accounts of the same provider with different userId stay isolated:
 *   re-authing one does not touch the other.
 * - UsageLog rows are preserved when the account is updated via upsert.
 */

class FakeCredentialEncryptor : CredentialEncryptor {
    override fun encrypt(plaintext: String) = "enc:$plaintext"
    override fun decrypt(ciphertext: String) = ciphertext.removePrefix("enc:")
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class UpsertAccountTest {

  private lateinit var db: AppDatabase
  private lateinit var dao: AccountDao
  private lateinit var repo: UsageRepository

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    dao = db.accountDao()
    repo = UsageRepository(db, FakeCredentialEncryptor())
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun upsert_newAccount_insertsNewRow() = runTest {
    val id = repo.upsertAccount(
      provider = "Ollama",
      userId = "new@test.com",
      email = "new@test.com",
      authToken = "",
      cookies = "session=abc",
      userAgent = "ua",
      planType = "Free",
    )
    assertNotEquals(0L, id)
    // Flow first emission; collect synchronously via list
    val accounts = dao.getAllAccounts().first()
    assertEquals(1, accounts.size)
    assertEquals("new@test.com", accounts[0].userId)
    assertEquals("session=abc", FakeCredentialEncryptor().decrypt(accounts[0].cookies))
  }

  @Test
  fun upsert_existingSameProviderAndUserId_updatesInPlaceNoDuplicate() = runTest {
    // Insert account A with id=7 (auto-generated)
    val originalId = repo.upsertAccount(
      provider = "Ollama",
      userId = "emailA@test.com",
      email = "emailA@test.com",
      authToken = "",
      cookies = "session=old",
      userAgent = "ua-old",
      planType = "Free",
    )

    // Re-auth with same (provider, userId) but new credentials
    val upsertedId = repo.upsertAccount(
      provider = "Ollama",
      userId = "emailA@test.com",
      email = "emailA@test.com",
      authToken = "",
      cookies = "session=new",
      userAgent = "ua-new",
      planType = "Pro",
    )

    assertEquals("upsert must return the same id", originalId, upsertedId)

    val accounts = dao.getAllAccounts().first()
    assertEquals("must be exactly 1 row, no duplicate", 1, accounts.size)
    assertEquals("session=new", FakeCredentialEncryptor().decrypt(accounts[0].cookies))
    assertEquals("ua-new", accounts[0].userAgent)
    assertEquals("Pro", accounts[0].planType)
  }

  @Test
  fun upsert_twoAccountsSameProviderDifferentUserId_bothRemainIsolated() = runTest {
    val idA = repo.upsertAccount(
      provider = "Ollama",
      userId = "emailA@test.com",
      email = "emailA@test.com",
      authToken = "",
      cookies = "session=A",
      userAgent = "ua-A",
      planType = "Free",
    )
    val idB = repo.upsertAccount(
      provider = "Ollama",
      userId = "emailB@test.com",
      email = "emailB@test.com",
      authToken = "",
      cookies = "session=B",
      userAgent = "ua-B",
      planType = "Free",
    )
    assertNotEquals("A and B must have different ids", idA, idB)

    // Re-auth A with new cookies
    val reAuthedA = repo.upsertAccount(
      provider = "Ollama",
      userId = "emailA@test.com",
      email = "emailA@test.com",
      authToken = "",
      cookies = "session=A-new",
      userAgent = "ua-A-new",
      planType = "Pro",
    )
    assertEquals("A keeps the same id", idA, reAuthedA)

    val accounts = dao.getAllAccounts().first()
    assertEquals("still 2 accounts", 2, accounts.size)

    val a = accounts.first { it.userId == "emailA@test.com" }
    val b = accounts.first { it.userId == "emailB@test.com" }
    assertEquals("session=A-new", FakeCredentialEncryptor().decrypt(a.cookies))
    assertEquals("Pro", a.planType)
    // B must be untouched
    assertEquals("session=B", FakeCredentialEncryptor().decrypt(b.cookies))
    assertEquals("ua-B", b.userAgent)
    assertEquals("Free", b.planType)
  }

  @Test
  fun upsert_preservesUsageLogHistoryOnUpdate() = runTest {
    val accountId = repo.upsertAccount(
      provider = "Ollama",
      userId = "emailA@test.com",
      email = "emailA@test.com",
      authToken = "",
      cookies = "session=old",
      userAgent = "ua-old",
      planType = "Free",
    )
    // Insert 3 historical logs
    repeat(3) { i ->
      repo.insertLog(
        UsageLog(
          accountId = accountId.toInt(),
          planType = "Free",
          primaryUsedPercent = 10.0 * i,
          primaryResetAt = 0L,
          primaryWindowSeconds = 3600L,
          primaryResetAfterSeconds = 0L,
          secondaryUsedPercent = 5.0 * i,
          secondaryResetAt = 0L,
          secondaryWindowSeconds = 7 * 24 * 3600L,
          secondaryResetAfterSeconds = 0L,
          hasCredits = false,
          unlimited = false,
          balance = "0",
          approxLocalUsed = 0,
          approxLocalLimit = 0,
          approxCloudUsed = 0,
          approxCloudLimit = 0,
        )
      )
    }

    // Re-auth (upsert) the same account
    val reAuthedId = repo.upsertAccount(
      provider = "Ollama",
      userId = "emailA@test.com",
      email = "emailA@test.com",
      authToken = "",
      cookies = "session=new",
      userAgent = "ua-new",
      planType = "Pro",
    )
    assertEquals(accountId, reAuthedId)

    // The 3 logs must still be bound to this accountId
    val logs = repo.getLogsForAccount(accountId.toInt()).first()
    assertEquals("UsageLog rows must be preserved after upsert", 3, logs.size)
    logs.forEach { assertEquals(accountId.toInt(), it.accountId) }
  }

  @Test
  fun getAccountByProviderAndUserId_returnsNullWhenAbsent() = runTest {
    val found = dao.getAccountByProviderAndUserId("Ollama", "nobody@test.com")
    assertNull(found)
  }

  @Test
  fun getAccountByProviderAndUserId_returnsRowWhenPresent() = runTest {
    repo.upsertAccount(
      provider = "Anthropic",
      userId = "org-uuid-123",
      email = "Claude Account",
      authToken = "sk-ant-x",
      cookies = "",
      userAgent = "",
      planType = "Pro",
    )
    val found = dao.getAccountByProviderAndUserId("Anthropic", "org-uuid-123")
    assertNotNull(found)
    assertEquals("org-uuid-123", found!!.userId)
  }
}
