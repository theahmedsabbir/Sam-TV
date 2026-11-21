package com.samtv.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.samtv.app.ui.home.HomeScreen
import com.samtv.app.ui.theme.SamTvTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SamTvTheme {
                HomeScreen(
                    onCategoryClick = { category ->
                        Toast.makeText(
                            this,
                            category.title,
                            Toast.LENGTH_SHORT,
                        ).show()
                    },
                )
            }
        }
    }
}
