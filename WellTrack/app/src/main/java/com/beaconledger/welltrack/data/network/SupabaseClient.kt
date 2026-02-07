package com.beaconledger.welltrack.data.network

import com.beaconledger.welltrack.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.CertificatePinner
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseClient @Inject constructor() {

    val client: SupabaseClient by lazy {
        val certificatePinner = CertificatePinner.Builder()
            // TODO: Replace with the actual SHA-256 fingerprint of the Supabase URL
            .add(BuildConfig.SUPABASE_URL.removePrefix("https://"), "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=")
            .build()

        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Auth)
            install(Postgrest)
            install(Storage)
            httpEngine = OkHttp.create {
                preconfigured = okhttp3.OkHttpClient.Builder()
                    .certificatePinner(certificatePinner)
                    .build()
            }
        }
    }
}

fun provideSupabaseClient(): SupabaseClient {
    val certificatePinner = CertificatePinner.Builder()
        // TODO: Replace with the actual SHA-256 fingerprint of the Supabase URL
        .add(BuildConfig.SUPABASE_URL.removePrefix("https://"), "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=")
        .build()

    return createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
        httpEngine = OkHttp.create {
            preconfigured = okhttp3.OkHttpClient.Builder()
                .certificatePinner(certificatePinner)
                .build()
        }
    }
}
