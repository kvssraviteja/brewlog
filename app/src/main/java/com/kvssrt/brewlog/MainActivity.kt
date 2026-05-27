package com.kvssrt.brewlog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.kvssrt.brewlog.data.BrewlogDatabase
import com.kvssrt.brewlog.data.BrewlogRepository
import com.kvssrt.brewlog.data.CoffeeBagImageStorage
import com.kvssrt.brewlog.ui.BrewlogApp
import com.kvssrt.brewlog.ui.theme.BrewlogTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = BrewlogRepository(
            BrewlogDatabase.getInstance(this).brewlogDao(),
        )
        val imageStorage = CoffeeBagImageStorage(applicationContext)

        setContent {
            BrewlogTheme {
                BrewlogApp(
                    repository = repository,
                    imageStorage = imageStorage,
                )
            }
        }
    }
}
