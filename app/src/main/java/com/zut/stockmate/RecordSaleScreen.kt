
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

@Composable
fun RecordSaleScreen(
    inventoryItems: List<StockItem>,
    onCancel: () -> Unit,
    onConfirmSale: (StockItem, Int) -> Unit
) {
    var selectedItemId by remember {
        mutableStateOf<Int?>(null)
    }

    var quantitySold by remember {
        mutableStateOf("")
    }

    var showErrors by remember {
        mutableStateOf(false)
    }

    var showItemSelector by remember {
        mutableStateOf(false)
    }

    val selectedItem = inventoryItems.find {
        it.id == selectedItemId
    }

    val quantityValue = quantitySold.toIntOrNull()

    val quantityError = showErrors &&
            (quantitySold.isBlank() ||
                    quantityValue == null ||
                    quantityValue <= 0)

    val exceedsStock = showErrors &&
            selectedItem != null &&
            quantityValue != null &&
            quantityValue > selectedItem.quantity

    val saleTotal = if (
        selectedItem != null &&
        quantityValue != null &&
        quantityValue > 0
    ) {
        selectedItem.price * quantityValue
    } else {
        0.0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(20.dp)
    ) {

        Text(
            text = "Record Sale",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF172033)
        )

        Text(
            text = "Enter the details of the transaction.",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Select Inventory Item",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {

            OutlinedButton(
                onClick = {
                    showItemSelector = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = selectedItem?.name ?: "Choose an item",
                    modifier = Modifier.weight(1f)
                )

                Text("▼")
            }

            DropdownMenu(
                expanded = showItemSelector,
                onDismissRequest = {
                    showItemSelector = false
                },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .heightIn(max = 300.dp)
            ) {
                inventoryItems.forEach { item ->

                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = item.name,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Text(
                                    text = "Available: ${item.quantity} units",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        },
                        onClick = {
                            selectedItemId = item.id
                            quantitySold = ""
                            showErrors = false
                            showItemSelector = false
                        },
                        enabled = item.quantity > 0
                    )
                }
            }
        }

        if (inventoryItems.isEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "There are no inventory items available.",
                color = Color(0xFFDC2626),
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (selectedItem != null) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Selected Item",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = selectedItem.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Available stock: ${selectedItem.quantity} units"
                    )

                    Text(
                        text = "Unit price: K ${"%.2f".format(selectedItem.price)}",
                        color = Color(0xFF2563EB),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = quantitySold,
            onValueChange = {
                quantitySold = it
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Quantity Sold")
            },
            placeholder = {
                Text("Enter quantity")
            },
            singleLine = true,
            isError = quantityError || exceedsStock,
            supportingText = {
                when {
                    quantityError -> {
                        Text("Enter a valid whole number greater than zero.")
                    }

                    exceedsStock -> {
                        Text("You cannot sell more than the available stock.")
                    }
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Text(
                    text = "SALE TOTAL",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = "K ${"%.2f".format(saleTotal)}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2563EB)
                )

                Text(
                    text = "Calculated automatically",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                showErrors = true

                if (
                    selectedItem != null &&
                    quantityValue != null &&
                    quantityValue > 0 &&
                    quantityValue <= selectedItem.quantity
                ) {
                    onConfirmSale(selectedItem, quantityValue)
                }
            },
            enabled = selectedItem != null &&
                    selectedItem.quantity > 0,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2563EB)
            )
        ) {
            Text(
                text = "Confirm Sale",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Cancel")
        }
    }
}
