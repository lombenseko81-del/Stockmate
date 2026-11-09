package com.zut.stockmate

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class StockItem(
    val id: Int,
    val name: String,
    val quantity: Int,
    val price: Double,
    val lowStockLimit: Int
)

@Composable
fun StockListScreen(
    inventoryItems: List<StockItem>,
    onAddItem: () -> Unit,
    onEditItem: (StockItem) -> Unit,
    onRecordSale: () -> Unit,
    onViewSummary: () -> Unit
) {
    var searchQuery by remember {
        mutableStateOf("")
    }

    val filteredItems = inventoryItems.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(16.dp)
    ) {

        Text(
            text = "StockMate",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF172033)
        )

        Text(
            text = "Inventory Management",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(20.dp))

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
                    text = "TOTAL PRODUCTS",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Text(
                    text = "${inventoryItems.size}",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF172033)
                )

                Text(
                    text = "Registered inventory items",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Inventory",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "${filteredItems.size} items",
                color = Color(0xFF2563EB),
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("Search inventory...")
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredItems.isEmpty()) {

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (inventoryItems.isEmpty()) {
                        "No inventory items yet.\nAdd your first item to get started."
                    } else {
                        "No items match your search."
                    },
                    color = Color.Gray,
                    fontSize = 15.sp
                )
            }

        } else {

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredItems) { item ->
                    StockItemCard(
                        item = item,
                        onClick = {
                            onEditItem(item)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onRecordSale,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF15803D)
            )
        ) {
            Text(
                text = "Record Sale",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = onViewSummary,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF475569)
            )
        ) {
            Text(
                text = "Daily Summary",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = onAddItem,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2563EB)
            )
        ) {
            Text(
                text = "+ Add New Item",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun StockItemCard(
    item: StockItem,
    onClick: () -> Unit
) {
    val isLowStock = item.quantity <= item.lowStockLimit

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
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
                    text = item.name,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF172033)
                )

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isLowStock) {
                        Color(0xFFFFF0D6)
                    } else {
                        Color(0xFFE0F5E9)
                    }
                ) {
                    Text(
                        text = if (isLowStock) "Low Stock" else "In Stock",
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isLowStock) {
                            Color(0xFFB45309)
                        } else {
                            Color(0xFF15803D)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Quantity: ${item.quantity} units",
                fontSize = 13.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = "K ${"%.2f".format(item.price)}",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2563EB)
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = "Tap to edit",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}
