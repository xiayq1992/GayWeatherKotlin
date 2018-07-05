package com.xyq.gayweatherkotlin

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.xyq.gayweatherkotlin.contants.Constants
import com.xyq.gayweatherkotlin.contants.DataQueryType
import com.xyq.gayweatherkotlin.db.City
import com.xyq.gayweatherkotlin.db.County
import com.xyq.gayweatherkotlin.db.Province
import com.xyq.gayweatherkotlin.util.HttpUtil
import com.xyq.gayweatherkotlin.util.Utility
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.litepal.crud.DataSupport
import java.io.IOException
import java.util.ArrayList

/**
 *
 * created by xyq
 * created on 2018/7/4
 */
class ChooseAreaFragment : Fragment() {

    private var progressDialog: ProgressDialog? = null

    private var titleText: TextView? = null

    private var ivBack: ImageView? = null

    private var listView: ListView? = null

    private var adapter: ArrayAdapter<String>? = null

    private val dataList = ArrayList<String>()

    private var provinceList: List<Province>? = null

    private var cityList: List<City>? = null

    private var countyList: List<County>? = null

    private var selectedProvince: Province? = null

    private var selectedCity: City? = null

    private var currentLevel: DataQueryType = DataQueryType.PROVINCE

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater!!.inflate(R.layout.choose_area, container, false)

        titleText = view.findViewById<TextView>(R.id.tv_title)
        ivBack = view.findViewById(R.id.iv_back)
        listView = view.findViewById(R.id.list_view)

        adapter = ArrayAdapter(activity, android.R.layout.simple_list_item_1, dataList)
        listView!!.adapter = adapter
        return view
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        listView!!.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            if (currentLevel == DataQueryType.PROVINCE) {
                selectedProvince = provinceList!![position]
                queryCities()
            } else if (currentLevel == DataQueryType.CITY) {
                selectedCity = cityList!![position]
                queryCounties()
            } else if (currentLevel == DataQueryType.COUNTY) {
                val weatherId = countyList!![position].weatherId
                if (activity is MainActivity) {
                    val intent = Intent(activity, WeatherActivity::class.java)
                    intent.putExtra("weather_id", weatherId)
                    startActivity(intent)
                    activity.finish()
                } /*else if (activity is WeatherActivity) {
                    val activity = activity as WeatherActivity
                    activity.drawerLayout.closeDrawers()
                    activity.swipeRefreshLayout.setRefreshing(true)
                    activity.requestWeather(weatherId)
                }*/
            }
        }

        ivBack!!.setOnClickListener {
            if (currentLevel == DataQueryType.COUNTY) {
                queryCities()
            } else if (currentLevel == DataQueryType.CITY) {
                queryProvinces()
            }
        }

        queryProvinces()
    }

    /**
     * 查询省
     */
    private fun queryProvinces() {
        titleText!!.setText("中国")
        ivBack!!.visibility = View.GONE
        provinceList = DataSupport.findAll(Province::class.java)
        if (provinceList!!.size > 0) {
            dataList.clear()
            for (province in provinceList!!) {
                dataList.add(province.provinceName!!)
            }
            notifyAndSetLevel(DataQueryType.PROVINCE)
        } else {
            val address = Constants.BASE_URL + "china"
            queryFromServer(address, DataQueryType.PROVINCE)

        }
    }


    /**
     * 查询城市
     */
    private fun queryCities() {
        titleText!!.setText(selectedProvince!!.provinceName)
        ivBack!!.visibility = View.VISIBLE
        cityList = DataSupport.where("provinceid = ?", selectedProvince!!.id.toString()).find(City::class.java)
        if (cityList!!.size > 0) {
            dataList.clear()
            for (city in cityList!!) {
                dataList.add(city.cityName!!)
            }
            notifyAndSetLevel(DataQueryType.CITY)
        } else {
            val provinceCode = selectedProvince!!.provinceCode
            val address = Constants.BASE_URL + "china/" + provinceCode
            queryFromServer(address, DataQueryType.CITY)
        }
    }


    /**
     * 查询区县
     */
    private fun queryCounties() {
        titleText!!.setText(selectedCity!!.cityName)
        ivBack!!.visibility = View.VISIBLE
        countyList = DataSupport.where("cityid = ?", selectedCity!!.id.toString()).find(County::class.java)
        if (countyList!!.size > 0) {
            dataList.clear()
            for (county in countyList!!) {
                dataList.add(county.countyName!!)
            }
            notifyAndSetLevel(DataQueryType.COUNTY)
        } else {
            val provinceCode = selectedProvince!!.provinceCode
            val cityCode = selectedCity!!.cityCode
            val address = Constants.BASE_URL + "china/" + provinceCode + "/" + cityCode
            queryFromServer(address, DataQueryType.COUNTY)
        }
    }


    private fun notifyAndSetLevel(level: DataQueryType) {
        adapter!!.notifyDataSetChanged()
        listView!!.setSelection(0)
        currentLevel = level
    }

    private fun queryFromServer(address: String, type: DataQueryType) {
        showProgressDialog()
        HttpUtil.sendOkHttpRequest(address, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity.runOnUiThread {
                    closeProgressDialog()
                    Toast.makeText(activity, "加载失败", Toast.LENGTH_SHORT).show()
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val responseText = response.body().string()
                var result = false
                if (DataQueryType.PROVINCE == type) {
                    result = Utility.handleProvinceResponse(responseText)
                } else if (DataQueryType.CITY == type) {
                    result = Utility.handleCityResponse(responseText, selectedProvince!!.id)
                } else if (DataQueryType.COUNTY == type) {
                    result = Utility.handleCountyResponse(responseText, selectedCity!!.id)
                }

                if (result) {
                    activity.runOnUiThread {
                        closeProgressDialog()
                        if (DataQueryType.PROVINCE == type) {
                            queryProvinces()
                        } else if (DataQueryType.CITY == type) {
                            queryCities()
                        } else if (DataQueryType.COUNTY == type) {
                            queryCounties()
                        }
                    }
                }
            }
        })
    }

    private fun showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(activity)
            progressDialog!!.setMessage("正在加载...")
            progressDialog!!.setCanceledOnTouchOutside(false)
        }
        progressDialog!!.show()
    }

    private fun closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog!!.dismiss()
        }
    }
}