package com.vest.bag.login

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator

/**
 *
 * @author xx
 * 2023/5/19 11:15
 */
data class AuthorizationInfo(
    val id: String,
    var email: String = "",
    var firstName: String = "",
    var lastName: String = "",


    ) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(email)
        parcel.writeString(firstName)
        parcel.writeString(lastName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<AuthorizationInfo> {
        override fun createFromParcel(parcel: Parcel): AuthorizationInfo {
            return AuthorizationInfo(parcel)
        }

        override fun newArray(size: Int): Array<AuthorizationInfo?> {
            return arrayOfNulls(size)
        }
    }
}

data class ErrorInfo(
    val errorCode: String,
    val message: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(errorCode)
        parcel.writeString(message)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<ErrorInfo> {
        override fun createFromParcel(parcel: Parcel): ErrorInfo {
            return ErrorInfo(parcel)
        }

        override fun newArray(size: Int): Array<ErrorInfo?> {
            return arrayOfNulls(size)
        }
    }
}

sealed class LoginType {

    object FacebookLogin : LoginType()

    object GoogleLogin : LoginType()

    object TwitterLogin : LoginType()

    object LinkedInLogin : LoginType()
}


