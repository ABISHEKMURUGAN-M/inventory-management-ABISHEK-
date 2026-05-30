package com.example.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.ProductionRecord
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportExporter {

    fun generateCSV(records: List<ProductionRecord>): String {
        val sb = StringBuilder()
        sb.append("Record ID,Date,Shift,Area,Matrix,Model,Before CAP,After CAP,Spare,Rejected,Total Solid,Employee,Timestamp,Synced\n")
        
        for (rec in records) {
            val total = rec.afterCap + rec.spareCount
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            val timeStr = sdf.format(Date(rec.timestamp))
            sb.append("${rec.id},${rec.date},${rec.shift},\"${rec.area}\",${rec.matrix},\"${rec.modelName}\",${rec.beforeCap},${rec.afterCap},${rec.spareCount},${rec.rejectedCount},$total,${rec.employeeId},\"$timeStr\",${rec.isSynced}\n")
        }
        return sb.toString()
    }

    fun generateHTMLReport(title: String, records: List<ProductionRecord>): String {
        val totalBefore = records.sumOf { it.beforeCap }
        val totalAfter = records.sumOf { it.afterCap }
        val totalSpare = records.sumOf { it.spareCount }
        val totalRejected = records.sumOf { it.rejectedCount }
        val grandTotal = totalAfter + totalSpare

        val rows = StringBuilder()
        for ((index, rec) in records.withIndex()) {
            val rowClass = if (index % 2 == 0) "even" else "odd"
            val recordTotal = rec.afterCap + rec.spareCount
            val timeStr = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(rec.timestamp))
            rows.append("""
                <tr class="$rowClass">
                    <td>${rec.date}</td>
                    <td>${rec.shift}</td>
                    <td>${rec.area}</td>
                    <td><strong>${rec.matrix}</strong></td>
                    <td>${rec.modelName}</td>
                    <td class="num">${rec.beforeCap}</td>
                    <td class="num">${rec.afterCap}</td>
                    <td class="num">${rec.spareCount}</td>
                    <td class="num reject">${rec.rejectedCount}</td>
                    <td class="num total"><strong>$recordTotal</strong></td>
                    <td>${rec.employeeId}</td>
                </tr>
            """.trimIndent())
        }

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <title>$title</title>
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; color: #2C3E50; margin: 24px; background-color: #FAFAFA; }
                    h1 { color: #003049; border-bottom: 2px solid #003049; padding-bottom: 8px; font-size: 24px; margin-bottom: 4px; }
                    .meta { color: #555; margin-bottom: 24px; font-size: 13px; }
                    table { width: 100%; border-collapse: collapse; margin-bottom: 30px; background-color: #FFFFFF; }
                    th { background-color: #003049; color: white; border: 1px solid #BDC3C7; padding: 10px; font-size: 13px; font-weight: 600; text-align: left; }
                    td { border: 1px solid #BDC3C7; padding: 10px; font-size: 13px; }
                    .even { background-color: #F8F9F9; }
                    .odd { background-color: #FFFFFF; }
                    .num { text-align: right; font-family: monospace; font-size: 14px; }
                    .reject { color: #C0392B; font-weight: bold; }
                    .total { color: #16A085; }
                    .summary { display: flex; justify-content: space-between; background-color: #EAEDED; border: 1.5px solid #BDC3C7; border-radius: 6px; padding: 15px; margin-bottom: 30px; }
                    .summary-item { text-align: center; flex: 1; }
                    .summary-item .title { font-size: 11px; text-transform: uppercase; color: #7F8C8D; font-weight: bold; }
                    .summary-item .value { font-size: 20px; font-weight: bold; color: #2C3E50; margin-top: 5px; }
                    .footer { text-align: center; font-size: 11px; color: #95A5A6; margin-top: 40px; border-top: 1px dashed #BDC3C7; padding-top: 10px; }
                    @media print {
                        body { background-color: white; margin: 0; }
                        table { page-break-inside: auto; }
                        tr { page-break-inside: avoid; page-break-after: auto; }
                    }
                </style>
            </head>
            <body>
                <h1>$title</h1>
                <div class="meta">Generated standard production summary on: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}</div>
                
                <div class="summary">
                    <div class="summary-item">
                        <div class="title">Total Input (Before)</div>
                        <div class="value">$totalBefore</div>
                    </div>
                    <div class="summary-item">
                        <div class="title">Total Yield (After)</div>
                        <div class="value">$totalAfter</div>
                    </div>
                    <div class="summary-item">
                        <div class="title">Total Spares</div>
                        <div class="value">$totalSpare</div>
                    </div>
                    <div class="summary-item">
                        <div class="title">Total Discards</div>
                        <div class="value" style="color: #C0392B;">$totalRejected</div>
                    </div>
                    <div class="summary-item">
                        <div class="title">Net Output (Qualified)</div>
                        <div class="value" style="color: #16A085;">$grandTotal</div>
                    </div>
                </div>

                <table>
                    <thead>
                        <tr>
                            <th>Date</th>
                            <th>Shift</th>
                            <th>Area</th>
                            <th>Matrix</th>
                            <th>Model</th>
                            <th>Before CAP</th>
                            <th>After CAP</th>
                            <th>Spare</th>
                            <th style="color: #FFC5C5">Rejected</th>
                            <th>Net Total</th>
                            <th>Employee</th>
                        </tr>
                    </thead>
                    <tbody>
                        $rows
                    </tbody>
                </table>

                <div class="footer">
                    Hanon Production Monitoring System &bull; Factory Execution Log Confidential.
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    fun shareReportFile(context: Context, filename: String, content: String, isHtml: Boolean = false) {
        val root = context.cacheDir
        val file = File(root, filename)
        try {
            file.writeText(content)
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = if (isHtml) "text/html" else "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Hanon Systems Production Log - $filename")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share factory report logs..."))
        } catch (e: Exception) {
            Toast.makeText(context, "Error sharing report: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
