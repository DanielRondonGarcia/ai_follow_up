package com.example.data

import kotlinx.coroutines.flow.Flow

class UsageRepository(private val db: AppDatabase) {
    private val accountDao = db.accountDao()
    private val usageLogDao = db.usageLogDao()

    val allAccounts: Flow<List<Account>> = accountDao.getAllAccounts()
    val activeAccountFlow: Flow<Account?> = accountDao.getActiveAccountFlow()
    val allLogsFlow: Flow<List<UsageLog>> = usageLogDao.getAllLogsFlow()

    suspend fun getActiveAccount(): Account? = accountDao.getActiveAccount()

    suspend fun getAccountByUserId(userId: String): Account? = accountDao.getAccountByUserId(userId)

    suspend fun insertAccount(account: Account): Long = accountDao.insertAccount(account)

    suspend fun updateAccount(account: Account) = accountDao.updateAccount(account)

    /**
     * Upsert an account by (provider, userId). Builds an Account from the
     * given fields with lastUpdated=now, then delegates to the DAO upsert
     * transaction. If a row with the same (provider, userId) exists, it is
     * updated in place (same id, same UsageLog history); otherwise a new row
     * is inserted.
     */
    suspend fun upsertAccount(
        provider: String,
        userId: String,
        email: String,
        authToken: String,
        cookies: String,
        userAgent: String,
        planType: String,
        isActive: Boolean = true,
    ): Long {
        val account = Account(
            provider = provider,
            email = email,
            userId = userId,
            planType = planType,
            authToken = authToken,
            cookies = cookies,
            userAgent = userAgent,
            isActive = isActive,
            lastUpdated = System.currentTimeMillis(),
        )
        return accountDao.upsertAccount(account)
    }

    suspend fun deleteAccount(account: Account) {
        usageLogDao.deleteLogsForAccount(account.id)
        accountDao.deleteAccount(account)
    }

    suspend fun setActiveAccount(accountId: Int) = accountDao.setActiveAccount(accountId)

    fun getLogsForAccount(accountId: Int): Flow<List<UsageLog>> = usageLogDao.getLogsForAccount(accountId)

    suspend fun insertLog(log: UsageLog) = usageLogDao.insertLog(log)
}
