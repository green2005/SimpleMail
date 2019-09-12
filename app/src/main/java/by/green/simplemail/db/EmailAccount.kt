package by.green.simplemail.db

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable

@Entity (tableName = "email_accounts")
data class EmailAccount(
    @PrimaryKey(autoGenerate = true) var id: Long? = null,
    val email: String,
    var pwd: String,
    var incomingServer: String?,
    var incomingPort: Int?,
    var outServer: String?,
    var outPort: Int?,
    var incomingSecuritySettings: String? = "",
    var outSecuritySettings: String? = "",
    var isActive:Byte = 0

) : Parcelable {
    constructor(parcel: Parcel) : this(
        id = null,
        email = parcel.readString(),
        pwd = parcel.readString(),
        incomingServer = parcel.readString(),
        incomingPort = parcel.readInt(),
        outServer = parcel.readString(),
        outPort = parcel.readInt(),
        incomingSecuritySettings = parcel.readString(),
        outSecuritySettings = parcel.readString(),
        isActive = parcel.readByte()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(email)
        parcel.writeString(pwd)
        parcel.writeString(incomingServer)
        parcel.writeInt(incomingPort?:0)
        parcel.writeString(outServer)
        parcel.writeInt(outPort?:0)
        parcel.writeString(incomingSecuritySettings)
        parcel.writeString(outSecuritySettings)
        parcel.writeByte(isActive?:0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<EmailAccount> {
        override fun createFromParcel(parcel: Parcel): EmailAccount {
            return EmailAccount(parcel)
        }

        override fun newArray(size: Int): Array<EmailAccount?> {
            return arrayOfNulls(size)
        }
    }
}