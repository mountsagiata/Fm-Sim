package com.mountsa.fmsimulation.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.mountsa.fmsimulation.R
import com.mountsa.fmsimulation.data.local.entities.UserProfileEntity
import com.mountsa.fmsimulation.ui.components.LogoHeader
import com.mountsa.fmsimulation.ui.theme.FM_Green
import com.mountsa.fmsimulation.ui.viewmodel.ProfileViewModel
import com.mountsa.fmsimulation.utils.AudioManager

@Composable
fun ProfileScreen(
    onContinue: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val users by viewModel.profiles.collectAsStateWithLifecycle()
    val selectedUser by viewModel.selectedProfile.collectAsStateWithLifecycle()
    val hasSave by viewModel.hasSave.collectAsStateWithLifecycle()
    
    var showCreateForm by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val audioManager = viewModel.audioManager
    val colorScheme = MaterialTheme.colorScheme

    // Handle initial selection and sync index
    LaunchedEffect(users, selectedUser) {
        if (users.isNotEmpty()) {
            if (selectedUser == null) {
                viewModel.selectProfile(users[0])
                selectedIndex = 0
            } else {
                val idx = users.indexOfFirst { it.id == selectedUser?.id }
                if (idx != -1) selectedIndex = idx
            }
            showCreateForm = false
        } else {
            showCreateForm = true
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "GlowTransition")
    val glowProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "GlowProgress"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .drawBehind {
                val strokeWidth = 1.dp.toPx()
                val gridColor = colorScheme.onBackground.copy(alpha = 0.05f)
                val startX = size.width * 0.55f
                val endX = size.width * 0.33f

                drawLine(
                    color = FM_Green.copy(alpha = 0.15f),
                    start = Offset(startX, 0f),
                    end = Offset(endX, size.height),
                    strokeWidth = strokeWidth
                )

                val head = glowProgress
                val pTail = Offset(
                    x = startX + (endX - startX) * (glowProgress - 0.4f).coerceAtLeast(0f),
                    y = size.height * (glowProgress - 0.4f).coerceAtLeast(0f)
                )
                val pHead = Offset(
                    x = startX + (endX - startX) * head.coerceAtMost(1f),
                    y = size.height * head.coerceAtMost(1f)
                )

                if (pTail != pHead) {
                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(Color.Transparent, FM_Green.copy(alpha = 0.5f), Color.Transparent),
                            start = pTail, end = pHead
                        ),
                        start = pTail, end = pHead, strokeWidth = 4.dp.toPx()
                    )
                }

                drawCircle(
                    color = gridColor,
                    center = Offset(size.width * 0.15f, size.height * 0.5f),
                    radius = 200.dp.toPx(),
                    style = Stroke(strokeWidth)
                )
            }
    ) {
        // Overlay for background effects using splashscreen image
        Image(
            painter = painterResource(id = R.drawable.splashscreen),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.linearGradient(0.3f to Color.Black, 0.45f to Color.Transparent),
                        blendMode = BlendMode.DstIn
                    )
                }
                .graphicsLayer(alpha = 0.15f)
        )

        LogoHeader(
            modifier = Modifier.padding(start = 70.dp).align(Alignment.CenterStart),
            scale = 0.6f,
            showEdition = true
        )

        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(0.4f).fillMaxHeight())

            Box(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxHeight()
                    .background(Brush.horizontalGradient(listOf(Color.Transparent, colorScheme.surface.copy(alpha = 0.85f)))),
                contentAlignment = Alignment.Center
            ) {
                if (showCreateForm) {
                    CreateUserForm(
                        onCancel = {
                            if (users.isNotEmpty()) {
                                audioManager.playClickSound()
                                showCreateForm = false
                            }
                        },
                        onCreate = { name, avatarUri ->
                            audioManager.playClickSound()
                            viewModel.createProfile(name, avatarUri)
                        }
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        UserCarousel(
                            users = users,
                            selectedIndex = selectedIndex,
                            selectedUserId = selectedUser?.id,
                            onIndexChange = {
                                audioManager.playClickSound()
                                selectedIndex = it
                                viewModel.selectProfile(users[it])
                            },
                            onUserClick = {
                                audioManager.playClickSound()
                                viewModel.selectProfile(it)
                            },
                            onUserLongClick = {
                                audioManager.playClickSound()
                                viewModel.deleteProfile(it)
                            },
                            onCreateClick = {
                                audioManager.playClickSound()
                                showCreateForm = true
                            }
                        )

                        AnimatedVisibility(
                            visible = selectedUser != null,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier.padding(top = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (hasSave) {
                                    Button(
                                        onClick = {
                                            audioManager.playClickSound()
                                            viewModel.continueCareer(onContinue)
                                        },
                                        modifier = Modifier.width(220.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = FM_Green),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text("CONTINUE CAREER", color = Color.Black, fontWeight = FontWeight.Black)
                                    }
                                }

                                OutlinedButton(
                                    onClick = {
                                        audioManager.playClickSound()
                                        viewModel.startNewCareer(onContinue)
                                    },
                                    modifier = Modifier.width(220.dp),
                                    border = BorderStroke(1.dp, FM_Green),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("START NEW CAREER", color = FM_Green, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InitialCreateButton(onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .border(1.dp, colorScheme.onSurface.copy(alpha = 0.2f), CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(50.dp), tint = colorScheme.onSurface)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "NEW MANAGER",
            color = colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun UserCarousel(
    users: List<UserProfileEntity>,
    selectedIndex: Int,
    selectedUserId: Long?,
    onIndexChange: (Int) -> Unit,
    onUserClick: (UserProfileEntity) -> Unit,
    onUserLongClick: (UserProfileEntity) -> Unit,
    onCreateClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        IconButton(
            onClick = { if (selectedIndex > 0) onIndexChange(selectedIndex - 1) },
            enabled = selectedIndex > 0
        ) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = null,
                tint = if (selectedIndex > 0) colorScheme.onSurface else colorScheme.outline,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(20.dp))

        if (selectedIndex >= 0 && selectedIndex < users.size) {
            ActiveUserItem(
                user = users[selectedIndex],
                isSelected = selectedUserId == users[selectedIndex].id,
                onClick = { onUserClick(users[selectedIndex]) },
                onLongClick = { onUserLongClick(users[selectedIndex]) }
            )
        }

        Spacer(modifier = Modifier.width(20.dp))

        if (selectedIndex < users.size - 1) {
            IconButton(onClick = { onIndexChange(selectedIndex + 1) }) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = colorScheme.onSurface,
                    modifier = Modifier.size(28.dp)
                )
            }
        } else {
            IconButton(onClick = onCreateClick) {
                Icon(Icons.Default.Add, contentDescription = null, tint = FM_Green, modifier = Modifier.size(28.dp))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ActiveUserItem(user: UserProfileEntity, isSelected: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(130.dp)
                .border(if (isSelected) 2.dp else 0.6.dp, if (isSelected) FM_Green else colorScheme.onSurface.copy(0.3f), CircleShape)
                .padding(6.dp)
                .combinedClickable(onClick = onClick, onLongClick = onLongClick),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(FM_Green.copy(0.1f), Color.Transparent)))
            ) {
                if (user.avatarUri != null) {
                    AsyncImage(
                        model = user.avatarUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(65.dp).align(Alignment.Center),
                        tint = colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = user.name.uppercase(),
            color = if (isSelected) FM_Green else colorScheme.onSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = user.title,
            color = if (isSelected) FM_Green.copy(0.7f) else colorScheme.onSurfaceVariant,
            fontSize = 10.sp,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun CreateUserForm(onCancel: () -> Unit, onCreate: (String, String?) -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    val colorScheme = MaterialTheme.colorScheme

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> if (uri != null) selectedUri = uri }
    )

    Card(
        modifier = Modifier.width(300.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("CREATE NEW PROFILE", color = colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(colorScheme.surfaceVariant)
                    .border(1.5.dp, FM_Green.copy(alpha = 0.5f), CircleShape)
                    .clickable { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                contentAlignment = Alignment.Center
            ) {
                if (selectedUri != null) {
                    AsyncImage(model = selectedUri, contentDescription = "Avatar", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Icon(Icons.Default.AddAPhoto, contentDescription = "Add Photo", tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(36.dp))
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { if (it.length <= 20) name = it },
                placeholder = { Text("Manager Name", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FM_Green,
                    unfocusedBorderColor = colorScheme.outline
                )
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                    Text("CANCEL", color = colorScheme.onSurfaceVariant)
                }
                Button(
                    onClick = { if (name.isNotBlank()) onCreate(name, selectedUri?.toString()) },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = FM_Green),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("CREATE", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
