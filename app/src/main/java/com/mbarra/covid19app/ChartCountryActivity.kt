package com.mbarra.covid19

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.mbarra.covid19app.InfoNegara
import com.mbarra.covid19app.InfoService
import com.mbarra.covid19app.R
import kotlinx.android.synthetic.main.activity_chart_country.*
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ChartCountryActivity : AppCompatActivity() {

    companion object {
        var EXTRA_COUNTRY: String = "extra_country"
        var EXTRA_LATESTUPDATE: String = "latestupdate"
        var EXTRA_NEWDEATH: String = "newdeath"
        var EXTRA_NEWCONFIRMED: String = "newconfirmed"
        var EXTRA_NEWRECOVERED: String = "newrecovered"
        var EXTRA_TOTALDEATH: String = "totaldeath"
        var EXTRA_TOTALCONFIRMED: String = "totalconfirmed"
        var EXTRA_TOTALRECOVERED: String = "totalrecovered"
        var EXTRA_COUNTRYID: String = "countryid"

    }

    private var tNegara: String? = ""
    private var daycases = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart_country)

        tNegara = intent.getStringExtra(EXTRA_COUNTRY)
        val tTangal = intent.getStringExtra(EXTRA_LATESTUPDATE)
        val tnDeath = intent.getStringExtra(EXTRA_NEWDEATH)
        val tnConfirmed = intent.getStringExtra(EXTRA_NEWCONFIRMED)
        val tnRecovered = intent.getStringExtra(EXTRA_NEWRECOVERED)
        val tDeath = intent.getStringExtra(EXTRA_TOTALDEATH)
        val tConfirmed = intent.getStringExtra(EXTRA_TOTALCONFIRMED)
        val tRecovered = intent.getStringExtra(EXTRA_TOTALRECOVERED)
        val tCountryId = intent.getStringExtra(EXTRA_COUNTRYID)

        latest_update.text = tTangal
        txt_name_country.text = tNegara
        hasil_total_deaths_currently.text = tDeath
        hasil_new_deaths_currently.text = tnDeath
        hasil_new_confirmed_currently.text = tnConfirmed
        hasil_new_recovery_currently.text = tnRecovered
        hasil_total_confirmed_currently.text = tConfirmed
        hasil_total_recovery_currently.text = tRecovered
        Glide.with(this).load("https://www.countryflags.io/" + tCountryId + "/flat/64.png").into(img_flag_country)

        chartDataView()

    }

    private fun chartDataView() {
        val okHttp = OkHttpClient().newBuilder()
            .connectTimeout(15, TimeUnit.SECONDS)// jika data tidak ada respon selama 15 detik
            .readTimeout(15, TimeUnit.SECONDS)//  jika data terkoneksi namun tidak ada data selama 15 detik
            .writeTimeout(15, TimeUnit.SECONDS)// jika Device tidak mengirim data selama 15 detik
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.covid19api.com/dayone/country/")// sebuah alamat api yg dituju
            .client(okHttp)// handler data transaksi
            .addConverterFactory(GsonConverterFactory.create())// convert data json
            .build()// membuild data

        val api = retrofit.create(InfoService::class.java)
        api.getInfoService(tNegara!!).enqueue(object  : Callback<List<InfoNegara>>{
            override fun onFailure(call: Call<List<InfoNegara>>, t: Throwable) {
                TODO("Not yet implemented")
            }

            override fun onResponse(call: Call<List<InfoNegara>>, response: Response<List<InfoNegara>>) {
                if (response.isSuccessful){
                    var getListDataCorona: List<InfoNegara> = response.body()!!

                    val barEntriesConfirmed: ArrayList<BarEntry> = ArrayList()
                    val barEntriesDeaths: ArrayList<BarEntry> = ArrayList()
                    val barEntriesRecovered: ArrayList<BarEntry> = ArrayList()
                    val barEntriesActive: ArrayList<BarEntry> = ArrayList()

                    var i = 0
                    while (i < getListDataCorona.size){
                        for (a in getListDataCorona){
                            val barEntryConfirmed = BarEntry(i.toFloat(), a.Confirmed.toFloat())
                            val barEntryDeaths = BarEntry(i.toFloat(), a.Deaths.toFloat())
                            val barEntryRecovered = BarEntry(i.toFloat(), a.Recovered.toFloat())
                            val barentryActive = BarEntry(i.toFloat(), a.Active.toFloat())

                            barEntriesConfirmed.add(barEntryConfirmed)
                            barEntriesDeaths.add(barEntryDeaths)
                            barEntriesRecovered.add(barEntryRecovered)
                            barEntriesActive.add(barentryActive)

                            daycases.add(a.Date)

                            i++
                        }
                    }

                    val barDataSetRecovered = BarDataSet(barEntriesRecovered,"Recovered")
                    val barDataSetDeaths = BarDataSet(barEntriesDeaths,"Deaths")
                    val barDataSetConfirmed = BarDataSet(barEntriesConfirmed,"Confirmed")
                    val barDataSetActive = BarDataSet(barEntriesActive,"Active")
                    barDataSetRecovered.setColor(Color.BLUE)
                    barDataSetConfirmed.setColor(Color.GREEN)
                    barDataSetDeaths.setColor(Color.RED)
                    barDataSetActive.setColor(Color.YELLOW)

                    // untuk menampilkan dan mengatur x atau horizontal data
                    val x: XAxis = barChartView.xAxis
                    barChartView.axisLeft.axisMinimum = 0f
                    x.position = XAxis.XAxisPosition.BOTTOM
                    x.granularity = 1f
                    x.setCenterAxisLabels(true)
                    x.isGranularityEnabled = true

                    // untuk menampilkan barchart Y atau vertical data
                    val data = BarData(barDataSetRecovered, barDataSetDeaths, barDataSetConfirmed, barDataSetActive)
                    barChartView.data = data

                    // mengatur keseluruhan view barchart
                    data.barWidth = 0.15f
                    barChartView.invalidate()// bisa tampil tanpa di klik widgetnya
                    barChartView.setNoDataTextColor(Color.BLACK)
                    barChartView.setTouchEnabled(true)
                    barChartView.description.isEnabled = true
                    barChartView.xAxis.axisMinimum = 0f
                    barChartView.groupBars(0f,0.3f,0.02f)
                    barChartView.setVisibleXRangeMaximum(0f + barChartView.barData.getGroupWidth(0.3f,0.02f)*4f)

                } else {

                }
            }
        })
    }
}