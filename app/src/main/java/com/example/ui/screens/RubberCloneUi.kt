package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AnalysisEntity
import com.example.data.CloneInfo
import com.example.data.UserEntity
import com.example.ui.RubberCloneViewModel
import com.example.ui.Screen
import coil.compose.AsyncImage
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun RubberCloneApp(viewModel: RubberCloneViewModel) {
    val context = LocalContext.current
    val currentScreen = viewModel.currentScreen

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
            when (screen) {
                is Screen.Splash -> SplashScreen(
                    onTimeout = {
                        val savedEmail = viewModel.currentUserEmail
                        if (savedEmail != null) {
                            viewModel.currentScreen = Screen.Dashboard
                        } else {
                            viewModel.currentScreen = Screen.Login
                        }
                    }
                )
                is Screen.Login -> LoginScreen(viewModel)
                is Screen.Register -> RegisterScreen(viewModel)
                is Screen.Dashboard,
                is Screen.AnalisisKlon,
                is Screen.SejarahAnalisis,
                is Screen.PetaLokasi,
                is Screen.CadanganPintar,
                is Screen.ProfilPengguna,
                is Screen.CloneDirectory,
                is Screen.LaporanId -> MainScaffold(viewModel, initialScreen = screen)
            }
        }
    }
}

// ==========================================
// 1. SPLASH SCREEN
// ==========================================
@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    var progress by remember { mutableStateOf(0.1f) }

    LaunchedEffect(Unit) {
        // Simulasikan loading logo botanical getah RISDA
        while (progress < 1.0f) {
            delay(100)
            progress += 0.08f
        }
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF04130A),
                        Color(0xFF0A2B18),
                        Color(0xFF05120A)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            AsyncImage(
                model = "https://rubberclone-ai.pats.my/assets/images/risda_logo.jpg",
                contentDescription = "RISDA Logo",
                placeholder = painterResource(id = com.example.R.drawable.risda_logo),
                error = painterResource(id = com.example.R.drawable.risda_logo),
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
                    .border(2.dp, Color(0xFFF59E0B), CircleShape),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Sistem Pengecaman\nKlon Pokok Getah",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "RISDA BESUT",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF59E0B),
                letterSpacing = 1.5.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(60.dp))

            // Progress bar moden
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .width(180.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            )
        }
    }
}

// ==========================================
// 2. LOGIN SCREEN
// ==========================================
@Composable
fun LoginScreen(viewModel: RubberCloneViewModel) {
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Botanical background drawing
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawCircle(
                        color = Color(0xFF104A27),
                        radius = size.width,
                        center = Offset(size.width, 0f),
                        alpha = 0.12f
                    )
                }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AsyncImage(
                model = "https://rubberclone-ai.pats.my/assets/images/risda_logo.jpg",
                contentDescription = "RISDA Logo",
                placeholder = painterResource(id = com.example.R.drawable.risda_logo),
                error = painterResource(id = com.example.R.drawable.risda_logo),
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.5.dp, Color(0xFFF59E0B), CircleShape),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Sistem Pengecaman Klon Pokok Getah",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Text(
                text = "RISDA BESUT - Log Masuk Pengguna",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFF59E0B),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
            )

            if (viewModel.loginError.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error Logo",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = viewModel.loginError,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            OutlinedTextField(
                value = viewModel.loginEmail,
                onValueChange = { viewModel.loginEmail = it },
                label = { Text("E-mel Pemeriksa (contoh: ahmad@risda.gov.my)") },
                placeholder = { Text("ahmad@risda.gov.my") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_email_input")
                    .padding(bottom = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
            )

            OutlinedTextField(
                value = viewModel.loginPassword,
                onValueChange = { viewModel.loginPassword = it },
                label = { Text("Kata Laluan") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Password"
                        )
                    }
                },
                singleLine = true,
                visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_password_input")
                    .padding(bottom = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
            )

            // Tip demo untuk log masuk pantas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .border(
                        BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        RoundedCornerShape(8.dp)
                    )
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Demo Tip",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Demo Akaun RISDA:\ne-mel: ahmad@risda.gov.my | k/laluan: 123456",
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            Button(
                onClick = { viewModel.processLogin() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("login_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "Buka Portal",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Akaun pengguna hanya boleh ditambah oleh Pentadbir RISDA melalui portal web.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )
        }
    }
}

