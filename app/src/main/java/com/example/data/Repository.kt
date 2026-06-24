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

    suspend fun deleteAccount(account: Account) {
        usageLogDao.deleteLogsForAccount(account.id)
        accountDao.deleteAccount(account)
    }

    suspend fun setActiveAccount(accountId: Int) = accountDao.setActiveAccount(accountId)

    fun getLogsForAccount(accountId: Int): Flow<List<UsageLog>> = usageLogDao.getLogsForAccount(accountId)

    suspend fun insertLog(log: UsageLog) = usageLogDao.insertLog(log)
}
