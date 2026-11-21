package com.samtv.app.ui.nav

import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.samtv.app.ui.home.HomeScreen
import com.samtv.app.ui.movie.MovieGridScreen
import com.samtv.app.ui.year.YearScreen

private object Routes {
    const val HOME = "home"
    const val YEAR = "year/{title}/{path}"
    const val MOVIES = "movies/{title}/{url}"

    fun year(title: String, path: String): String =
        "year/${Uri.encode(title)}/${Uri.encode(path)}"

    fun movies(title: String, url: String): String =
        "movies/${Uri.encode(title)}/${Uri.encode(url)}"
}

@Composable
fun SamNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier,
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onCategoryClick = { category ->
                    navController.navigate(Routes.year(category.title, category.path))
                },
            )
        }

        composable(
            route = Routes.YEAR,
            arguments = listOf(
                navArgument("title") { type = NavType.StringType },
                navArgument("path") { type = NavType.StringType },
            ),
        ) { entry ->
            val title = Uri.decode(entry.arguments?.getString("title").orEmpty())
            val path = Uri.decode(entry.arguments?.getString("path").orEmpty())
            YearScreen(
                categoryTitle = title,
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
                    Toast.makeText(context, movie.title, Toast.LENGTH_SHORT).show()
                },
            )
        }
    }
}
