package com.example.dbasket

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val AppPrimaryColor = Color(0xFF1E3A8A)
val AppBackgroundColor = Color(0xFFF8FAFC)
val TextPrimary = Color(0xFF0F172A)
val TextSecondary = Color(0xFF64748B)

data class Product(
    val id: Int,
    val name: String,
    val weight: String,
    val price: String,
    val category: String,
    val imageResId: Int
)

val categories = listOf("All", "Fruits & Vegetables", "Dairy & Breakfast", "Munchies", "Beverages", "Personal Care")

val myProducts = listOf(
    Product(1, "Fresh Tomatoes", "500g", "₹29", "Fruits & Vegetables", R.drawable.tomato_image),
    Product(2, "Onions", "1kg", "₹39", "Fruits & Vegetables", R.drawable.onion_image),
    Product(3, "Banana", "6 pcs", "₹49", "Fruits & Vegetables", R.drawable.banana_image),
    Product(4, "Apple", "4 pcs", "₹99", "Fruits & Vegetables", R.drawable.apple_image),
    Product(5, "Potatoes", "1kg", "₹45", "Fruits & Vegetables", R.drawable.potato_image),
    Product(6, "Amul Taaza Milk", "500ml", "₹33", "Dairy & Breakfast", R.drawable.milk_image),
    Product(7, "Whole Wheat Bread", "1 packet", "₹50", "Dairy & Breakfast", R.drawable.bread_image),
    Product(8, "Farm Fresh Eggs", "6 pcs", "₹60", "Dairy & Breakfast", R.drawable.eggs_image),
    Product(9, "Amul Butter", "100g", "₹58", "Dairy & Breakfast", R.drawable.butter_image),
    Product(10, "Classic Salted Chips", "50g", "₹20", "Munchies", R.drawable.chips_image),
    Product(11, "Dark Chocolate", "50g", "₹55", "Munchies", R.drawable.choco_image),
    Product(12, "Choco Chip Biscuits", "150g", "₹30", "Munchies", R.drawable.biscuits_image),
    Product(13, "Cola Drink", "750ml", "₹40", "Beverages", R.drawable.cola_image),
    Product(14, "Mixed Fruit Juice", "1L", "₹120", "Beverages", R.drawable.juice_image),
    Product(15, "Bathing Soap", "125g", "₹45", "Personal Care", R.drawable.soap_image),
    Product(16, "Mint Toothpaste", "150g", "₹90", "Personal Care", R.drawable.toothpaste_image)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainScreen(products = myProducts, onLogout = { logout() })
            }
        }
    }

    private fun logout() {
        val sharedPrefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        // FIXED: Do NOT use .clear(). It deletes all registered accounts.
        // Instead, only remove the login session keys.
        sharedPrefs.edit()
            .remove("is_logged_in")
            .remove("current_user_email")
            .apply()

        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

@Composable
fun MainScreen(products: List<Product>, onLogout: () -> Unit) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE) }

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    val currentEmail = sharedPrefs.getString("current_user_email", "") ?: ""
    val userName = sharedPrefs.getString("user_name_$currentEmail", "User") ?: "User"

    var permanentAddress by remember {
        mutableStateOf(sharedPrefs.getString("perm_address", "Set your address in Profile") ?: "")
    }

    val cart = remember { mutableStateListOf<Product>() }
    val wishlist = remember { mutableStateListOf<Product>() }
    var showOrderDialog by remember { mutableStateOf(false) }

    if (showOrderDialog) {
        AlertDialog(
            onDismissRequest = { showOrderDialog = false },
            title = { Text("Order Placed! 🎉", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Your D-Basket delivery is arriving in 10 mins ⚡", fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Delivering to: $permanentAddress", fontSize = 14.sp, color = TextSecondary)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showOrderDialog = false
                        cart.clear()
                        selectedTab = 0
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppPrimaryColor)
                ) {
                    Text("Got it")
                }
            }
        )
    }

    Scaffold(
        bottomBar = { AppBottomNavigation(selectedItem = selectedTab, cartItemCount = cart.size, onItemSelected = { selectedTab = it }) },
        containerColor = AppBackgroundColor
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (selectedTab) {
                0 -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        TopHeader(searchQuery = searchQuery, onSearchQueryChange = { searchQuery = it })
                        CategoryRow(selectedCategory = selectedCategory, onCategorySelected = { selectedCategory = it })
                        val filteredProducts = products.filter { product ->
                            (selectedCategory == "All" || product.category == selectedCategory) &&
                                    product.name.contains(searchQuery, ignoreCase = true)
                        }
                        ProductGrid(filteredProducts, cart, wishlist)
                    }
                }
                1 -> WishlistScreen(wishlist, cart)
                2 -> CartScreen(cart, permanentAddress, onCheckout = { showOrderDialog = true })
                3 -> ProfileScreen(
                    name = userName,
                    email = currentEmail,
                    address = permanentAddress,
                    onAddressSave = { newAddr ->
                        permanentAddress = newAddr
                        sharedPrefs.edit().putString("perm_address", newAddr).apply()
                    },
                    onLogout = onLogout
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopHeader(searchQuery: String, onSearchQueryChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(AppPrimaryColor).padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = "D-Basket", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Text(text = "Express Delivery • 10 mins", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search products...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent, cursorColor = AppPrimaryColor),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp),
            singleLine = true
        )
    }
}