// ==========================================
// 3. REGISTER SCREEN
// ==========================================
@Composable
fun RegisterScreen(viewModel: RubberCloneViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.AppRegistration,
                contentDescription = "Reg",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(60.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Daftar RubberClone AI",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Daftar profil lapangan anda sebagai Pekebun Kecil / Pegawai RISDA",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            if (viewModel.regError.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error Logo",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = viewModel.regError,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            OutlinedTextField(
                value = viewModel.regFullname,
                onValueChange = { viewModel.regFullname = it },
                label = { Text("Nama Penuh") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Person") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = viewModel.regEmail,
                onValueChange = { viewModel.regEmail = it },
                label = { Text("Alamat E-mel") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = viewModel.regUsername,
                onValueChange = { viewModel.regUsername = it },
                label = { Text("Nama Isian Log masuk") },
                leadingIcon = { Icon(Icons.Default.ManageAccounts, contentDescription = "User") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = viewModel.regPassword,
                onValueChange = { viewModel.regPassword = it },
                label = { Text("Kata Laluan Baharu") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock") },
                singleLine = true,
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = viewModel.regAgency,
                onValueChange = { viewModel.regAgency = it },
                label = { Text("Agensi / Wilayah RISDA (contoh: RISDA Perak)") },
                leadingIcon = { Icon(Icons.Default.Domain, contentDescription = "Agency") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )

            Button(
                onClick = { viewModel.processRegister() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("register_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "Daftar & Mula Imbasan",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Sudah mempunyai akaun?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = { viewModel.currentScreen = Screen.Login }
                ) {
                    Text(
                        text = "Masuk Di Sini",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ==========================================
// 4. MAIN SCAFFOLD & BOTTOM NAV
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(viewModel: RubberCloneViewModel, initialScreen: Screen) {
    var activeTab by remember { mutableStateOf(initialScreen) }
    var showMenuSheet by remember { mutableStateOf(false) }

    // Selaraskan tab dengan perubahan skrin vm
    LaunchedEffect(viewModel.currentScreen) {
        val current = viewModel.currentScreen
        if (current !is Screen.Splash && current !is Screen.Login && current !is Screen.Register) {
            activeTab = current
        }
    }

    // Bottom Sheet Menu Burger
    if (showMenuSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMenuSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Menu Lanjutan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Peta Lokasi
                Surface(
                    onClick = {
                        showMenuSheet = false
                        activeTab = Screen.PetaLokasi
                        viewModel.currentScreen = Screen.PetaLokasi
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Map,
                            contentDescription = "Peta",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                "Peta Lokasi Imbasan",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Lihat taburan geografi imbasan klon getah",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // AgroPintar
                Surface(
                    onClick = {
                        showMenuSheet = false
                        activeTab = Screen.CadanganPintar
                        viewModel.currentScreen = Screen.CadanganPintar
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Memory,
                            contentDescription = "AgroPintar",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                "AgroPintar AI",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Cadangan klon pintar berasaskan AI Gemini",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Direktori Klon
                Surface(
                    onClick = {
                        showMenuSheet = false
                        activeTab = Screen.CloneDirectory
                        viewModel.currentScreen = Screen.CloneDirectory
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Biotech,
                            contentDescription = "Direktori Klon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                "Direktori Klon Hevea",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Ensiklopedia baka klon getah RISDA",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Log Keluar
                Surface(
                    onClick = {
                        showMenuSheet = false
                        viewModel.processLogout()
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Logout,
                            contentDescription = "Log Keluar",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            "Log Keluar",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        bottomBar = {
            Box(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                // Background navigation bar
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    // 1. Dashboard
                    NavigationBarItem(
                        selected = activeTab is Screen.Dashboard,
                        onClick = {
                            activeTab = Screen.Dashboard
                            viewModel.currentScreen = Screen.Dashboard
                        },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        label = { Text("Dashboard", fontSize = 10.sp, maxLines = 1) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    // 2. Sejarah
                    NavigationBarItem(
                        selected = activeTab is Screen.SejarahAnalisis || activeTab is Screen.LaporanId,
                        onClick = {
                            activeTab = Screen.SejarahAnalisis
                            viewModel.currentScreen = Screen.SejarahAnalisis
                        },
                        icon = { Icon(Icons.Default.History, contentDescription = "Sejarah") },
                        label = { Text("Sejarah", fontSize = 10.sp, maxLines = 1) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    // 3. Spacer for center FAB
                    NavigationBarItem(
                        selected = false,
                        onClick = {},
                        icon = { Spacer(modifier = Modifier.size(24.dp)) },
                        label = { Text("") },
                        enabled = false,
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        )
                    )

                    // 4. Profil
                    NavigationBarItem(
                        selected = activeTab is Screen.ProfilPengguna,
                        onClick = {
                            activeTab = Screen.ProfilPengguna
                            viewModel.currentScreen = Screen.ProfilPengguna
                        },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profil") },
                        label = { Text("Profil", fontSize = 10.sp, maxLines = 1) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    // 5. Menu (burger)
                    NavigationBarItem(
                        selected = activeTab is Screen.PetaLokasi || activeTab is Screen.CadanganPintar || activeTab is Screen.CloneDirectory,
                        onClick = { showMenuSheet = true },
                        icon = { Icon(Icons.Default.Menu, contentDescription = "Menu") },
                        label = { Text("Menu", fontSize = 10.sp, maxLines = 1) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                // Center FAB Camera Button (floating above the nav bar)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-22).dp)
                ) {
                    FloatingActionButton(
                        onClick = {
                            activeTab = Screen.AnalisisKlon
                            viewModel.currentScreen = Screen.AnalisisKlon
                        },
                        containerColor = Color(0xFFF59E0B),
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(64.dp),
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 10.dp
                        )
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Imbas Klon",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    (fadeIn() + scaleIn(initialScale = 0.98f))
                        .togetherWith(fadeOut())
                },
                label = "MainTabsTransitions"
            ) { tab ->
                when (tab) {
                    is Screen.Dashboard -> DashboardScreen(viewModel)
                    is Screen.AnalisisKlon -> LeafScannerScreen(viewModel)
                    is Screen.SejarahAnalisis -> HistoryScreen(viewModel)
                    is Screen.PetaLokasi -> MapViewScreen(viewModel)
                    is Screen.CadanganPintar -> RecommendationScreen(viewModel)
                    is Screen.CloneDirectory -> CloneDirectoryScreen(viewModel)
                    is Screen.ProfilPengguna -> UserProfileScreen(viewModel)
                    is Screen.LaporanId -> PdfViewScreen(viewModel, tab.recordId)
                    else -> DashboardScreen(viewModel)
                }
            }
        }
    }
}


// ==========================================
// 5. DASHBOARD SCREEN
// ==========================================
@Composable
fun DashboardScreen(viewModel: RubberCloneViewModel) {
    val context = LocalContext.current
    val historyList by viewModel.analysesForUser.collectAsStateWithLifecycle()

    // Kira data statistik untuk dipetakan dalam Canvas yang cantik!
    val countsByClone = historyList.groupBy { it.cloneName }
        .mapValues { it.value.size }
    val totalScans = historyList.size

    val topClone = if (countsByClone.isNotEmpty()) {
        countsByClone.maxByOrNull { it.value }?.key
    } else null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
    ) {
        // Welcoming Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Selamat Datang,",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = viewModel.currentUser?.fullname ?: "Pekebun RISDA",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Negeri: ${viewModel.currentUser?.agency ?: "RISDA Wilayah"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
                )
            }

            IconButton(
                onClick = { viewModel.currentScreen = Screen.ProfilPengguna },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profil",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(34.dp)
                )
            }
        }

        // Summary Statistics Box
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Jumlah Imbasan",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$totalScans",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Klon Dominan",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = topClone ?: "Tiada",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Stat Graf Kanvas Custom (Drawn directly in Compose Canvas for premium accuracy and offline responsiveness)
        Text(
            text = "Taburan Klon Pokok Getah Diimbas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .padding(bottom = 20.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            if (countsByClone.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.BarChart,
                            contentDescription = "Carta Kosong",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tiada rekod data diimbas lagi.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Vector Canvas Bar Chart
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    countsByClone.entries.forEach { (cloneName, count) ->
                        val pct = if (totalScans > 0) count.toFloat() / totalScans else 0f
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            // Jumlah kaunter atas bar
                            Text(
                                text = "$count",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            // Bar vector
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight(0.7f * pct + 0.15f) // scale bars beautifully
                                    .width(36.dp)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.secondary
                                            )
                                        )
                                    )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            // Nama label klon
                            Text(
                                text = cloneName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Quick Shortcut Action Buttons Grid
        Text(
            text = "Navigasi Pantas RISDA",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ElevatedCard(
                onClick = { viewModel.currentScreen = Screen.AnalisisKlon },
                modifier = Modifier
                    .weight(1f)
                    .height(100.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Camera,
                        contentDescription = "Scan",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Imbas Daun",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            ElevatedCard(
                onClick = { viewModel.currentScreen = Screen.CadanganPintar },
                modifier = Modifier
                    .weight(1f)
                    .height(100.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = "Recomm",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Saranan Klon",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Latest News / Info RISDA Cards
        Text(
            text = "Pekeliling & Makluman RISDA Semasa",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Newspaper,
                        contentDescription = "News Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MAKLUMAN INTENSIF BAJA GETAH RISDA",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Bantuan Skim Baja RISDA 2026 kini dibuka untuk permohonan berskala besar di seluruh Semenanjung Malaysia. Pastikan klon getah yang ditanam berdaftar.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ==========================================
// 6. SENSOR SCANNER / LEAF ANALYSIS SCREEN (AI Predict)
// ==========================================
@Composable
fun LeafScannerScreen(viewModel: RubberCloneViewModel) {
    val context = LocalContext.current
    var isMockPermissionActive by remember { mutableStateOf(true) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            viewModel.initGpsTracking()
        }
    }

    LaunchedEffect(Unit) {
        val hasFine = androidx.core.content.ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val hasCoarse = androidx.core.content.ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            locationPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            viewModel.initGpsTracking()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopGpsTracking()
        }
    }

    var photoFile by remember { mutableStateOf<java.io.File?>(null) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    // Memuat naik imej menggunakan pemilih Android Intent
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    viewModel.leafScanSource = "Guna Gambar"
                    viewModel.analyzeLeafImage(bitmap)
                }
            } catch (e: Exception) {
                android.util.Log.e("LeafScanner", "Ralat memuatkan imej dari galeri: ${e.message}", e)
                Toast.makeText(context, "Ralat galeri: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Capture imej menggunakan kamera (Menyimpan gambar sebenar)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && photoFile != null) {
            try {
                val bitmap = BitmapFactory.decodeFile(photoFile!!.absolutePath)
                if (bitmap != null) {
                    viewModel.leafScanSource = "Guna Gambar"
                    viewModel.analyzeLeafImage(bitmap)
                } else {
                    // Fallback jika decode gagal
                    val width = 400
                    val height = 400
                    val conf = Bitmap.Config.ARGB_8888
                    val mockBitmap = Bitmap.createBitmap(width, height, conf)
                    val canvas = android.graphics.Canvas(mockBitmap)
                    val paint = android.graphics.Paint()
                    paint.color = android.graphics.Color.parseColor("#10B981")
                    canvas.drawRect(0f, 0f, 400f, 400f, paint)
                    viewModel.leafScanSource = "Guna Gambar"
                    viewModel.analyzeLeafImage(mockBitmap)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Ralat membaca imej kamera: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Jika dibatalkan atau tiada kamera (Emulator)
            val width = 400
            val height = 400
            val conf = Bitmap.Config.ARGB_8888
            val mockBitmap = Bitmap.createBitmap(width, height, conf)
            val canvas = android.graphics.Canvas(mockBitmap)
            val paint = android.graphics.Paint()
            paint.color = android.graphics.Color.parseColor("#10B981")
            canvas.drawRect(0f, 0f, 400f, 400f, paint)
            
            viewModel.leafScanSource = "Guna Gambar"
            viewModel.analyzeLeafImage(mockBitmap)
        }
    }

    // Pengeboman keizinan Kamera untuk peranti fizikal / emulator keselamatan
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            try {
                val file = java.io.File(context.cacheDir, "camera_photo_${System.currentTimeMillis()}.jpg")
                photoFile = file
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                photoUri = uri
                cameraLauncher.launch(uri)
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal menyediakan fail kamera: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Kebenaran kamera diperlukan untuk mengambil gambar di lapangan.", Toast.LENGTH_SHORT).show()
        }
    }

    fun checkAndLaunchCamera() {
        val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CAMERA
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        
        if (hasPermission) {
            try {
                val file = java.io.File(context.cacheDir, "camera_photo_${System.currentTimeMillis()}.jpg")
                photoFile = file
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                photoUri = uri
                cameraLauncher.launch(uri)
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal menyediakan fail kamera: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            permissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "Penganalisis AI Daun Getah",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Text(
                text = "Halakan kamera telefon pada helaian daun getah tunggal atau muat naik dari galeri untuk analisis klon terpantas.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 20.dp)
            )
        }

        // GPS Status
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (viewModel.isGpsReady) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    } else {
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                    }
                )
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (viewModel.isGpsReady) Icons.Default.Place else Icons.Default.Warning,
                        contentDescription = "GPS Status",
                        tint = if (viewModel.isGpsReady) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (viewModel.isGpsReady) {
                            val latStr = String.format(java.util.Locale.US, "%.4f", viewModel.gpsLatitude)
                            val lngStr = String.format(java.util.Locale.US, "%.4f", viewModel.gpsLongitude)
                            "GPS Bersedia: Lat: $latStr, Lng: $lngStr"
                        } else {
                            viewModel.gpsError ?: "Menunggu isyarat GPS..."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (viewModel.isGpsReady) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        // Paparan Ralat Analisis (jika ada)
        if (viewModel.analysisError != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = viewModel.analysisError!!,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }

        // Kamera Scan Viewfinder (Simulated beautiful graphical container)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                        RoundedCornerShape(16.dp)
                    )
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable {
                        checkAndLaunchCamera()
                    },
                contentAlignment = Alignment.Center
            ) {
                if (viewModel.scanBitmap != null) {
                    Image(
                        bitmap = viewModel.scanBitmap!!.asImageBitmap(),
                        contentDescription = "Analyzed leaf",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Cam Finder",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Tekan untuk Buka Kamera Lapangan",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Atau guna senarai sampel emulator di bawah",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Scanning Overlay Effect if loading
                if (viewModel.isAnalyzing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.65f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Menghubungi Enjin AI RISDA...",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Core Trigger Buttons
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { checkAndLaunchCamera() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = "Kamera")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ambil Gambar", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = "Galeri", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pilih Galeri", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        }

        // SAMPLES SECTION: SANGAT UTAMA BAGI PENGUJI PERANTI EMULATOR!
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Eco,
                    contentDescription = "Samples",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "UJIAN EMULATOR: Klik Sampel Di Bawah",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Text(
                text = "Klik salah satu ikon pautan daun getah baka asli RISDA untuk mensimulasikan analisis pintar serta-merta tanpa gambar fizikal.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        items(viewModel.clonesInfo) { clone ->
            OutlinedCard(
                onClick = {
                    // Cipta fail bitmap tiruan bertemakan warna daun yang dikehendaki
                    val width = 300
                    val height = 300
                    val conf = Bitmap.Config.ARGB_8888
                    val bitmap = Bitmap.createBitmap(width, height, conf)
                    // Hantar isian pratetap ke VM
                    viewModel.leafScanSource = "Sampel"
                    viewModel.analyzeLeafImage(bitmap)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Forest,
                        contentDescription = "Leaf icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Sampel Daun: ${clone.name}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = clone.series,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForwardIos,
                        contentDescription = "Analisis",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // PREDICTION ANALYSIS OUTPUT RESULT
        if (viewModel.predictionResult != null) {
            val record = viewModel.predictionResult!!
            item {
                Spacer(modifier = Modifier.height(28.dp))
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("prediction_result_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "KEPUTUSAN AI DAUN",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Text(
                                    text = "${String.format("%.1f", record.confidence * 100)}% Padanan",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = record.cloneName,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Text(
                            text = "Tarikh Ambilan Semasa: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(record.timestamp))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                        Spacer(modifier = Modifier.height(12.dp))

                        // Butiran botani
                        Text(
                            text = "Huraian Agro-Morfologi:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = record.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                        )

                        Row(modifier = Modifier.padding(bottom = 8.dp)) {
                            Text(
                                text = "Tanah Sesuai: ",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = record.soilType,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(modifier = Modifier.padding(bottom = 8.dp)) {
                            Text(
                                text = "Potensi Hasil: ",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = record.rainfall,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(modifier = Modifier.padding(bottom = 14.dp)) {
                            Text(
                                text = "Ketinggian Max: ",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = record.elevation,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.currentScreen = Screen.LaporanId(record.id)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "PDF")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Buka Sijil Laporan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    viewModel.generateAndSharePdf(context, record)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "WhatsApp")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("WhatsApp PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 7. SEJARAH ANALISIS SCREEN
// ==========================================
@Composable
fun HistoryScreen(viewModel: RubberCloneViewModel) {
    val context = LocalContext.current
    val historyList by viewModel.analysesForUser.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Rekod Imbasan Daun",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Senarai analisis dan sijil digital milik akaun anda.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (historyList.isNotEmpty()) {
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.clearHistory(viewModel.currentUserEmail ?: "")
                            Toast.makeText(context, "Sejarah berjaya dikosongkan.", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Padam Semua",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        if (historyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = "Open Fold",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Tiada Rekod Dijumpai",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Sila lakukan imbasan AI di tab 'Imbas AI' dahulu.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(historyList) { record ->
                    HistoryCardItem(
                        record = record,
                        onOpen = {
                            viewModel.currentScreen = Screen.LaporanId(record.id)
                        },
                        onShare = {
                            viewModel.generateAndSharePdf(context, record)
                        },
                        onDelete = {
                            viewModel.deleteHistoryRecord(record)
                            Toast.makeText(context, "Rekod berjaya dipadam.", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryCardItem(
    record: AnalysisEntity,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            val formattedDate = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(record.timestamp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = record.cloneName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "${String.format("%.1f", record.confidence * 100)}% Padanan",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Padam",
                            tint = Color.Red.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (record.notes.length > 95) record.notes.substring(0, 95) + "..." else record.notes,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = "GPS",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Lat: ${String.format("%.4f", record.latitude)}, Lng: ${String.format("%.4f", record.longitude)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpen,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sijil PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onShare,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Share", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Siri WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// 8. GP VECTOR MAP VIEW SCREEN (PETA LOKASI)
// ==========================================
@Composable
fun MapViewScreen(viewModel: RubberCloneViewModel) {
    val context = LocalContext.current
    val historyList by viewModel.analysesForUser.collectAsStateWithLifecycle()
    var selectedRecordOnMap by remember { mutableStateOf<AnalysisEntity?>(null) }

    // State untuk zoom dan pan interaktif dalam Canvas peta
    var zoomScale by remember { mutableStateOf(1f) }
    var translationOffset by remember { mutableStateOf(Offset(0f, 0f)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
    ) {
        Text(
            text = "Peta Geografi Pokok Getah",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Plot koordinat kedudukan pokok yang telah berjaya dikesan AI di lapangan RISDA.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Interactive Canvas Map Component (Flawless alternative that never crashes due to Map API issues!)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                .background(Color(0xFF030A05))
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        zoomScale = (zoomScale * zoom).coerceIn(0.7f, 3.5f)
                        translationOffset += pan
                    }
                }
        ) {
            // Lukisan Peta Canvas Vector
            Canvas(modifier = Modifier.fillMaxSize()) {
                val mapWidth = size.width
                val mapHeight = size.height

                // Melakukan transformasi zoom/pan
                val gridStroke = 0.5f
                val gridGap = 65f

                // Lakar Garis Latitud/Longitud Hijau Grid
                for (x in 0..(mapWidth / gridGap).toInt()) {
                    drawLine(
                        color = Color(0xFF1B3D25),
                        start = Offset(x * gridGap + translationOffset.x, 0f),
                        end = Offset(x * gridGap + translationOffset.x, mapHeight),
                        strokeWidth = gridStroke
                    )
                }
                for (y in 0..(mapHeight / gridGap).toInt()) {
                    drawLine(
                        color = Color(0xFF1B3D25),
                        start = Offset(0f, y * gridGap + translationOffset.y),
                        end = Offset(mapWidth, y * gridGap + translationOffset.y),
                        strokeWidth = gridStroke
                    )
                }

                // Melukis Garis Kontur Negeri Semenanjung Malaysia (Tiruan yang sungguh realistik)
                val linePaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#145B25")
                    strokeWidth = 2.5f
                    style = android.graphics.Paint.Style.STROKE
                    pathEffect = android.graphics.DashPathEffect(floatArrayOf(15f, 15f), 0f)
                }

                // Lukis lingkaran rupa bumi
                drawCircle(
                    color = Color(0xFF0A2613),
                    radius = 210f * zoomScale,
                    center = Offset(mapWidth / 2f + translationOffset.x, mapHeight / 2.3f + translationOffset.y),
                    style = Stroke(width = 1.3f)
                )

                // Lukis Label Geografi Semenanjung Utara, Tengah, Selatan
                val textPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#8DB09E")
                    textSize = 34f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawContext.canvas.nativeCanvas.drawText("ZON UTARA", mapWidth / 2f + translationOffset.x, mapHeight / 4.4f + translationOffset.y, textPaint)
                drawContext.canvas.nativeCanvas.drawText("ZON SELATAN", mapWidth / 2f + translationOffset.x, mapHeight / 1.5f + translationOffset.y, textPaint)
            }

            // Melukis Pin Pokok secara Dinamik mengikut koordinat GPS dalam Rekod Sejarah!
            historyList.forEach { record ->
                val normalizedX = remember(record.longitude) { ((record.longitude - 101.0) * 1100f).toFloat() }
                val normalizedY = remember(record.latitude) { (350f - (record.latitude - 2.0) * 850f).toFloat() }

                val pinOffsetX = normalizedX * zoomScale + translationOffset.x
                val pinOffsetY = normalizedY * zoomScale + translationOffset.y

                Box(
                    modifier = Modifier
                        .offset { IntOffset(pinOffsetX.roundToInt(), pinOffsetY.roundToInt()) }
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                        .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable { selectedRecordOnMap = record },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Eco,
                        contentDescription = "Pokok Pin",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Panduan Hint dalam Peta
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = "Guna gerak isyarat cubit (pinch) & seret untuk kembara peta",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }

            // Floating Sheet Info Box when tapping a Pin marker on Map
            if (selectedRecordOnMap != null) {
                val selected = selectedRecordOnMap!!
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(14.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LOKASI PIN DAUN",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = { selectedRecordOnMap = null },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                            }
                        }

                        Text(
                            text = selected.cloneName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = "Tahap keyakinan AI: ${String.format("%.1f", selected.confidence * 100)}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "Huraian tapak: ${selected.notes}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.currentScreen = Screen.LaporanId(selected.id)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Buka Sijil Laporan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    viewModel.generateAndSharePdf(context, selected)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text(" WhatsApp PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 9. CADANGAN KLON PINTAR (AI Agro Recommendations)
// ==========================================
@Composable
fun RecommendationScreen(viewModel: RubberCloneViewModel) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = "Agro AI",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(34.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Cadangan Klon Pintar (Inovasi)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Gunakan enjin pengkomputeran agronomik RISDA untuk menentukan keserasian klon terbaik bagi rupa bumi kebun anda.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Input Jenis Tanah
                Text(
                    text = "Jenis Tanah Kebun",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = viewModel.inputSoilType,
                    onValueChange = { viewModel.inputSoilType = it },
                    placeholder = { Text("Contoh: Sandy Clay, Lateral Merah") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                )

                // Input Hujan
                Text(
                    text = "Purata Hujan Setahun (mm)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = viewModel.inputRainfall,
                    onValueChange = { viewModel.inputRainfall = it },
                    placeholder = { Text("contoh: 2200") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                )

                // Input Ketinggian
                Text(
                    text = "Ketinggian dari Paras Laut (meter)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = viewModel.inputElevation,
                    onValueChange = { viewModel.inputElevation = it },
                    placeholder = { Text("contoh: 150") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                )

                // Sejarah hasil
                Text(
                    text = "Sasaran Hasil / Sejarah Hasil (kg/ha/tahun)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = viewModel.inputPastYield,
                    onValueChange = { viewModel.inputPastYield = it },
                    placeholder = { Text("contoh: 2100") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 18.dp)
                )

                Button(
                    onClick = { viewModel.processSmartRecommendation() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (viewModel.isRecommending) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Default.Psychology, contentDescription = "Analisis")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Analisis Kesesuaian Klon", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Laporan Output Makmal AI
        if (viewModel.smartRecommendationReport != null) {
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("recommendation_output_card"),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LAPORAN AGRO-PINTAR RISDA",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = "Verified badge",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Paparkan output markdown laporan yang dihuraikan dengan cara kemas
                    Text(
                        text = viewModel.smartRecommendationReport!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val msg = """
                                *LAPORAN CADANGAN KLON SAYA (RUBBERCLONE AI)*
                                
                                Bahagian Rupa Bumi:
                                - Jenis Tanah: ${viewModel.inputSoilType}
                                - Hujan: ${viewModel.inputRainfall} mm
                                - Tinggi: ${viewModel.inputElevation} meter
                                
                                Ulasan Risalah:
                                ${viewModel.smartRecommendationReport?.take(180)}...
                                
                                Dijana secara automatik menerusi modul Inovasi RISDA 2026.
                            """.trimIndent()
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, msg)
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Kongsi Cadangan Ke WhatsApp"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "WhatsApp share")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Kongsi Ringkasan Cadangan", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// 10. CLONE RECOGNITION encyclopedia (DIREKTORI)
// ==========================================
@Composable
fun CloneDirectoryScreen(viewModel: RubberCloneViewModel) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredClones = viewModel.clonesInfo.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.series.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Direktori Klon Hevea",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Kompilasi rasmi baka ulasan Botani RISDA.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = { viewModel.currentScreen = Screen.ProfilPengguna }) {
                Icon(Icons.Outlined.AccountCircle, contentDescription = "User info")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Cari klon (contoh: RRIM 2025, PB)") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredClones) { clone ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = clone.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                contentColor = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = "Tahun ${clone.yearIntroduced}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(6.dp)
                                )
                            }
                        }

                        Text(
                            text = clone.series,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )

                        Text(
                            text = clone.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 14.dp)
                        )

                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                        Spacer(modifier = Modifier.height(10.dp))

                        // Butiran spesifikasi botani
                        Row(modifier = Modifier.padding(bottom = 4.dp)) {
                            Text("Potensi Hasil: ", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Text(clone.yieldPotential, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Row(modifier = Modifier.padding(bottom = 4.dp)) {
                            Text("Baka Imun Penyakit: ", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Text(clone.diseaseResistance, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Row(modifier = Modifier.padding(bottom = 4.dp)) {
                            Text("Pengesyoran Tanah: ", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Text(clone.soilSuitability, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Row(modifier = Modifier.padding(bottom = 4.dp)) {
                            Text("Ketinggian Rupa Bumi: ", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Text("< ${clone.maxElevation}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 11. USER PROFILE SCREEN
// ==========================================
@Composable
fun UserProfileScreen(viewModel: RubberCloneViewModel) {
    val historyList by viewModel.analysesForUser.collectAsStateWithLifecycle()
    val user = viewModel.currentUser ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .padding(top = 24.dp)
                .size(90.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Avatar",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(54.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = user.fullname,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "E-mel: ${user.email}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Badge(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = user.agency,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Profil Stats Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Aktiviti Penyelidikan Lapangan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Jumlah Imbasan berjaya:", style = MaterialTheme.typography.bodyMedium)
                    Text("${historyList.size} kali", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Pendaftaran Sistem:", style = MaterialTheme.typography.bodyMedium)
                    val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(user.registrationDate))
                    Text(dateStr, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ==========================================
// 12. DIGITAL SIJIL LAB REPORT COMPONENT (PDF PREVIEW)
// ==========================================
@Composable
fun PdfViewScreen(viewModel: RubberCloneViewModel, recordId: Int) {
    val context = LocalContext.current
    val historyList by viewModel.analysesForUser.collectAsStateWithLifecycle()
    val record = historyList.find { it.id == recordId }

    if (record == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Laporan tidak dijumpai.")
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
    ) {
        // Pemilih Tajuk / Kembali
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.currentScreen = Screen.SejarahAnalisis },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = "Papar Sijil Digital",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Laporan Sijil Visual (Simulated beautiful physical certificate paper format)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFFCFDFD) // Premium white ivory certificate base
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header RISDA
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = "RISDA Seal",
                    tint = Color(0xFFD4AF37),
                    modifier = Modifier.size(54.dp)
                )

                Text(
                    text = "RISDA MALAYSIA",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF0F3820),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Text(
                    text = "AKREDITASI DIGITAL PENGECAMAN KLON",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF163C24),
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.8.dp)
                        .background(Color(0xFFD4AF37))
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Perincian Sijil
                Text(
                    text = "Dengan ini disahkan bahawa pokok getah Hevea di lapangan telah dianalisis secara analitik pintar melalui RubberClone AI RISDA dan diklasifikasikan sebagai:",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = record.cloneName,
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color(0xFF0F3820),
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Keyakinan Padanan AI: ${String.format("%.1f", record.confidence * 100)}%",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFC5A028),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Grid Butiran
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF0F5F2), RoundedCornerShape(10.dp))
                        .padding(14.dp)
                ) {
                    SijilRowItem(label = "Pegawai Pemeriksa:", value = viewModel.currentUser?.fullname ?: "RISDA User")
                    SijilRowItem(label = "Agensi Risalah:", value = viewModel.currentUser?.agency ?: "RISDA Cawangan")
                    SijilRowItem(label = "Waktu Imbasan:", value = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(record.timestamp)))
                    SijilRowItem(label = "Nilai Koordinat GPS:", value = "${record.latitude}, ${record.longitude}")
                    SijilRowItem(label = "Jenis Tanah:", value = record.soilType)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Cop Mohor
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .border(1.dp, Color.LightGray, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Security, "SEC", tint = Color.LightGray, modifier = Modifier.size(24.dp))
                        }
                        Text("Keselamatan", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .border(1.5.dp, Color(0xFF0F3820), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Eco, "RISDA ECO", tint = Color(0xFF0F3820), modifier = Modifier.size(34.dp))
                        }
                        Text("Mohor RISDA 2026", style = MaterialTheme.typography.labelSmall, color = Color(0xFF0F3820), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // share buttons
        Button(
            onClick = {
                viewModel.generateAndSharePdf(context, record)
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Send, contentDescription = "Share")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Hantar Sijil PDF Ke WhatsApp", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SijilRowItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color(0xFF144D2B))
        Text(text = value, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
    }
}
