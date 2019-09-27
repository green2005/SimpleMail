package by.green.simplemail.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query

@Dao
interface EmailDao {

    @Query("select * from emails where folderId = :folderId")
    fun getEmails(folderId: Long): LiveData<List<Email>>

    @Query("delete from emails where folderId = :folderId")
    fun deleteEmails(folderId: Long)

    @Query("delete from emails where id = :emailId  ")
    fun deleteEmail(emailId: Long)

    @Query("update emails set unread =:unRead where id = :emailId  ")
    fun setEmailRead(unRead: Boolean, emailId: Long)

    @Insert(onConflict = REPLACE)
    fun insertEmails(emails: List<Email>)


}