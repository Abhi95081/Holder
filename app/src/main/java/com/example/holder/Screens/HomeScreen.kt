
package com.example.holder

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import kotlinx.coroutines.launch

// Data classes
data class ImageFolder(val name: String, val images: List<ImageData>)
data class ImageData(val uri: android.net.Uri, val name: String)

@Composable
fun ImageThumbnail(image: ImageData, onClick: () -> Unit) {
    AsyncImage(
        model = image.uri,
        contentDescription = null,
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() }
    )
}

@Composable
fun MiniImageThumbnail(image: ImageData, onClick: () -> Unit = {}) {
    Box(modifier = Modifier.clickable { onClick() }) {
        AsyncImage(
            model = image.uri,
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .padding(4.dp)
        )
    }
}

@Composable
fun FullScreenImageViewer(images: List<ImageData>, startIndex: Int, onClose: () -> Unit) {
    val pagerState = rememberPagerState(initialPage = startIndex, pageCount = { images.size })
    val scope = rememberCoroutineScope()

    BackHandler(onBack = onClose)

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        Box(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                AsyncImage(
                    model = images[page].uri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }

            LazyRow(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(images.size) { i ->
                    MiniImageThumbnail(image = images[i]) {
                        scope.launch { pagerState.scrollToPage(i) }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val context = LocalContext.current
    var folders by remember { mutableStateOf<List<ImageFolder>>(emptyList()) }
    var selectedFolder by remember { mutableStateOf<ImageFolder?>(null) }
    var permissionGranted by remember { mutableStateOf(false) }
    var fullScreenIndex by remember { mutableStateOf<Int?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionGranted = granted
        if (granted) folders = loadImageFolders(context)
    }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            permissionGranted = true
            folders = loadImageFolders(context)
        } else {
            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
        }
    }

    val isLandscape = LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    BackHandler(enabled = selectedFolder != null && fullScreenIndex == null) {
        selectedFolder = null
    }

    Scaffold(
        topBar = {
            if (!isLandscape || fullScreenIndex == null) {
                TopAppBar(
                    title = { Text(selectedFolder?.name ?: "LeoGuard Gallery") },
                    navigationIcon = {
                        if (selectedFolder != null && fullScreenIndex == null) {
                            IconButton(onClick = { selectedFolder = null }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        }
                    }
                )
            }
        },
        containerColor = Color(0xFFF0F0F0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (permissionGranted) {
                if (selectedFolder != null && fullScreenIndex == null) {
                    val images = selectedFolder!!.images
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text("Photos", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(start = 16.dp, top = 8.dp))
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(120.dp),
                            modifier = Modifier
                                .weight(1f)
                                .padding(8.dp)
                        ) {
                            items(images) { image ->
                                val index = images.indexOf(image)
                                ImageThumbnail(image) { fullScreenIndex = index }
                            }
                        }
                        Text("All Thumbnails", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 16.dp, bottom = 4.dp))
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(images) { image ->
                                MiniImageThumbnail(image)
                            }
                        }
                    }
                } else if (selectedFolder == null) {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(folders) { folder ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedFolder = folder }
                                    .padding(12.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.folder),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(32.dp)
                                )
                                Text(
                                    text = folder.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                Text("Permission required to access media.", modifier = Modifier.align(Alignment.Center))
            }
        }
    }

    selectedFolder?.let { folder ->
        fullScreenIndex?.let { index ->
            FullScreenImageViewer(images = folder.images, startIndex = index, onClose = { fullScreenIndex = null })
        }
    }
}

fun loadImageFolders(context: Context): List<ImageFolder> {
    val folders = mutableMapOf<String, MutableList<ImageData>>()
    val projection = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DISPLAY_NAME,
        MediaStore.Images.Media.BUCKET_DISPLAY_NAME
    )

    val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
    val query = context.contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection,
        null,
        null,
        sortOrder
    )

    query?.use { cursor ->
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
        val bucketColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

        while (cursor.moveToNext()) {
            val id = cursor.getLong(idColumn)
            val name = cursor.getString(nameColumn)
            val bucket = cursor.getString(bucketColumn)
            val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            val image = ImageData(uri, name)
            folders.getOrPut(bucket) { mutableListOf() }.add(image)
        }
    }

    return folders.map { ImageFolder(it.key, it.value) }
}
