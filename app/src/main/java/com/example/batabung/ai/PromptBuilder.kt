package com.example.batabung.ai

import com.example.batabung.data.local.entity.JenisTransaksi
import com.example.batabung.data.repository.TabunganRepository
import com.example.batabung.util.FormatUtils
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data class untuk menyimpan konteks keuangan user.
 */
data class FinancialContext(
    val saldo: Long,
    val pemasukanBulanIni: Long,
    val pengeluaranBulanIni: Long,
    val namaTabungan: String,
    val targetTabungan: Long?,
    val progressPercentage: Float,
    val topKategoriPengeluaran: List<Pair<String, Long>>
)

/**
 * Data class untuk intent yang terdeteksi dari pertanyaan user.
 */
data class DetectedIntent(
    val category: IntentCategory,
    val confidence: Float,
    val keywords: List<String>
)

/**
 * Kategori intent yang dapat dideteksi.
 */
enum class IntentCategory {
    SALDO_QUERY,           // Pertanyaan tentang saldo
    PEMASUKAN_QUERY,       // Pertanyaan tentang pemasukan
    PENGELUARAN_QUERY,     // Pertanyaan tentang pengeluaran
    TARGET_QUERY,          // Pertanyaan tentang target/progress
    ANALISIS_QUERY,        // Pertanyaan analisis keuangan
    MOTIVASI_QUERY,        // Minta motivasi menabung
    KATEGORI_QUERY,        // Pertanyaan tentang kategori pengeluaran
    GENERAL_QUERY          // Pertanyaan umum lainnya
}

/**
 * Builder untuk membuat prompt yang dikirim ke AI.
 * Mengikuti prinsip: AI hanya menggunakan data yang diberikan, tidak mengarang.
 * Dilengkapi dengan deteksi karakter untuk mengenali intent user.
 */
