package com.example.batabung.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility functions untuk format currency dan tanggal.
 */
object FormatUtils {
    
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }
    
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
    private val dateTimeFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
    
    /**
     * Format nominal ke Rupiah (contoh: Rp1.500.000)
     */
    fun formatRupiah(amount: Long): String {
        return currencyFormat.format(amount)
    }
    
    /**
     * Format timestamp ke tanggal (contoh: 15 Des 2024)
     */
    fun formatDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }
    
    /**
     * Format timestamp ke tanggal dan waktu (contoh: 15 Des 2024, 14:30)
     */
    fun formatDateTime(timestamp: Long): String {
        return dateTimeFormat.format(Date(timestamp))
    }
    
    /**
     * Format ke bulan dan tahun (contoh: Desember 2024)
     */
    fun formatMonthYear(timestamp: Long): String {
        return monthYearFormat.format(Date(timestamp))
    }
    
    /**
     * Dapatkan timestamp awal bulan ini.
     */
    fun getStartOfMonth(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    /**
     * Dapatkan timestamp akhir bulan ini.
     */
    fun getEndOfMonth(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }
}
