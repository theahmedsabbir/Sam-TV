package com.samtv.app.ui.nav

import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.samtv.app.playback.ExternalPlayer
import com.samtv.app.ui.home.HomeScreen
import com.samtv.app.ui.movie.MovieGridScreen
import com.samtv.app.ui.year.YearScreen
import kotlinx.coroutines.launch

private object Routes {
    const val HOME = "home"
    const val YEAR = "year/{title}/{serverId}/{path}"
    const val MOVIES = "movies/{title}/{url}"

    fun year(title: String, serverId: String, path: String): String =
        "year/${Uri.encode(title)}/${Uri.encode(serverId)}/${Uri.encode(path)}"

    fun movies(title: String, url: String): String =
        "movies/${Uri.encode(title)}/${Uri.encode(url)}"
}

@Composable
fun SamNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var opening by remember { mutableStateOf(false) }

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier,
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onCategoryClick = { category ->
                    navController.navigate(
                        Routes.year(category.title, category.serverId, category.path),
                    )
                },
            )
        }

        composable(
            route = Routes.YEAR,
            arguments = listOf(
                navArgument("title") { type = NavType.StringType },
                navArgument("serverId") { type = NavType.StringType },
                navArgument("path") { type = NavType.StringType },
            ),
        ) { entry ->
            val title = Uri.decode(entry.arguments?.getString("title").orEmpty())
            val serverId = Uri.decode(entry.arguments?.getString("serverId").orEmpty())
            val path = Uri.decode(entry.arguments?.getString("path").orEmpty())
            YearScreen(
                categoryTitle = title,
                serverId = serverId,
                categoryPath = path,
                onYearClick = { year ->
                    navController.navigate(Routes.movies(year.name, year.url))
                },
            )
        }

        composable(
            route = Routes.MOVIES,
            arguments = listOf(
                navArgument("title") { type = NavType.StringType },
                navArgument("url") { type = NavType.StringType },
            ),
        ) { entry ->
            val title = Uri.decode(entry.arguments?.getString("title").orEmpty())
            val url = Uri.decode(entry.arguments?.getString("url").orEmpty())
            MovieGridScreen(
                yearTitle = title,
                yearUrl = url,
                onMovieClick = { movie ->
                    if (opening) return@MovieGridScreen
                    opening = true
                    scope.launch {
                        try {
                            Toast.makeText(context, "Opening…", Toast.LENGTH_SHORT).show()
                            ExternalPlayer.playFolder(
                                context = context,
                                folderUrl = movie.folderUrl,
                                title = movie.title,
                            )
                        } finally {
                            opening = false
                        }
                    }
                },
            )
        }
    }
}
