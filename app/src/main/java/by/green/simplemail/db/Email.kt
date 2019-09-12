package by.green.simplemail.db

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable

@Entity(
    tableName = "emails",
    foreignKeys = [
        ForeignKey(
            entity = EmailFolder::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.CASCADE
        )]
)

data class Email(
    @PrimaryKey(autoGenerate = true) var id: Long? = null,
    val folderId: Long,
    val folderUrl:String,
    var subject: String? = null,
    val folderName: String,
    val email_id: String,
    var short_content: String? = null,
    var threadId: String? = null,
    var date: String? = null,
    var from_title: String? = null,
    var from_email: String? = null,
    var unread: Boolean? = null,
    var nextPageToken: String? = null,
    val msgNum: Int? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Long::class.java.classLoader) as  Long,
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeLong(folderId)
        parcel.writeString(folderUrl)
        parcel.writeString(subject)
        parcel.writeString(folderName)
        parcel.writeString(email_id)
        parcel.writeString(short_content)
        parcel.writeString(threadId)
        parcel.writeString(date)
        parcel.writeString(from_title)
        parcel.writeString(from_email)
        parcel.writeValue(unread)
        parcel.writeString(nextPageToken)
        parcel.writeValue(msgNum)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Email> {
        override fun createFromParcel(parcel: Parcel): Email {
            return Email(parcel)
        }

        override fun newArray(size: Int): Array<Email?> {
            return arrayOfNulls(size)
        }
    }


}
