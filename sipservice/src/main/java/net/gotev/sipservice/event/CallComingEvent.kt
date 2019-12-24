package net.gotev.sipservice.event

import android.os.Parcel
import android.os.Parcelable


/**
 *
 * @author zhangli
 * @email zhanglihow@gmail.com
 * @time
 */
class CallComingEvent(val accountID:String,
                      val callID:Int,
                      val displayName:String) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readInt(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(accountID)
        parcel.writeInt(callID)
        parcel.writeString(displayName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CallComingEvent> {
        override fun createFromParcel(parcel: Parcel): CallComingEvent {
            return CallComingEvent(parcel)
        }

        override fun newArray(size: Int): Array<CallComingEvent?> {
            return arrayOfNulls(size)
        }
    }
}