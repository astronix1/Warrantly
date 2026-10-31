package com.example.warrantly.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.warrantly.data.preferences.DataStoreManager
import com.example.warrantly.domain.repository.DocumentRepository
import com.example.warrantly.domain.repository.ProductRepository
import com.example.warrantly.notifications.NotificationScheduler
import com.example.warrantly.ui.addproduct.AddProductScreen
import com.example.warrantly.ui.addproduct.EditProductScreen
import com.example.warrantly.ui.documents.DocumentsScreen
import com.example.warrantly.ui.home.HomeScreen
import com.example.warrantly.ui.productdetail.ProductDetailScreen
import com.example.warrantly.ui.products.ItemsScreen
import com.example.warrantly.ui.settings.SettingsScreen

private data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Screen.Items, "Items", Icons.Filled.Inventory2, Icons.Outlined.Inventory2),
    BottomNavItem(Screen.Documents, "Documents", Icons.Filled.Description, Icons.Outlined.Description),
    BottomNavItem(Screen.Settings, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings),
)

private val fabDestinations = setOf(Screen.Home.route, Screen.Items.route)
private val bottomNavRoutes = bottomNavItems.map { it.screen.route }.toSet()

@Composable
fun WarrantlyNavGraph(
    productRepository: ProductRepository,
    documentRepository: DocumentRepository,
    dataStoreManager: DataStoreManager,
    notificationScheduler: NotificationScheduler,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val showBottomBar = currentRoute in bottomNavRoutes
    val showFab = currentRoute in fabDestinations

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy
                            ?.any { it.route == item.screen.route } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.AddProduct.route) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add product"
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    productRepository = productRepository,
                    dataStoreManager = dataStoreManager,
                    onProductClick = { productId ->
                        navController.navigate(Screen.ProductDetail.createRoute(productId))
                    },
                )
            }

            composable(Screen.Items.route) {
                ItemsScreen(
                    productRepository = productRepository,
                    onProductClick = { productId ->
                        navController.navigate(Screen.ProductDetail.createRoute(productId))
                    },
                )
            }

            composable(Screen.Documents.route) {
                DocumentsScreen(
                    documentRepository = documentRepository,
                    productRepository = productRepository,
                    onProductClick = { productId ->
                        navController.navigate(Screen.ProductDetail.createRoute(productId))
                    },
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    dataStoreManager = dataStoreManager,
                    notificationScheduler = notificationScheduler,
                    productRepository = productRepository,
                )
            }

            composable(Screen.AddProduct.route) {
                AddProductScreen(
                    productRepository = productRepository,
                    documentRepository = documentRepository,
                    notificationScheduler = notificationScheduler,
                    dataStoreManager = dataStoreManager,
                    onProductSaved = { navController.popBackStack() },
                    onNavigateUp = { navController.popBackStack() },
                )
            }

            composable(
                route = Screen.EditProduct.route,
                arguments = listOf(
                    navArgument("productId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getLong("productId") ?: return@composable
                EditProductScreen(
                    productId = productId,
                    productRepository = productRepository,
                    documentRepository = documentRepository,
                    notificationScheduler = notificationScheduler,
                    dataStoreManager = dataStoreManager,
                    onProductSaved = { navController.popBackStack() },
                    onNavigateUp = { navController.popBackStack() },
                )
            }

            composable(
                route = Screen.ProductDetail.route,
                arguments = listOf(
                    navArgument("productId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getLong("productId") ?: return@composable
                ProductDetailScreen(
                    productId = productId,
                    productRepository = productRepository,
                    documentRepository = documentRepository,
                    notificationScheduler = notificationScheduler,
                    onEditClick = { id ->
                        navController.navigate(Screen.EditProduct.createRoute(id))
                    },
                    onNavigateUp = { navController.popBackStack() },
                )
            }
        }
    }
}
