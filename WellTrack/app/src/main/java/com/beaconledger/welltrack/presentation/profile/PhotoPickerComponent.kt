package com.beaconledger.welltrack.presentation.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun ProfilePhotoSelector(
    currentPhotoUri: Uri?,
    onPhotoSelected: (Uri?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPhotoOptions by remember { mutableStateOf(false) }
    
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        onPhotoSelected(uri)
        showPhotoOptions = false
    }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // Photo was taken successfully, URI is already set
        }
        showPhotoOptions = false
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Photo Display
        Card(
            modifier = Modifier
                .size(120.dp)
                .clickable { showPhotoOptions = true },
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = if (currentPhotoUri != null) Color.Transparent 
                else MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (currentPhotoUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(currentPhotoUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Profile Photo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Default Profile Photo",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                // Overlay for edit indication
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Color.Black.copy(alpha = 0.3f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Edit Photo",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Add/Change Photo Button
        TextButton(
            onClick = { showPhotoOptions = true }
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Photo",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (currentPhotoUri != null) "Change Photo" else "Add Photo")
        }
    }

    // Photo Options Bottom Sheet
    if (showPhotoOptions) {
        PhotoOptionsBottomSheet(
            onGalleryClick = {
                galleryLauncher.launch("image/*")
            },
            onCameraClick = {
                // For camera, we'd need to create a temporary file URI
                // This is a simplified version - in production you'd want proper file handling
                // cameraLauncher.launch(tempUri)
                // For now, just use gallery
                galleryLauncher.launch("image/*")
            },
            onRemoveClick = {
                onPhotoSelected(null)
                showPhotoOptions = false
            },
            onDismiss = { showPhotoOptions = false },
            hasCurrentPhoto = currentPhotoUri != null
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoOptionsBottomSheet(
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit,
    onRemoveClick: () -> Unit,
    onDismiss: () -> Unit,
    hasCurrentPhoto: Boolean
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Profile Photo",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Gallery Option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onGalleryClick() }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Gallery",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Choose from Gallery",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            
            // Camera Option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCameraClick() }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Camera",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Take Photo",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            
            // Remove Option (only if there's a current photo)
            if (hasCurrentPhoto) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRemoveClick() }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Remove",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Remove Photo",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}