package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.BetterCraftViewModel
import com.example.ui.MainUiState
import com.example.ui.components.LoginDialog
import com.example.ui.components.MinecraftBadge
import com.example.ui.components.MinecraftButton
import com.example.ui.components.MinecraftPalette
import com.example.ui.components.MinecraftText
import com.example.ui.screens.AdminUsersScreen
import com.example.ui.screens.FirebaseRemoteScreen
import com.example.ui.screens.MainPatcherScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WebViewScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: BetterCraftViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                BetterCraftApp(viewModel = viewModel)
            }
        }
    }
}

data class NavTabItem(
    val id: Int,
    val title: String,
    val icon: ImageVector,
    val adminOnly: Boolean = false,
    val testTag: String
)

@Composable
fun BetterCraftApp(viewModel: BetterCraftViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.showLoginDialog) {
        LoginDialog(
            onDismiss = { viewModel.showLoginDialog(false) },
            onLoginGoogle = { viewModel.loginWithGoogle() },
            isLoading = uiState.isAuthLoading,
            errorMessage = uiState.authError
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MinecraftPalette.BackgroundDark),
        topBar = {
            MinecraftTopAppBar(
                uiState = uiState,
                onLoginClick = { viewModel.showLoginDialog(true) },
                onLogoutClick = { viewModel.logout() }
            )
        },
        bottomBar = {
            MinecraftBottomNavBar(
                selectedTab = uiState.selectedTab,
                isAdmin = uiState.isAdmin,
                onSelectTab = { viewModel.selectTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MinecraftPalette.BackgroundDark)
                .padding(innerPadding)
        ) {
            when (uiState.selectedTab) {
                0 -> MainPatcherScreen(uiState = uiState, viewModel = viewModel)
                1 -> WebViewScreen(uiState = uiState, viewModel = viewModel)
                2 -> if (uiState.isAdmin) SettingsScreen(uiState = uiState, viewModel = viewModel) else MainPatcherScreen(uiState, viewModel)
                3 -> if (uiState.isAdmin) FirebaseRemoteScreen(uiState = uiState, viewModel = viewModel) else MainPatcherScreen(uiState, viewModel)
                4 -> if (uiState.isAdmin) AdminUsersScreen(uiState = uiState, viewModel = viewModel) else MainPatcherScreen(uiState, viewModel)
                else -> MainPatcherScreen(uiState = uiState, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MinecraftTopAppBar(
    uiState: MainUiState,
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MinecraftPalette.PanelBackground)
            .statusBarsPadding()
            .border(1.5.dp, MinecraftPalette.ButtonBorderBlack)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MinecraftPalette.ButtonGreen)
                    .border(1.5.dp, MinecraftPalette.ButtonGreenHighlight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MinecraftText(
                        text = "BETTERCRAFT",
                        fontSize = 14,
                        fontWeight = FontWeight.ExtraBold,
                        color = MinecraftPalette.GoldYellow
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    MinecraftBadge(text = "v5.17", color = MinecraftPalette.DiamondBlue)
                }
                MinecraftText(
                    text = if (uiState.isAdmin) "Admin: ${uiState.currentUser?.email?.substringBefore('@')}" else "Modo Visitante",
                    fontSize = 10,
                    color = if (uiState.isAdmin) MinecraftPalette.EmeraldGreen else Color.LightGray
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Auth Button / Profile
            if (uiState.currentUser == null) {
                MinecraftButton(
                    text = "LOGIN",
                    onClick = onLoginClick,
                    isActionGreen = true,
                    icon = Icons.Default.Login,
                    modifier = Modifier.height(34.dp),
                    testTag = "btn_top_login"
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MinecraftBadge(
                        text = if (uiState.isSuperAdmin) "OWNER" else "ADMIN",
                        color = if (uiState.isSuperAdmin) MinecraftPalette.GoldYellow else MinecraftPalette.EmeraldGreen
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onLogoutClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Desconectar",
                            tint = MinecraftPalette.RedstoneRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MinecraftBottomNavBar(
    selectedTab: Int,
    isAdmin: Boolean,
    onSelectTab: (Int) -> Unit
) {
    val allTabs = listOf(
        NavTabItem(0, "MONTAR", Icons.Default.Build, adminOnly = false, testTag = "tab_patcher"),
        NavTabItem(1, "SITE WEB", Icons.Default.Language, adminOnly = false, testTag = "tab_web"),
        NavTabItem(2, "CONFIG", Icons.Default.Settings, adminOnly = true, testTag = "tab_config"),
        NavTabItem(3, "FIREBASE", Icons.Default.Cloud, adminOnly = true, testTag = "tab_firebase"),
        NavTabItem(4, "ADMINS", Icons.Default.AdminPanelSettings, adminOnly = true, testTag = "tab_admins")
    )

    // Filter tabs based on admin status: hide Settings, Firebase, and Admins for non-admins!
    val visibleTabs = allTabs.filter { !it.adminOnly || isAdmin }
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MinecraftPalette.PanelBackground)
            .navigationBarsPadding()
            .border(1.5.dp, MinecraftPalette.ButtonBorderBlack)
            .padding(horizontal = 4.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            visibleTabs.forEach { tab ->
                val isSelected = selectedTab == tab.id
                val tabWidth = if (visibleTabs.size <= 2) Modifier.weight(1f) else Modifier.width(76.dp)

                MinecraftTabButton(
                    title = tab.title,
                    icon = tab.icon,
                    isSelected = isSelected,
                    onClick = { onSelectTab(tab.id) },
                    modifier = tabWidth,
                    testTag = tab.testTag
                )
            }
        }
    }
}

@Composable
fun MinecraftTabButton(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "tab_btn"
) {
    val bgColor = if (isSelected) MinecraftPalette.ButtonGreen else MinecraftPalette.ButtonStone
    val borderColor = if (isSelected) MinecraftPalette.EmeraldGreen else MinecraftPalette.ButtonStoneHighlight

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 50.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(MinecraftPalette.ButtonBorderBlack)
            .padding(1.dp)
            .border(1.dp, borderColor)
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 2.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) Color.White else Color(0xFFC0C0C0),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            MinecraftText(
                text = title,
                fontSize = 9,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else Color(0xFFA0A0A0)
            )
        }
    }
}
