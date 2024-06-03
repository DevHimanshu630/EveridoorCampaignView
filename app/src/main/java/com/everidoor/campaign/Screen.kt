package com.everidoor.campaign

import android.os.Parcel
import android.os.Parcelable

data class Screen(
    val screenId: String,
    val screenName: String,
    val type: String,
    val typeVal: String,
    val username: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(screenId)
        parcel.writeString(screenName)
        parcel.writeString(type)
        parcel.writeString(typeVal)
        parcel.writeString(username)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Screen> {
        override fun createFromParcel(parcel: Parcel): Screen {
            return Screen(parcel)
        }

        override fun newArray(size: Int): Array<Screen?> {
            return arrayOfNulls(size)
        }
    }
}