@Composable
fun CategoryRow(selectedCategory: String, onCategorySelected: (String) -> Unit) {
    LazyRow(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(categories) { category ->
            val isSelected = category == selectedCategory
            Surface(onClick = { onCategorySelected(category) }, shape = RoundedCornerShape(8.dp), color = if (isSelected) AppPrimaryColor else Color.White, border = if (!isSelected) BorderStroke(1.dp, Color(0xFFE2E8F0)) else null, modifier = Modifier.clip(RoundedCornerShape(8.dp))) {
                Text(text = category, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) Color.White else TextPrimary)
            }
        }
    }
}

@Composable
fun ProductGrid(products: List<Product>, cart: MutableList<Product>, wishlist: MutableList<Product>) {
    if (products.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No products found", color = TextSecondary) }
    } else {
        LazyVerticalGrid(columns = GridCells.Fixed(2), contentPadding = PaddingValues(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
            items(products) { product -> ProductCard(product, cart, wishlist) }
        }
    }
}

@Composable
fun ProductCard(product: Product, cart: MutableList<Product>, wishlist: MutableList<Product>) {
    val isWishlisted = wishlist.contains(product)
    val cartCount = cart.count { it.id == product.id }
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Image(painter = painterResource(id = product.imageResId), contentDescription = null, modifier = Modifier.fillMaxWidth().height(100.dp).padding(8.dp))
                IconButton(onClick = { if (isWishlisted) wishlist.remove(product) else wishlist.add(product) }, modifier = Modifier.align(Alignment.TopEnd).size(28.dp)) {
                    Icon(imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, contentDescription = null, tint = if (isWishlisted) Color.Red else Color.LightGray, modifier = Modifier.size(20.dp))
                }
            }
            Text(product.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 1, color = TextPrimary)
            Text(product.weight, color = TextSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(product.price, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                if (cartCount == 0) {
                    OutlinedButton(onClick = { cart.add(product) }, shape = RoundedCornerShape(6.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = AppPrimaryColor), modifier = Modifier.height(32.dp).width(64.dp), contentPadding = PaddingValues(0.dp)) { Text("ADD", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                } else {
                    Row(modifier = Modifier.height(32.dp).width(80.dp).background(AppPrimaryColor, RoundedCornerShape(6.dp)), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
                        IconButton(onClick = { cart.remove(product) }, modifier = Modifier.size(20.dp)) { Box(modifier = Modifier.width(10.dp).height(2.dp).background(Color.White)) }
                        Text(cartCount.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        IconButton(onClick = { cart.add(product) }, modifier = Modifier.size(20.dp)) { Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(16.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun WishlistScreen(wishlist: MutableList<Product>, cart: MutableList<Product>) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth().background(AppPrimaryColor).padding(20.dp), contentAlignment = Alignment.Center) { Text("My Wishlist", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
        ProductGrid(products = wishlist, cart = cart, wishlist = wishlist)
    }
}

@Composable
fun CartScreen(cart: MutableList<Product>, permanentAddress: String, onCheckout: () -> Unit) {
    val uniqueItems = cart.distinct()

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth().background(AppPrimaryColor).padding(20.dp), contentAlignment = Alignment.Center) { Text("Checkout", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) }

        if (cart.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Your cart is empty", color = TextSecondary) }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = AppPrimaryColor)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Delivering to Home:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextSecondary)
                            Text(permanentAddress, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                        }
                    }
                }

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(uniqueItems) { product ->
                        val qty = cart.count { it.id == product.id }
                        Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(painter = painterResource(id = product.imageResId), contentDescription = null, modifier = Modifier.size(40.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(product.name, fontWeight = FontWeight.Medium)
                                        Text(product.price, fontWeight = FontWeight.Bold, color = AppPrimaryColor)
                                    }
                                }

                                Row(
                                    modifier = Modifier.height(30.dp).width(90.dp).background(AppPrimaryColor, RoundedCornerShape(6.dp)),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    IconButton(onClick = { cart.remove(product) }, modifier = Modifier.size(20.dp)) {
                                        Box(modifier = Modifier.width(10.dp).height(2.dp).background(Color.White))
                                    }
                                    Text(qty.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    IconButton(onClick = { cart.add(product) }, modifier = Modifier.size(20.dp)) {
                                        Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                val totalAmount = cart.sumOf { p -> p.price.filter { it.isDigit() }.toIntOrNull() ?: 0 }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Total: ₹$totalAmount", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = onCheckout, modifier = Modifier.fillMaxWidth().height(54.dp), colors = ButtonDefaults.buttonColors(containerColor = AppPrimaryColor), shape = RoundedCornerShape(8.dp)) {
                    Text("Place Order", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(name: String, email: String, address: String, onAddressSave: (String) -> Unit, onLogout: () -> Unit) {
    var showEditDialog by remember { mutableStateOf(false) }
    var tempAddress by remember { mutableStateOf(address) }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Update Permanent Address") },
            text = { OutlinedTextField(value = tempAddress, onValueChange = { tempAddress = it }, label = { Text("Enter Address") }, modifier = Modifier.fillMaxWidth()) },
            confirmButton = { Button(onClick = { onAddressSave(tempAddress); showEditDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = AppPrimaryColor)) { Text("Save") } }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth().background(AppPrimaryColor).padding(40.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, modifier = Modifier.size(50.dp), tint = AppPrimaryColor) }
                Spacer(modifier = Modifier.height(12.dp))
                Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Text(email, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        ListItem(
            headlineContent = { Text("Permanent Address") },
            supportingContent = { Text(address) },
            leadingContent = { Icon(Icons.Default.Home, null) },
            trailingContent = { TextButton(onClick = { showEditDialog = true }) { Text("Edit", color = AppPrimaryColor) } }
        )
        ListItem(headlineContent = { Text("Logout") }, leadingContent = { Icon(Icons.Default.ExitToApp, null) }, modifier = Modifier.clickable { onLogout() })
    }
}

@Composable
fun AppBottomNavigation(selectedItem: Int, cartItemCount: Int, onItemSelected: (Int) -> Unit) {
    NavigationBar(containerColor = Color.White) {
        val navItems = listOf(
            Icons.Default.Home to "Home",
            Icons.Default.FavoriteBorder to "Wishlist",
            Icons.Default.ShoppingCart to "Cart",
            Icons.Default.Person to "Profile"
        )
        navItems.forEachIndexed { index, pair ->
            NavigationBarItem(
                selected = selectedItem == index,
                onClick = { onItemSelected(index) },
                icon = {
                    if (index == 2 && cartItemCount > 0) {
                        BadgedBox(badge = { Badge { Text(cartItemCount.toString()) } }) { Icon(pair.first, null) }
                    } else Icon(pair.first, null)
                },
                label = { Text(pair.second, fontSize = 10.sp) }
            )
        }
    }
}