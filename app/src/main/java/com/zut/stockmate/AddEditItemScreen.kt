package com.zut.stockmate

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AddEditItemScreen(
    item: StockItem?,
    onCancel: () -> Unit,
    onSave: (String, Int, Double, Int) -> Unit
) {
    var name by remember(item) {
        mutableStateOf(item?.name ?: "")
    }

    var quantity by remember(item) {
        mutableStateOf(item?.quantity?.toString() ?: "")
    }

    var price by remember(item) {
        mutableStateOf(item?.price?.toString() ?: "")
    }

    var lowStockLimit by remember(item) {
        mutableStateOf(item?.lowStockLimit?.toString() ?: "")
    }

    var showErrors by remember(item) {
        mutableStateOf(false)
    }

    val quantityValue = quantity.toIntOrNull()
    val priceValue = price.toDoubleOrNull()
    val lowStockValue = lowStockLimit.toIntOrNull()

    val nameError = showErrors && name.isBlank()

    val quantityError = showErrors &&
            (quantity.isBlank() || quantityValue == null || quantityValue < 0)

    val priceError = showErrors &&
            (price.isBlank() || priceValue == null ||
                    !priceValue.isFinite() || priceValue < 0)

    val lowStockError = showErrors &&
            (lowStockLimit.isBlank() || lowStockValue == null || lowStockValue < 0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text(
            text = if (item == null) "Add New Item" else "Edit Item",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF172033)
        )

        Text(
            text = "Enter the inventory item details below.",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Item Name") },
            placeholder = { Text("e.g. Cooking Oil") },
            singleLine = true,
            isError = nameError,
            supportingText = {
                if (nameError) Text("Item name cannot be empty.")
            },
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = quantity,
            onValueChange = { quantity = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Quantity") },
            placeholder = { Text("e.g. 20") },
            singleLine = true,
            isError = quantityError,
            supportingText = {
                if (quantityError) {
                    Text("Enter a valid whole number (0 or greater).")
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = price,
            onValueChange = { price = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Selling Price (K)") },
            placeholder = { Text("e.g. 50.00") },
            singleLine = true,
            isError = priceError,
            supportingText = {
                if (priceError) {
                    Text("Enter a valid price (0 or greater). Use a decimal point.")
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = lowStockLimit,
            onValueChange = { lowStockLimit = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Low Stock Limit") },
            placeholder = { Text("e.g. 5") },
            singleLine = true,
            isError = lowStockError,
            supportingText = {
                if (lowStockError) {
                    Text("Enter a valid whole number (0 or greater).")
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = {
                showErrors = true

                if (
                    name.isNotBlank() &&
                    quantityValue != null && quantityValue >= 0 &&
                    priceValue != null && priceValue.isFinite() && priceValue >= 0 &&
                    lowStockValue != null && lowStockValue >= 0
                ) {
                    onSave(
                        name.trim(),
                        quantityValue,
                        priceValue,
                        lowStockValue
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2563EB)
            )
        ) {
            Text(
                text = if (item == null) "Save Item" else "Update Item",
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
