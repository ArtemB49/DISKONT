package com.belyaev.artem.agzs_diskont.model

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONArray

/**
 * Created by Artem on 22.11.2017.
 */
data class GazStationParcelable(
        val id: String,
        val name: String,
        val address: String,
        val number: Int,
        val prefix: String

) : Parcelable {



    constructor(jsonArray: JSONArray)
            :this(
            id = jsonArray.getString(1),
            name = jsonArray.getString(2),
            address = jsonArray.getString(3),
            number = jsonArray.getInt(4),
            prefix = jsonArray.getString(5)
    )

    constructor(parcel: Parcel)
            :this(
            id = parcel.readString(),
            name = parcel.readString(),
            address = parcel.readString(),
            number = parcel.readInt(),
            prefix = parcel.readString()
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(id)
        dest?.writeString(name)
        dest?.writeString(address)
        dest?.writeInt(number)
        dest?.writeString(prefix)
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<GazStationParcelable>{
            override fun createFromParcel(source: Parcel): GazStationParcelable? = GazStationParcelable(source)
            override fun newArray(size: Int): Array<GazStationParcelable?> = arrayOfNulls(size)
        }
    }


}