package com.zut.stockmate

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.rememberCoroutineScope
import androidx.room.withTransaction
import kotlinx.coroutines.launch
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import androidx.compose.material3.Button
import androidx.compose.material3.Text

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts
                .RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                LowStockNotifications.createChannel(this)
            }
        }
    private fun requestNotificationPermission() {

        if (android.os.Build.VERSION.SDK_INT >= 33) {

            if (
                checkSelfPermission(
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(
                    android.Manifest.permission.POST_NOTIFICATIONS
                )
            } else {
                LowStockNotifications.createChannel(this)
            }

        } else {
            LowStockNotifications.createChannel(this)
        }
    }
    private fun scheduleLowStockCheck() {

        val workRequest =
            PeriodicWorkRequestBuilder<LowStockWorker>(
                24,
                TimeUnit.HOURS
            ).build()

        WorkManager.getInstance(applicationContext)
            .enqueueUniquePeriodicWork(
                "DailyLowStockCheck",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Open the local Room database
        val database = StockMateDatabase.getDatabase(this)

        val inventoryDao = database.inventoryDao()
        val saleDao = database.saleDao()

        scheduleLowStockCheck()
        requestNotificationPermission()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF5F7FA)
                ) {

                    // Observe inventory and sales from Room
                    val inventoryEntities by inventoryDao
                        .getAllItems()
                        .collectAsStateWithLifecycle(initialValue = emptyList())

                    val salesEntities by saleDao
                        .getAllSales()
                        .collectAsStateWithLifecycle(initialValue = emptyList())

                    // Convert database records into the existing screen models
                    val stockItems = inventoryEntities.map {
                        it.toStockItem()
                    }

                    val sales = salesEntities.map {
                        it.toSaleRecord()
                    }

                    val scope = rememberCoroutineScope()

                    // Insert original sample products only when inventory is empty
                    LaunchedEffect(Unit) {
                        if (inventoryDao.getItemCount() == 0) {

                            val sampleItems = listOf(
                                InventoryEntity(
                                    id = 1,
                                    name = "Cooking Oil",
                                    quantity = 20,
                                    price = 50.00,
                                    lowStockLimit = 5
                                ),
                                InventoryEntity(
                                    id = 2,
                                    name = "Sugar",
                                    quantity = 3,
                                    price = 35.00,
                                    lowStockLimit = 5
                                ),
                                InventoryEntity(
                                    id = 3,
                                    name = "Rice",
                                    quantity = 15,
                                    price = 45.00,
                                    lowStockLimit = 5
                                ),
                                InventoryEntity(
                                    id = 4,
                                    name = "Soap",
                                    quantity = 2,
                                    price = 15.00,
                                    lowStockLimit = 4
                                ),
                                InventoryEntity(
                                    id = 5,
                                    name = "Milk",
                                    quantity = 12,
                                    price = 25.00,
                                    lowStockLimit = 3
                                )
                            )

                            database.withTransaction {
                                sampleItems.forEach { item ->
                                    inventoryDao.insertItem(item)
                                }
                            }
                        }
                    }

                    // Screen navigation
                    var showingEditor by remember {
                        mutableStateOf(false)
                    }

                    var showingSale by remember {
                        mutableStateOf(false)
                    }

                    var showingSummary by remember {
                        mutableStateOf(false)
                    }

                    // Item currently being edited
                    var selectedItem by remember {
                        mutableStateOf<StockItem?>(null)
                    }

                    if (showingEditor) {

                        AddEditItemScreen(
                            item = selectedItem,

                            onCancel = {
                                showingEditor = false
                                selectedItem = null
                            },

                            onSave = { name, quantity, price, lowStockLimit ->

                                val existingItem = selectedItem

                                scope.launch {

                                    if (existingItem == null) {

                                        // Insert a new inventory item
                                        inventoryDao.insertItem(
                                            InventoryEntity(
                                                name = name,
                                                quantity = quantity,
                                                price = price,
                                                lowStockLimit = lowStockLimit
                                            )
                                        )

                                    } else {

                                        // Update an existing inventory item
                                        inventoryDao.updateItem(
                                            InventoryEntity(
                                                id = existingItem.id,
                                                name = name,
                                                quantity = quantity,
                                                price = price,
                                                lowStockLimit = lowStockLimit
                                            )
                                        )
                                    }

                                    showingEditor = false
                                    selectedItem = null
                                }
                            }
                        )

                    } else if (showingSale) {

                        RecordSaleScreen(
                            inventoryItems = stockItems,

                            onCancel = {
                                showingSale = false
                            },

                            onConfirmSale = { item, quantitySold ->

                                scope.launch {

                                    // Update stock and record the sale together
                                    val saleRecorded = database.withTransaction {

                                        val currentItem =
                                            inventoryDao.getItemById(item.id)

                                        if (
                                            currentItem == null ||
                                            quantitySold <= 0 ||
                                            quantitySold > currentItem.quantity
                                        ) {
                                            false
                                        } else {

                                            val saleTotal =
                                                currentItem.price * quantitySold

                                            // Reduce inventory quantity
                                            inventoryDao.updateItem(
                                                currentItem.copy(
                                                    quantity = currentItem.quantity - quantitySold
                                                )
                                            )

                                            // Save the transaction
                                            saleDao.insertSale(
                                                SaleEntity(
                                                    itemId = currentItem.id,
                                                    itemName = currentItem.name,
                                                    quantitySold = quantitySold,
                                                    unitPrice = currentItem.price,
                                                    total = saleTotal,
                                                    dateTimeMillis = System.currentTimeMillis()
                                                )
                                            )

                                            true
                                        }
                                    }

                                    if (saleRecorded) {

                                        Toast.makeText(
                                            this@MainActivity,
                                            "Sale recorded successfully!",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        showingSale = false

                                    } else {

                                        Toast.makeText(
                                            this@MainActivity,
                                            "Sale could not be recorded. Check available stock.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        )

                    } else if (showingSummary) {

                        DailySummaryScreen(
                            sales = sales,

                            onBack = {
                                showingSummary = false
                            }
                        )

                    } else {

                        StockListScreen(
                            inventoryItems = stockItems,

                            onAddItem = {
                                selectedItem = null
                                showingEditor = true
                            },

                            onEditItem = { item ->
                                selectedItem = item
                                showingEditor = true
                            },

                            onRecordSale = {
                                showingSale = true
                            },

                            onViewSummary = {
                                showingSummary = true
                            }
                        )
                    }
                }
            }
        }
    }
}

// Convert a Room inventory record into the existing UI model
private fun InventoryEntity.toStockItem(): StockItem {
    return StockItem(
        id = id,
        name = name,
        quantity = quantity,
        price = price,
        lowStockLimit = lowStockLimit
    )
}

// Convert a Room sales record into the existing UI model
private fun SaleEntity.toSaleRecord(): SaleRecord {
    return SaleRecord(
        id = id,
        itemId = itemId,
        itemName = itemName,
        quantitySold = quantitySold,
        unitPrice = unitPrice,
        total = total,
        dateTimeMillis = dateTimeMillis
    )
}
