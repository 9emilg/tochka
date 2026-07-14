package bg.tochka.reader.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import bg.tochka.reader.ui.article.ArticleScreen
import bg.tochka.reader.ui.components.TochkaBottomNavBar
import bg.tochka.reader.ui.disclaimer.DisclaimerScreen
import bg.tochka.reader.ui.home.HomeScreen
import bg.tochka.reader.ui.saved.SavedScreen
import bg.tochka.reader.ui.search.SearchScreen
import bg.tochka.reader.ui.settings.SettingsScreen

private val topLevelRoutes = setOf(Destinations.HOME, Destinations.SAVED, Destinations.SETTINGS)

@Composable
fun TochkaNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in topLevelRoutes) {
                TochkaBottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destinations.HOME,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Destinations.HOME) {
                HomeScreen(
                    onArticleClick = { article -> navController.navigate(Destinations.article(article.id)) },
                    onSearchClick = { navController.navigate(Destinations.SEARCH) },
                )
            }
            composable(Destinations.SAVED) {
                SavedScreen(
                    onArticleClick = { article -> navController.navigate(Destinations.article(article.id)) },
                    onBrowseHome = {
                        navController.navigate(Destinations.HOME) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(Destinations.SETTINGS) {
                SettingsScreen(onAboutClick = { navController.navigate(Destinations.ABOUT) })
            }
            composable(Destinations.SEARCH) {
                SearchScreen(
                    onBack = { navController.popBackStack() },
                    onArticleClick = { article -> navController.navigate(Destinations.article(article.id)) },
                )
            }
            composable(Destinations.ABOUT) {
                DisclaimerScreen(onDismiss = { navController.popBackStack() })
            }
            composable(
                route = Destinations.ARTICLE,
                arguments = listOf(navArgument(Destinations.ARTICLE_ID_ARG) { type = NavType.IntType }),
            ) {
                ArticleScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
