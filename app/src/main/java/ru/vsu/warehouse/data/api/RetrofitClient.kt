package ru.vsu.warehouse.data.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object RetrofitClient {
//    private const val BASE_URL = "http://10.0.2.2:8080/"
    private const val BASE_URL = "http://192.168.220.189:8080/"
//    private const val BASE_URL = "http://192.168.0.103:8080/"


    private val gson: Gson by lazy {
        GsonBuilder()
            .registerTypeAdapter(LocalDate::class.java, LocalDateTypeAdapter())
            .create()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val api: WarehouseApi by lazy {
        retrofit.create(WarehouseApi::class.java)
    }

    class LocalDateTypeAdapter : TypeAdapter<LocalDate>() {
        private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

        override fun write(out: JsonWriter, value: LocalDate?) {
            out.value(value?.format(formatter))
        }

        override fun read(`in`: JsonReader): LocalDate? {
            return `in`.nextString().let {
                if (it.isEmpty()) null else LocalDate.parse(it, formatter)
            }
        }
    }
}