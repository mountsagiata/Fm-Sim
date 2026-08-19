package com.mountsa.fmsimulation.ui.screens.dashboard.shop

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mountsa.fmsimulation.ui.components.AppColumn
import com.mountsa.fmsimulation.ui.components.ShopItemCard
import com.mountsa.fmsimulation.ui.screens.dashboard.FM_GREEN
import com.mountsa.fmsimulation.ui.viewmodel.DashboardViewModel

data class ShopItem(
    val title: String,
    val description: String,
    val price: String,
    val icon: String
)

@Composable
fun ShopHub(viewModel: DashboardViewModel) {
    val shopItems = listOf(
        ShopItem("Wealthy Benefactor", "Receive €50M transfer budget injection.", "$4.99", "💰"),
        ShopItem("No Sack", "The board will never fire you from this club.", "$2.99", "🛡️"),
        ShopItem("All Players Scouted", "Instantly see stats for every player.", "$3.99", "🔍"),
        ShopItem("Magic Sponge", "Instantly heal all injured players.", "$1.99", "🧽"),
        ShopItem("Youth Intake", "Force a high-quality youth intake.", "$2.49", "👶"),
        ShopItem("Transfer Guru", "All transfer negotiations succeed.", "$5.99", "🤝")
    )

    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AppColumn(modifier = Modifier.weight(2f), title = "EXCLUSIVE SHOP") {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "UNLOCK PREMIUM FEATURES",
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = FM_GREEN,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(130.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(shopItems) { item ->
                        ShopItemCard(
                            title = item.title,
                            description = item.description,
                            price = item.price,
                            icon = item.icon,
                            onPurchase = { /* Handle purchase */ }
                        )
                    }
                }
            }
        }
        
        AppColumn(modifier = Modifier.weight(1f), title = "CURRENCY & SUBSCRIPTION") {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Manager Points: 1,250", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                Text("Membership: FREE", fontSize = 12.sp, color = Color.Gray)
                
                androidx.compose.material3.Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = FM_GREEN)
                ) {
                    Text("GO PREMIUM", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
