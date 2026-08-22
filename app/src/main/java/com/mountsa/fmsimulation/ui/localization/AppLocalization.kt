package com.mountsa.fmsimulation.ui.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppLanguage = staticCompositionLocalOf { "en" }

@Composable
fun ProvideAppLanguage(languageTag: String, content: @Composable () -> Unit) {
    val resolved = if (languageTag == "system") java.util.Locale.getDefault().language else languageTag
    CompositionLocalProvider(LocalAppLanguage provides resolved, content = content)
}

@Composable
fun localized(source: String): String = translations[LocalAppLanguage.current]?.get(source) ?: source

private val translations: Map<String, Map<String, String>> = mapOf(
    "id" to mapOf(
        "Home" to "Beranda", "Squad" to "Skuad", "League" to "Liga", "Training" to "Latihan",
        "Transfers" to "Transfer", "Scouting" to "Pemanduan", "Club" to "Klub", "Shop" to "Toko",
        "Inbox" to "Kotak Masuk", "Calendar" to "Kalender", "Settings" to "Pengaturan",
        "PITCH" to "LAPANGAN", "SQUAD" to "SKUAD", "FORMATION" to "FORMASI", "PLAYER DETAILS" to "DETAIL PEMAIN",
        "TEAM OVERVIEW" to "RINGKASAN TIM", "STANDING" to "KLASEMEN", "FINANCE" to "KEUANGAN",
        "LATEST NEWS" to "BERITA TERBARU", "OBJECTIVES" to "TARGET", "UPCOMING FIXTURES" to "JADWAL MENDATANG",
        "LEAGUE STATS" to "STATISTIK LIGA", "CLUB INFO" to "INFO KLUB", "FACILITIES" to "FASILITAS",
        "Game" to "Permainan", "Display" to "Tampilan", "Audio" to "Audio", "Language" to "Bahasa",
        "CONTINUE" to "LANJUTKAN", "VIEW RESULT" to "LIHAT HASIL", "RETURN TO HUB" to "KEMBALI KE HUB",
        "STARTING XI" to "SEBELAS UTAMA", "SUBSTITUTES" to "PEMAIN PENGGANTI", "START SECOND HALF" to "MULAI BABAK KEDUA",
        "HALF-TIME" to "PARUH WAKTU", "FULL TIME" to "PERTANDINGAN SELESAI", "POST-MATCH ANALYSIS" to "ANALISIS PASCAPERTANDINGAN",
        "MATCH STATS" to "STATISTIK PERTANDINGAN", "Shots" to "Tembakan", "Target" to "Tepat Sasaran",
        "On Target" to "Tepat Sasaran", "Possession" to "Penguasaan", "Corners" to "Sepak Pojok", "Fouls" to "Pelanggaran",
        "EVENT" to "PERISTIWA", "STATISTIK" to "STATISTIK", "KOMENTAR" to "KOMENTAR",
        "VICTORY" to "MENANG", "DEFEAT" to "KALAH", "DRAW" to "SERI", "WINNER" to "PEMENANG",
        "CALENDAR" to "KALENDER", "NO EVENTS" to "TIDAK ADA KEGIATAN", "League Match" to "Pertandingan Liga",
        "Friendly" to "Persahabatan", "Domestic Cup" to "Piala Domestik", "Continental" to "Kontinental",
        "Select Player" to "Pilih Pemain", "CANCEL" to "BATAL", "Save" to "Simpan", "Accept" to "Terima",
        "Reject" to "Tolak", "Cancel" to "Batal", "FIND PLAYERS" to "CARI PEMAIN", "INCOMING OFFERS" to "PENAWARAN MASUK",
        "TRANSFER MARKET" to "BURSA TRANSFER", "SCOUTING" to "PEMANDUAN", "No active offers" to "Tidak ada penawaran aktif",
        "UNLOCK PREMIUM FEATURES" to "BUKA FITUR PREMIUM", "Start Game" to "Mulai Permainan",
        "No scouts hired" to "Belum ada pemandu", "No active assignments" to "Tidak ada tugas aktif",
        "Search the database for specific targets" to "Cari target tertentu dalam database",
        "INBOX" to "KOTAK MASUK", "MESSAGE DETAILS" to "DETAIL PESAN", "Select a message to read" to "Pilih pesan untuk dibaca",
        "No news available" to "Belum ada berita", "No active objectives" to "Tidak ada target aktif",
        "GAME" to "PERMAINAN", "DISPLAY" to "TAMPILAN", "AUDIO" to "AUDIO", "LANGUAGE" to "BAHASA",
        "Auto-save after match" to "Simpan otomatis setelah pertandingan", "Enable match commentary" to "Aktifkan komentar pertandingan",
        "Confirm before advancing on matchday" to "Konfirmasi sebelum melanjutkan pada hari pertandingan",
        "Pause automatically at half-time" to "Jeda otomatis saat paruh waktu", "Show player faces" to "Tampilkan wajah pemain",
        "Show attributes as progress bars" to "Tampilkan atribut sebagai bilah progres", "Compact squad rows" to "Baris skuad ringkas",
        "Reduce animations" to "Kurangi animasi", "Music" to "Musik", "Sound effects" to "Efek suara", "Crowd" to "Suara penonton",
        "RESET CAREER DATA" to "ATUR ULANG DATA KARIER", "Reset career data?" to "Atur ulang data karier?",
        "This permanently deletes the active career, fixtures, match history and standings." to "Tindakan ini menghapus permanen karier aktif, jadwal, riwayat pertandingan, dan klasemen.",
        "RESET" to "ATUR ULANG", "BACK" to "KEMBALI", "CONTINUE CAREER" to "LANJUTKAN KARIER",
        "NEW CAREER" to "KARIER BARU", "START NEW CAREER" to "MULAI KARIER BARU", "NEW MANAGER" to "MANAJER BARU",
        "Manager Name" to "Nama Manajer", "Choose your path to glory" to "Pilih jalan menuju kejayaan",
        "Finalize tactics & prepare for battle" to "Selesaikan taktik dan bersiap untuk pertandingan",
        "Review events, statistics and make tactical changes." to "Tinjau peristiwa, statistik, dan lakukan perubahan taktik.",
        "Simulating football world..." to "Menyimulasikan dunia sepak bola...",
        "The team is heading back to the dressing room." to "Tim kembali menuju ruang ganti."
    ),
    "pt" to mapOf(
        "Home" to "Início", "Squad" to "Elenco", "League" to "Liga", "Training" to "Treino", "Transfers" to "Transferências",
        "Scouting" to "Observação", "Club" to "Clube", "Settings" to "Definições", "CONTINUE" to "CONTINUAR",
        "VIEW RESULT" to "VER RESULTADO", "HALF-TIME" to "INTERVALO", "FULL TIME" to "FIM DE JOGO",
        "Language" to "Idioma", "Display" to "Ecrã", "Music" to "Música", "Crowd" to "Torcida"
    ),
    "ja" to mapOf(
        "Home" to "ホーム", "Squad" to "スカッド", "League" to "リーグ", "Training" to "トレーニング", "Transfers" to "移籍",
        "Scouting" to "スカウト", "Club" to "クラブ", "Settings" to "設定", "CONTINUE" to "続ける",
        "VIEW RESULT" to "結果を見る", "HALF-TIME" to "ハーフタイム", "FULL TIME" to "試合終了",
        "Language" to "言語", "Display" to "表示", "Music" to "音楽", "Crowd" to "観客"
    )
)
