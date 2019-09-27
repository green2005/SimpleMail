package by.green.simplemail.db

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.IGNORE

@Dao
interface EmailAccountsDao {
    @Query("select * from email_accounts")
    fun getAccounts(): LiveData<List<EmailAccount>>

    @Insert(onConflict = IGNORE)
    fun insert(account: EmailAccount)

    @Transaction
    fun addAccount(account: EmailAccount) {
        setAccountsInActive()
        insert(account)
    }

    @Delete
    fun delete(account: EmailAccount)

    @Query("update email_accounts set isActive = 0")
    fun setAccountsInActive()

    @Query("update email_accounts set isActive = 1 where id=:id")
    fun doSetActiveAccount(id: Long)

    @Query("select * from email_accounts where email = :email")
    fun getAccountData(email: String): EmailAccount?

    @Transaction
    fun setActiveAccount(id: Long) {
        setAccountsInActive()
        doSetActiveAccount(id)
    }

    @Query(
        "update email_accounts set incomingServer = :incomingServer, incomingPort=:incomingPort," +
                " outServer=:outServer, " +
                " outPort=:outPort, pwd = :pwd " +
                " where email=:email"
    )
    fun updateAccountSettings(
        email: String,
        pwd: String,
        incomingServer: String,
        incomingPort: Int,
        outServer: String,
        outPort: Int
    )

}