@Singleton
class PromptBuilder @Inject constructor(
    private val repository: TabunganRepository
) {
    companion object {
        const val SYSTEM_PROMPT = """
Anda adalah BaTabung AI - asisten tabungan pribadi yang ramah dan membantu.

ATURAN KETAT:
1. HANYA gunakan data keuangan yang diberikan dalam konteks
2. DILARANG mengarang atau memperkirakan angka yang tidak ada
3. Jawaban harus singkat, jelas, dan dalam Bahasa Indonesia
4. Jangan memberikan saran investasi atau pinjaman
5. Fokus pada membantu user memahami kondisi tabungannya
6. Gunakan format Rupiah yang rapi (contoh: Rp1.500.000)
7. Bersikap positif dan memotivasi user untuk menabung

KEMAMPUAN:
- Menjawab pertanyaan tentang saldo
- Memberikan ringkasan pemasukan dan pengeluaran
- Menghitung persentase target tabungan
- Menganalisis pola pengeluaran (jika data tersedia)
- Memberikan motivasi menabung sederhana

BATASAN:
- Tidak bisa menambah/menghapus transaksi (user harus melakukannya sendiri)
- Tidak memberikan prediksi keuangan kompleks
- Tidak menyarankan produk finansial tertentu
"""
        
        // Keyword patterns untuk deteksi intent
        private val SALDO_KEYWORDS = listOf(
            "saldo", "uang", "berapa", "jumlah", "tabungan", "simpanan",
            "sisa", "punya", "dana", "duit", "balance"
        )
        
        private val PEMASUKAN_KEYWORDS = listOf(
            "pemasukan", "masuk", "income", "pendapatan", "gaji", "dapat",
            "terima", "dapat", "menabung", "nabung", "tambah"
        )
        
        private val PENGELUARAN_KEYWORDS = listOf(
            "pengeluaran", "keluar", "habis", "belanja", "beli", "bayar",
            "expense", "spending", "biaya", "boros", "buang"
        )
        
        private val TARGET_KEYWORDS = listOf(
            "target", "goal", "tujuan", "progress", "persen", "capai",
            "tercapai", "kurang", "lagi", "%", "berapa lagi"
        )
        
        private val ANALISIS_KEYWORDS = listOf(
            "analisis", "analisa", "review", "kondisi", "keuangan", 
            "sehat", "baik", "buruk", "status", "ringkasan", "summary"
        )
        
        private val MOTIVASI_KEYWORDS = listOf(
            "motivasi", "semangat", "tips", "saran", "nasihat", "bantu",
            "gimana", "bagaimana", "cara", "hemat"
        )
        
        private val KATEGORI_KEYWORDS = listOf(
            "kategori", "jenis", "terbesar", "paling", "banyak", 
            "apa saja", "kemana", "dimana", "untuk apa"
        )
    }
    
    /**
     * Deteksi intent dari pesan user menggunakan fuzzy matching.
     * @param userMessage Pesan dari user
     * @return DetectedIntent yang berisi kategori dan confidence score
     */
    fun detectIntent(userMessage: String): DetectedIntent {
        val normalizedMessage = normalizeMessage(userMessage)
        val words = normalizedMessage.split(" ", ",", "?", "!", ".").filter { it.isNotBlank() }
        
        val scores = mutableMapOf<IntentCategory, Float>()
        val matchedKeywords = mutableMapOf<IntentCategory, MutableList<String>>()
        
        // Calculate match scores for each category
        scores[IntentCategory.SALDO_QUERY] = calculateMatchScore(words, SALDO_KEYWORDS, matchedKeywords, IntentCategory.SALDO_QUERY)
        scores[IntentCategory.PEMASUKAN_QUERY] = calculateMatchScore(words, PEMASUKAN_KEYWORDS, matchedKeywords, IntentCategory.PEMASUKAN_QUERY)
        scores[IntentCategory.PENGELUARAN_QUERY] = calculateMatchScore(words, PENGELUARAN_KEYWORDS, matchedKeywords, IntentCategory.PENGELUARAN_QUERY)
        scores[IntentCategory.TARGET_QUERY] = calculateMatchScore(words, TARGET_KEYWORDS, matchedKeywords, IntentCategory.TARGET_QUERY)
        scores[IntentCategory.ANALISIS_QUERY] = calculateMatchScore(words, ANALISIS_KEYWORDS, matchedKeywords, IntentCategory.ANALISIS_QUERY)
        scores[IntentCategory.MOTIVASI_QUERY] = calculateMatchScore(words, MOTIVASI_KEYWORDS, matchedKeywords, IntentCategory.MOTIVASI_QUERY)
        scores[IntentCategory.KATEGORI_QUERY] = calculateMatchScore(words, KATEGORI_KEYWORDS, matchedKeywords, IntentCategory.KATEGORI_QUERY)
        
        // Find the category with highest score
        val bestMatch = scores.maxByOrNull { it.value }
        
        return if (bestMatch != null && bestMatch.value > 0.2f) {
            DetectedIntent(
                category = bestMatch.key,
                confidence = bestMatch.value.coerceAtMost(1.0f),
                keywords = matchedKeywords[bestMatch.key] ?: emptyList()
            )
        } else {
            DetectedIntent(
                category = IntentCategory.GENERAL_QUERY,
                confidence = 0.5f,
                keywords = emptyList()
            )
        }
    }
    
    /**
     * Normalize message untuk matching yang lebih baik.
     */
    private fun normalizeMessage(message: String): String {
        return message.lowercase()
            .replace("berapa", "berapa")
            .replace("brp", "berapa")
            .replace("gmn", "gimana")
            .replace("gmna", "gimana")
            .replace("bgmn", "bagaimana")
            .replace("duit", "uang")
            .replace("dwit", "uang")
            .replace("sldo", "saldo")
            .replace("sdlo", "saldo")
    }
    
    /**
     * Calculate match score using fuzzy matching.
     */
    private fun calculateMatchScore(
        words: List<String>,
        keywords: List<String>,
        matchedKeywords: MutableMap<IntentCategory, MutableList<String>>,
        category: IntentCategory
    ): Float {
        var matchCount = 0
        val matched = mutableListOf<String>()
        
        for (word in words) {
            for (keyword in keywords) {
                // Exact match
                if (word == keyword) {
                    matchCount += 2
                    matched.add(word)
                    break
                }
                // Fuzzy match - word contains keyword or vice versa
                else if (word.contains(keyword) || keyword.contains(word)) {
                    if (word.length >= 3 && levenshteinDistance(word, keyword) <= 2) {
                        matchCount += 1
                        matched.add(word)
                        break
                    }
                }
                // Levenshtein distance for typos
                else if (word.length >= 3 && levenshteinDistance(word, keyword) <= 1) {
                    matchCount += 1
                    matched.add(word)
                    break
                }
            }
        }
        
        if (matched.isNotEmpty()) {
            matchedKeywords[category] = matched
        }
        
        // Normalize score based on number of words
        return if (words.isNotEmpty()) {
            (matchCount.toFloat() / (words.size.toFloat() * 2)).coerceAtMost(1.0f)
        } else {
            0f
        }
    }
    
    /**
     * Calculate Levenshtein distance between two strings.
     * Used for fuzzy matching to handle typos.
     */
    private fun levenshteinDistance(s1: String, s2: String): Int {
        val m = s1.length
        val n = s2.length
        
        if (m == 0) return n
        if (n == 0) return m
        
        val dp = Array(m + 1) { IntArray(n + 1) }
        
        for (i in 0..m) dp[i][0] = i
        for (j in 0..n) dp[0][j] = j
        
        for (i in 1..m) {
            for (j in 1..n) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // deletion
                    dp[i][j - 1] + 1,      // insertion
                    dp[i - 1][j - 1] + cost // substitution
                )
            }
        }
        
        return dp[m][n]
    }
    
    /**
     * Membangun prompt lengkap dengan konteks keuangan dan intent yang terdeteksi.
     */
    fun buildPrompt(userMessage: String, context: FinancialContext): String {
        val contextSection = buildContextSection(context)
        val intent = detectIntent(userMessage)
        val intentHint = buildIntentHint(intent)
        
        return """
$contextSection

$intentHint

Pertanyaan User:
"$userMessage"

Berikan jawaban yang singkat, jelas, dan ramah.
""".trimIndent()
    }
    
    /**
     * Build hint untuk AI berdasarkan intent yang terdeteksi.
     */
    private fun buildIntentHint(intent: DetectedIntent): String {
        val categoryHint = when (intent.category) {
            IntentCategory.SALDO_QUERY -> 
                "User menanyakan tentang SALDO. Fokus pada informasi saldo saat ini."
            IntentCategory.PEMASUKAN_QUERY -> 
                "User menanyakan tentang PEMASUKAN. Fokus pada total pemasukan bulan ini."
            IntentCategory.PENGELUARAN_QUERY -> 
                "User menanyakan tentang PENGELUARAN. Fokus pada total pengeluaran bulan ini."
            IntentCategory.TARGET_QUERY -> 
                "User menanyakan tentang TARGET/PROGRESS tabungan. Fokus pada persentase pencapaian target."
            IntentCategory.ANALISIS_QUERY -> 
                "User meminta ANALISIS keuangan. Berikan ringkasan kondisi keuangan secara keseluruhan."
            IntentCategory.MOTIVASI_QUERY -> 
                "User membutuhkan MOTIVASI. Berikan semangat positif untuk terus menabung."
            IntentCategory.KATEGORI_QUERY -> 
                "User menanyakan tentang KATEGORI pengeluaran. Fokus pada kategori terbesar."
            IntentCategory.GENERAL_QUERY -> 
                "Pertanyaan umum. Jawab sesuai konteks yang relevan."
        }
        
        val confidenceNote = if (intent.confidence < 0.5f) {
            "\n(Catatan: Intent kurang jelas, minta klarifikasi jika perlu)"
        } else {
            ""
        }
        
        return """
=== INTENT TERDETEKSI ===
Kategori: ${intent.category.name}
Confidence: ${(intent.confidence * 100).toInt()}%
Keywords: ${intent.keywords.joinToString(", ").ifEmpty { "-" }}
Hint: $categoryHint$confidenceNote
=========================
""".trimIndent()
    }
    
    private fun buildContextSection(context: FinancialContext): String {
        val targetInfo = if (context.targetTabungan != null && context.targetTabungan > 0) {
            """
Target Tabungan: ${FormatUtils.formatRupiah(context.targetTabungan)}
Progress: ${(context.progressPercentage * 100).toInt()}%
            """.trimIndent()
        } else {
            "Target Tabungan: Tidak diatur"
        }
        
        val kategoriInfo = if (context.topKategoriPengeluaran.isNotEmpty()) {
            val kategoriStr = context.topKategoriPengeluaran.take(3).joinToString("\n") { (kategori, total) ->
                "  - $kategori: ${FormatUtils.formatRupiah(total)}"
            }
            "\nTop Kategori Pengeluaran:\n$kategoriStr"
        } else {
            ""
        }
        
        return """
=== DATA KEUANGAN USER ===
Nama Tabungan: ${context.namaTabungan}
Saldo Saat Ini: ${FormatUtils.formatRupiah(context.saldo)}

Bulan Ini (${FormatUtils.formatMonthYear(System.currentTimeMillis())}):
- Total Pemasukan: ${FormatUtils.formatRupiah(context.pemasukanBulanIni)}
- Total Pengeluaran: ${FormatUtils.formatRupiah(context.pengeluaranBulanIni)}
- Selisih: ${FormatUtils.formatRupiah(context.pemasukanBulanIni - context.pengeluaranBulanIni)}

$targetInfo$kategoriInfo
=========================
        """.trimIndent()
    }
    
    /**
     * Mendapatkan konteks keuangan dari repository.
     */
    suspend fun getFinancialContext(tabunganId: Long): FinancialContext? {
        val tabungan = repository.getTabunganByIdOnce(tabunganId) ?: return null
        
        val startOfMonth = FormatUtils.getStartOfMonth()
        val endOfMonth = FormatUtils.getEndOfMonth()
        
        val saldo = repository.getSaldoOnce(tabunganId)
        val pemasukanBulanIni = repository.getTotalPemasukanBulanIni(tabunganId, startOfMonth, endOfMonth)
        val pengeluaranBulanIni = repository.getTotalPengeluaranBulanIni(tabunganId, startOfMonth, endOfMonth)
        
        val topKategori = repository.getTotalByKategori(tabunganId, JenisTransaksi.KELUAR)
            .map { it.kategori to it.total }
        
        val progress = if (tabungan.target != null && tabungan.target > 0) {
            (saldo.toFloat() / tabungan.target.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
        
        return FinancialContext(
            saldo = saldo,
            pemasukanBulanIni = pemasukanBulanIni,
            pengeluaranBulanIni = pengeluaranBulanIni,
            namaTabungan = tabungan.nama,
            targetTabungan = tabungan.target,
            progressPercentage = progress,
            topKategoriPengeluaran = topKategori
        )
    }
}
