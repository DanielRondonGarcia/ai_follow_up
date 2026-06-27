package com.example.ui

import com.example.data.UsageLog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure-logic unit test for [latestLogPerAccount] (Task 2.8).
 *
 * Verifies the derivation correctness without instantiating MainViewModel,
 * which would require an Application context and a Room database. The function
 * under test is a top-level pure function extracted from the ViewModel for
 * exactly this reason.
 */
class LatestLogPerAccountTest {

  private fun log(accountId: Int, timestamp: Long, plan: String = "plus"): UsageLog =
    UsageLog(
      accountId = accountId,
      timestamp = timestamp,
      planType = plan,
      primaryUsedPercent = 0.0,
      primaryResetAt = 0L,
      primaryWindowSeconds = 0L,
      primaryResetAfterSeconds = 0L,
      secondaryUsedPercent = 0.0,
      secondaryResetAt = 0L,
      secondaryWindowSeconds = 0L,
      secondaryResetAfterSeconds = 0L,
      hasCredits = false,
      unlimited = false,
      balance = "0",
      approxLocalUsed = 0,
      approxLocalLimit = 0,
      approxCloudUsed = 0,
      approxCloudLimit = 0,
    )

  @Test
  fun emptyList_returnsEmptyMap() {
    val result = latestLogPerAccount(emptyList())
    assertTrue(result.isEmpty())
  }

  @Test
  fun singleAccount_singleLog_returnsThatLog() {
    val l1 = log(accountId = 1, timestamp = 100L)
    val result = latestLogPerAccount(listOf(l1))
    assertEquals(1, result.size)
    assertEquals(l1, result[1])
  }

  @Test
  fun singleAccount_multipleLogs_returnsLatestByTimestamp() {
    val old = log(accountId = 1, timestamp = 100L, plan = "old")
    val newer = log(accountId = 1, timestamp = 300L, plan = "newer")
    val newest = log(accountId = 1, timestamp = 200L, plan = "mid")
    val result = latestLogPerAccount(listOf(old, newest, newer))
    assertEquals(1, result.size)
    // newest by timestamp is the one at 300, regardless of insertion order
    assertEquals("newer", result[1]?.planType)
  }

  @Test
  fun multipleAccounts_returnsLatestPerAccount() {
    val a1Old = log(accountId = 1, timestamp = 100L, plan = "a1-old")
    val a1New = log(accountId = 1, timestamp = 500L, plan = "a1-new")
    val a2Old = log(accountId = 2, timestamp = 200L, plan = "a2-old")
    val a2New = log(accountId = 2, timestamp = 400L, plan = "a2-new")
    // Interleave to ensure grouping is by accountId not insertion order
    val result = latestLogPerAccount(listOf(a2Old, a1Old, a2New, a1New))
    assertEquals(2, result.size)
    assertEquals("a1-new", result[1]?.planType)
    assertEquals("a2-new", result[2]?.planType)
  }

  @Test
  fun accountWithNoLogs_isAbsentFromMap() {
    val l1 = log(accountId = 1, timestamp = 100L)
    val result = latestLogPerAccount(listOf(l1))
    // Account 2 was never present in the logs, so it is not a key
    assertNull(result[2])
    assertEquals(1, result.size)
  }
}