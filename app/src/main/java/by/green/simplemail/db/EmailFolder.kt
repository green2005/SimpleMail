package by.green.simplemail.db

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable

@Entity(
    tableName = "email_folders" /*,
     foreignKeys = [
        ForeignKey(
            entity = EmailAccount::class,
            parentColumns = ["id"],
            childColumns = ["emailAccountId"],
            onDelete = CASCADE
        )]
        */
)

data class EmailFolder(
    @PrimaryKey var id: Long,
    val emailAccountId: Long,
    val email: String,
    val name: String,
    val folder_id: String,
    var show: Boolean,
    var type: String? = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Long::class.java.classLoader) as  Long,
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeLong(emailAccountId)
        parcel.writeString(email)
        parcel.writeString(name)
        parcel.writeString(folder_id)
        parcel.writeByte(if (show) 1 else 0)
        parcel.writeString(type)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<EmailFolder> {
        override fun createFromParcel(parcel: Parcel): EmailFolder {
            return EmailFolder(parcel)
        }

        override fun newArray(size: Int): Array<EmailFolder?> {
            return arrayOfNulls(size)
        }
    }

}
