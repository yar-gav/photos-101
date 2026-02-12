package com.example.photos101.di

import com.example.photos101.BuildConfig
import com.example.photos101.data.remote.FlickrApi
import com.example.photos101.data.remote.FlickrApiKeyInterceptor
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

val networkModule = module {

    single { BuildConfig.FLICKR_API_KEY }

    single<OkHttpClient> {
        OkHttpClient.Builder()
            .addInterceptor(FlickrApiKeyInterceptor(get()))
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    single {
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            encodeDefaults = true
        }
    }

    single<Retrofit> {
        val json: Json = get()
        Retrofit.Builder()
            .baseUrl("https://api.flickr.com/services/rest/")
            .client(get())
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType())
            )
            .build()
    }

    single<FlickrApi> { get<Retrofit>().create(FlickrApi::class.java) }
}
