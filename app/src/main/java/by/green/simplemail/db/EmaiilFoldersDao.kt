package by.green.simplemail.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface EmailFoldersDao {
    @Query("select * from email_folders where emailAccountId=:accountId")
    fun getFolders(accountId: Long): LiveData<List<EmailFolder>>

    @Query("delete from email_folders where emailAccountId=:accountId")
    fun clearFolders(accountId: Long)

    @Delete
    fun deleteFolders(folders: List<EmailFolder>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertFolders(folders: List<EmailFolder>)

    @Update
    fun updateFolders(folders: List<EmailFolder>): Int

    @Query("select * from email_folders where emailAccountId=:accountId")
    fun getFoldersList(accountId: Long): List<EmailFolder>

    @Transaction
    fun updateData(accountId: Long, folders: List<EmailFolder>?) {

        val eFolders = getFoldersList(accountId)
        val foldersToRemove = ArrayList<EmailFolder>()
        val map = HashMap<Long, EmailFolder>()
        if (eFolders != null) {
            for (folder in folders ?: return) {
                map[folder.id] = folder
            }

            for (folder in eFolders) {
                if (!map.containsKey(folder.id)) {
                    foldersToRemove.add(folder)
                }
            }

            if (!foldersToRemove.isEmpty())
                deleteFolders(foldersToRemove)
        }
        insertFolders(folders ?: return)
    }
}