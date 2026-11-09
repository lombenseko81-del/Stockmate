package com.zut.stockmate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private data class DailySales(
    val date: LocalDate,
    val total: Double
)

@Composable
fun DailySummaryScreen(
    sales: List<SaleRecord>,
    onBack: () -> Unit
) {
    val today = LocalDate.now()

    // Calculate today's sales
    val todaysSales = sales.filter { sale ->
        Instant.ofEpochMilli(sale.dateTimeMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate() == today
    }

    val todaysTotal = todaysSales.sumOf { it.total }

    // Prepare sales totals for the last seven days
    val weeklySales = (6 downTo 0).map { daysAgo ->

        val date = today.minusDays(daysAgo.toLong())

        val dailyTotal = sales
            .filter { sale ->
                Instant.ofEpochMilli(sale.dateTimeMillis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate() == date
            }
            .sumOf { it.total }

        DailySales(
            date = date,
            total = dailyTotal
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Text(
            text = "Daily Summary",
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF172033)
        )

        Text(
            text = "Your sales performance at a glance.",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Today's sales summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "TODAY'S SALES",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "K ${"%.2f".format(todaysTotal)}",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2563EB)
                )

                Text(
                    text = "${todaysSales.size} transactions recorded today",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Weekly sales chart
        Text(
            text = "Weekly Sales",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Sales totals for the last seven days",
            fontSize = 13.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                val maximumSales = weeklySales.maxOfOrNull {
                    it.total
                } ?: 0.0

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(155.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.Bottom
                ) {

                    weeklySales.forEach { day ->

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {

                            Text(
                                text = if (day.total > 0) {
                                    "%.0f".format(day.total)
                                } else {
                                    ""
                                },
                                fontSize = 9.sp,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.65f)
                                    .height(
                                        if (maximumSales > 0) {
                                            (105.0 * day.total / maximumSales)
                                                .toFloat()
                                                .dp
                                        } else {
                                            0.dp
                                        }
                                    )
                                    .background(
                                        color = Color(0xFF2563EB),
                                        shape = RoundedCornerShape(
                                            topStart = 5.dp,
                                            topEnd = 5.dp
                                        )
                                    )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    weeklySales.forEach { day ->

                        Text(
                            text = day.date.dayOfWeek.name
                                .take(3)
                                .lowercase()
                                .replaceFirstChar { it.uppercase() },
                            modifier = Modifier.weight(1f),
                            fontSize = 10.sp,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Transaction history
        Text(
            text = "Today's Transactions",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "A record of sales made today",
            fontSize = 13.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (todaysSales.isEmpty()) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No sales have been recorded today.",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }

        } else {

            todaysSales
                .sortedByDescending { it.dateTimeMillis }
                .forEach { sale ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Text(
                                    text = sale.itemName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF172033)
                                )

                                Text(
                                    text = "K ${"%.2f".format(sale.total)}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2563EB)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "${sale.quantitySold} units × K ${"%.2f".format(sale.unitPrice)}",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = SimpleDateFormat(
                                    "hh:mm a",
                                    Locale.getDefault()
                                ).format(Date(sale.dateTimeMillis)),
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Back to Inventory")
        }
    }
}
