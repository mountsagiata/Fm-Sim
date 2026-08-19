// File: utils/AssetValidator.kt
package com.mountsa.fmsimulation.utils

import android.content.Context
import android.util.Log
import java.io.IOException

object AssetValidator {

    fun validateAssets(context: Context): List<String> {
        val missingAssets = mutableListOf<String>()

        // Disinkronkan penuh dengan 27 negara pada DatabaseSeeder
        val requiredFlags = listOf(
            "ENGLAND.webp", "SCOTLAND.webp", "WALES.webp", "NL.webp",
            "US.webp", "DE.webp", "FR.webp", "IT.webp", "ES.webp",
            "AR.webp", "BR.webp", "PT.webp", "BE.webp", "DK.webp",
            "NO.webp", "SE.webp", "PL.webp", "CH.webp", "RO.webp",
            "AT.webp", "KR.webp", "AU.webp", "TR.webp", "CN.webp",
            "IE.webp", "SA.webp", "IN.webp"
        )

        val requiredJsonFiles = listOf(
            "database/leagues.json",
            "database/teams.json",
            "database/database_players.json",
            "database/nationality.json",
            "database/stadiums.json",
            "database/team_abbr.json"
        )

        // Validasi aset gambar bendera negara
        requiredFlags.forEach { flag ->
            try {
                context.assets.open("database/logo/flag/$flag").close()
            } catch (e: IOException) {
                missingAssets.add("database/logo/flag/$flag")
                Log.w("AssetValidator", "Missing flag asset: $flag")
            }
        }

        // Validasi berkas konfigurasi JSON
        requiredJsonFiles.forEach { jsonFile ->
            try {
                context.assets.open(jsonFile).close()
            } catch (e: IOException) {
                missingAssets.add(jsonFile)
                Log.e("AssetValidator", "Critical Missing JSON: $jsonFile")
            }
        }

        return missingAssets
    }
}