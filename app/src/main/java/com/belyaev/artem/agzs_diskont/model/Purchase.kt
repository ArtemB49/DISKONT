package com.belyaev.artem.agzs_diskont.model

import io.realm.RealmObject
import java.util.*

open class Purchase (
    open var date: Date = Date(),
    open var station: Station? = null,
    open var gas: Double = 0.0,
    open var price: Double = 0.0,
    open var discount: Double = 0.0,
    open var state: Boolean? = null

) : RealmObject()