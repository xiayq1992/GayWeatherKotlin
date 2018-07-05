package com.xyq.gayweatherkotlin.util

import com.xyq.gayweatherkotlin.db.City
import com.xyq.gayweatherkotlin.db.County
import com.xyq.gayweatherkotlin.db.Province
import org.json.JSONArray
import org.json.JSONException

/**
 * 数据处理类
 */
object Utility {

    /**
     * 解析并保存省级数据
     */
    fun handleProvinceResponse(response: String) : Boolean{
        try {

            val allProvince = JSONArray(response)
            if (allProvince.length() > 0) {
                for (i in 0..(allProvince.length() - 1)) {
                    val jsonObject = allProvince.getJSONObject(i)
                    val province = Province()
                    province.provinceName = jsonObject.getString("name")
                    province.provinceCode = jsonObject.getInt("id")
                    province.save()
                }
                return true
            }
        }catch (e: JSONException) {
            e.printStackTrace()
        }
        return false;
    }


    /**
     * 解析并保存市级数据
     */
    fun handleCityResponse(response: String, provinceId: Int) : Boolean{
        try {

            val allCities = JSONArray(response)
            if (allCities.length() > 0) {
                for (i in 0..(allCities.length() - 1)) {
                    val jsonObject = allCities.getJSONObject(i)
                    val city = City()
                    city.cityName = jsonObject.getString("name")
                    city.cityCode = jsonObject.getInt("id")
                    city.provinceId = provinceId
                    city.save()
                }
                return true
            }
        }catch (e: JSONException) {
            e.printStackTrace()
        }
        return false;
    }


    /**
     * 解析并保存县级数据
     */
    fun handleCountyResponse(response: String, cityId: Int) : Boolean{
        try {

            val allCounties = JSONArray(response)
            if (allCounties.length() > 0) {
                for (i in 0..(allCounties.length() - 1)) {
                    val jsonObject = allCounties.getJSONObject(i)
                    val county = County()
                    county.countyName = jsonObject.getString("name")
                    county.weatherId = jsonObject.getString("weather_id")
                    county.cityId = cityId
                    county.save()
                }
                return true
            }
        }catch (e: JSONException) {
            e.printStackTrace()
        }
        return false;
    }



}