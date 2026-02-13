package com.example.photos101

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.photos101.ui.navigation.Routes
import com.example.photos101.ui.photodetail.PhotoDetailScreen
import com.example.photos101.ui.photodetail.PhotoDetailViewModel
import com.example.photos101.ui.photoslist.PhotosListEvent
import com.example.photos101.ui.photoslist.PhotosListScreen
import com.example.photos101.ui.photoslist.PhotosListViewModel
import com.example.photos101.ui.theme.Photos101Theme
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Photos101Theme {
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                ) { _ -> }
                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Routes.PHOTOS_LIST,
                        modifier = Modifier.padding(innerPadding),
                    ) {
                        composable(Routes.PHOTOS_LIST) {
                            val listViewModel: PhotosListViewModel = koinViewModel()
                            LaunchedEffect(listViewModel.events) {
                                listViewModel.events.collect { event ->
                                    when (event) {
                                        is PhotosListEvent.NavigateToDetail ->
                                            navController.navigate(
                                                Routes.photoDetail(event.photoId, event.secret)
                                            )
                                    }
                                }
                            }
                            PhotosListScreen(
                                modifier = Modifier.fillMaxSize(),
                                viewModel = listViewModel,
                            )
                        }
                        composable(
                            route = Routes.PHOTO_DETAIL,
                            arguments = listOf(
                                navArgument("photoId") { type = NavType.StringType },
                                navArgument("secret") { type = NavType.StringType },
                            ),
                        ) { backStackEntry ->
                            val photoId = backStackEntry.arguments?.getString("photoId") ?: ""
                            val secret = backStackEntry.arguments?.getString("secret")
                            val detailViewModel: PhotoDetailViewModel = koinViewModel(
                                parameters = { parametersOf(photoId, secret) }
                            )
                            PhotoDetailScreen(
                                onBack = { navController.popBackStack() },
                                viewModel = detailViewModel,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                }
            }
        }
    }
}
