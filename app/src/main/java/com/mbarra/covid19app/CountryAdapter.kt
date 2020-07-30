package com.mbarra.covid19app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.list_country.view.*
import org.w3c.dom.Text
import java.security.AlgorithmConstraints
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import java.util.Locale.ROOT
import kotlin.collections.ArrayList

class CountryAdapter (
    private var negara: ArrayList<Negara>,
    private val clickListener: (Negara) -> Unit
):
    RecyclerView.Adapter<CountryAdapter.ViewHolder>(), Filterable {

    var countryFilterList = ArrayList<Negara>()

    init {
        countryFilterList = negara
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_country, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return countryFilterList.size
    }

    override fun onBindViewHolder(holder: CountryAdapter.ViewHolder, position: Int) {
        holder.bind(countryFilterList[position], clickListener)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(negara: Negara, clickListener: (Negara) -> Unit) {

            // Membinding Data ke tempat Widget pada Model
            val country: TextView = itemView.country_name
            val cTotalCase: TextView = itemView.country_total_cases
            val cTotalRecovered: TextView = itemView.country_total_recovered
            val cTotalDeaths: TextView = itemView.country_total_deaths
            val flag: ImageView = itemView.img_flag_circle

            // Membuat Format Separator 1000
            val formatter: NumberFormat = DecimalFormat("#,####")

            // Injeksi Data ke Variable Widget
            country.text = negara.Country
            cTotalCase.text = formatter.format(negara.TotalConfirmed.toDouble())
            cTotalRecovered.text = formatter.format(negara.TotalRecovered.toDouble())
            cTotalDeaths.text = formatter.format(negara.TotalDeaths.toDouble())
            Glide.with(itemView)
                .load("https://www.countryflags.io/" + negara.CountryCode + "/flat/16.png")
                .into(flag)

            // Menjadikan Data Respon untuk di klik
            country.setOnClickListener { clickListener(negara) }
            cTotalCase.setOnClickListener { clickListener(negara) }
            cTotalRecovered.setOnClickListener { clickListener(negara) }
            cTotalDeaths.setOnClickListener { clickListener(negara) }
            flag.setOnClickListener { clickListener(negara) }
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraints: CharSequence?): FilterResults {
                var charSearch = constraints.toString()
                countryFilterList = if (charSearch.isEmpty()) {
                    negara
                } else {
                    val resultList = ArrayList<Negara>()
                    for (row in negara) {
                        if (row.Country.toLowerCase(Locale.ROOT)
                                .contains(charSearch.toLowerCase(Locale.ROOT))
                        ) {
                            resultList.add(row)
                        }
                    }
                    resultList
                }
                val filterResult = FilterResults()
                filterResult.values = countryFilterList
                return filterResult
            }

            override fun publishResults(constraints: CharSequence?, results: FilterResults?) {
                countryFilterList = results?.values as ArrayList<Negara>
                notifyDataSetChanged()
            }
        }
    }
}