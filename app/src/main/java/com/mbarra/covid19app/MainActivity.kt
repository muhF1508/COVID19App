package com.mbarra.covid19app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mbarra.covid19.ChartCountryActivity
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URI.create
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var adapters: CountryAdapter
    private var descending = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getCountry()

        search_view.setOnQueryTextListener(object :SearchView.OnQueryTextListener,
        androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapters.filter.filter(newText)
                return false
            }
        })

        btn_sequence.setOnClickListener {
            sequence(descending)
            descending = !descending
        }
    }

    private fun sequence(descending: Boolean) {
        recycler_view_country.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@MainActivity)

            if (descending) {
                (layoutManager as LinearLayoutManager).reverseLayout = true
                (layoutManager as LinearLayoutManager).stackFromEnd = true
            } else {
                (layoutManager as LinearLayoutManager).reverseLayout = false
                (layoutManager as LinearLayoutManager).stackFromEnd = false
            }
        }
    }

    private fun getCountry() {
        val okHttp = OkHttpClient().newBuilder()
            .connectTimeout(15, TimeUnit.SECONDS)// jika tidak respon selama 15 menit
            .readTimeout(15, TimeUnit.SECONDS)// jika data terkoneksi namun tidak ada data selama 15 detik
            .writeTimeout(15, TimeUnit.SECONDS)// jika device tidak mengirim data selama 15 detik
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.covid19api.com/")// alamat api
            .client(okHttp)// handler untuk transaksi
            .addConverterFactory(GsonConverterFactory.create())// convert data json
            .build()

        val api = retrofit.create(APIService::class.java)
        api.getAllNegara().enqueue(object : Callback<AllNegara> {
            override fun onFailure(call: Call<AllNegara>, t: Throwable) {
                progress_bar.visibility = View.GONE
                Toast.makeText(this@MainActivity, "Data Unreachable", Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<AllNegara>, response: Response<AllNegara>) {
                if (response.isSuccessful) {
                    progress_bar.visibility = View.GONE
                    Toast.makeText(this@MainActivity,"Data Success",Toast.LENGTH_SHORT).show()
                    val getDataListCorona = response.body()!!.Global
                    confirmed_globe.text = getDataListCorona.TotalConfirmed
                    recovered_globe.text = getDataListCorona.TotalRecovered
                    deaths_globe.text = getDataListCorona.TotalDeaths
                    recycler_view_country.apply {
                        setHasFixedSize(true)
                        layoutManager = LinearLayoutManager(this@MainActivity)
                        adapters = CountryAdapter(response.body()!!.Countries as ArrayList<Negara>) {
                            negara -> itemClicked(negara)
                        }
                        adapter = adapters
                    }
                } else {
                    progress_bar.visibility = View.GONE
                    Toast.makeText(this@MainActivity,"Data Unreachable", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun itemClicked(negara: Negara) {
        val pindahData = Intent(this, ChartCountryActivity::class.java)
        pindahData.putExtra(ChartCountryActivity.EXTRA_COUNTRY, negara.Country)
        pindahData.putExtra(ChartCountryActivity.EXTRA_LATESTUPDATE, negara.Date)
        pindahData.putExtra(ChartCountryActivity.EXTRA_NEWDEATH, negara.NewDeaths)
        pindahData.putExtra(ChartCountryActivity.EXTRA_NEWCONFIRMED, negara.NewConfirmed)
        pindahData.putExtra(ChartCountryActivity.EXTRA_NEWRECOVERED, negara.NewRecovered)
        pindahData.putExtra(ChartCountryActivity.EXTRA_TOTALDEATH, negara.TotalDeaths)
        pindahData.putExtra(ChartCountryActivity.EXTRA_TOTALCONFIRMED, negara.TotalConfirmed)
        pindahData.putExtra(ChartCountryActivity.EXTRA_TOTALRECOVERED, negara.TotalRecovered)
        pindahData.putExtra(ChartCountryActivity.EXTRA_COUNTRYID, negara.CountryCode)
        startActivity(pindahData)
    }
}