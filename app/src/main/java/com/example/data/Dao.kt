package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY email ASC")
    fun getAllAccounts(): Flow<List<Account>>

    @Query("SELECT * FROM accounts WHERE isActive = 1 LIMIT 1")
    fun getActiveAccountFlow(): Flow<Account?>

    @Query("SELECT * FROM accounts WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveAccount(): Account?

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: Int): Account?

    @Query("SELECT * FROM accounts WHERE provider = :provider AND userId = :userId LIMIT 1")
    suspend fun getAccountByProviderAndUserId(provider: String, userId: String): Account?

    @Query("SELECT * FROM accounts WHERE userId = :userId LIMIT 1")
    suspend fun getAccountByUserId(userId: String): Account?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: Account): Long

    @Update
    suspend fun updateAccount(account: Account)

    @Delete
    suspend fun deleteAccount(account: Account)

    @Query("UPDATE accounts SET isActive = 0")
    suspend fun deactivateAll()

    @Transaction
    suspend fun setActiveAccount(accountId: Int) {
        deactivateAll()
        val account = getAccountById(accountId)
        if (account != null) {
            updateAccount(account.copy(isActive = true))
        }
    }

    /**
     * Upsert by (provider, userId). Looks up the existing row; if found,
     * updates it in place (preserving id + UsageLog history) and returns the
     * existing id. If not found, inserts a new row and returns the new id.
     *
     * No schema change, no unique index: this avoids a v3 destructive
     * migration while enforcing the (provider, userId) invariant atomically
     * under @Transaction.
     */
    @Transaction
    suspend fun upsertAccount(account: Account): Long {
        val existing = getAccountByProviderAndUserId(account.provider, account.userId)
        return if (existing != null) {
            updateAccount(account.copy(id = existing.id))
            existing.id.toLong()
        } else {
            insertAccount(account)
        }
    }
}

@Dao
interface UsageLogDao {
    @Query("SELECT * FROM usage_logs ORDER BY timestamp DESC")
    fun getAllLogsFlow(): Flow<List<UsageLog>>

    @Query("SELECT * FROM usage_logs WHERE accountId = :accountId ORDER BY timestamp DESC")
    fun getLogsForAccount(accountId: Int): Flow<List<UsageLog>>

    @Query("SELECT * FROM usage_logs WHERE accountId = :accountId ORDER BY timestamp DESC LIMIT :limit")
    fun getLatestLogsForAccount(accountId: Int, limit: Int): Flow<List<UsageLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: UsageLog)

    @Query("DELETE FROM usage_logs WHERE accountId = :accountId")
    suspend fun deleteLogsForAccount(accountId: Int)
}
