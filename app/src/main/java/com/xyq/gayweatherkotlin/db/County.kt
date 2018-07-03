package com.xyq.gayweatherkotlin.db

import org.litepal.crud.DataSupport

class County : DataSupport() {

    var id : Int = 0

    var countyName: String ?= null

    var weatherId: String ?= null

    var cityId: Int = 0
}