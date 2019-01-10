package com.belyaev.artem.agzs_diskont.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass
open class Station (
        @PrimaryKey open var idOfRealm: Long = 0,
        open var name: String = "DataObject",
        open var idOfService: String = "001"

) : RealmObject() {

    override fun toString(): String {
        return name
    }
}