package com.shajikhan.ladspa.amprack.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shajikhan.ladspa.amprack.ui.screens.*
import com.shajikhan.ladspa.amprack.ui.theme.AmpRackTheme

enum class AmpRackScreen(val label: String, val icon: ImageVector) {
    RACK("Rack", Icons.Default.GraphicEq),
    DRUMS("Drums", Icons.Default.Radio),
    TUNER("Tuner", Icons.Default.Tune),
    RECORDER("Record", Icons.Default.Mic),
    GUIDE("Guide", Icons.Default.Cable),
    SETTINGS("Settings", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmpRackApp(
    viewModel: AmpRackViewModel,
    modifier: Modifier = Modifier
) {
    val currentTheme by viewModel.currentTheme.collectAsStateWithLifecycle()
    var currentScreen by remember { mutableStateOf(AmpRackScreen.RACK) }

    AmpRackTheme(selectedTheme = currentTheme) {
        Scaffold(
            modifier = modifier.testTag("app_scaffold"),
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Amp Rack",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    text = "v5.95",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                currentScreen = if (currentScreen == AmpRackScreen.SETTINGS) AmpRackScreen.RACK else AmpRackScreen.SETTINGS
                            },
                            modifier = Modifier.testTag("top_bar_settings_btn")
                        ) {
                            Icon(
                                if (currentScreen == AmpRackScreen.SETTINGS) Icons.Default.Close else Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = if (currentScreen == AmpRackScreen.SETTINGS) MaterialTheme.colorScheme.primary else Color.LightGray
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF14151C)
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color(0xFF14151C),
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    val navItems = listOf(
                        AmpRackScreen.RACK,
                        AmpRackScreen.DRUMS,
                        AmpRackScreen.TUNER,
                        AmpRackScreen.RECORDER,
                        AmpRackScreen.GUIDE
                    )

                    navItems.forEach { screen ->
                        val isSelected = currentScreen == screen
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentScreen = screen },
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.label
                                )
                            },
                            label = { Text(screen.label, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            ),
                            modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (currentScreen) {
                    AmpRackScreen.RACK -> RackScreen(
                        viewModel = viewModel,
                        onNavigateToTuner = { currentScreen = AmpRackScreen.TUNER }
                    )
                    AmpRackScreen.DRUMS -> DrumMachineScreen(viewModel = viewModel)
                    AmpRackScreen.TUNER -> TunerScreen(viewModel = viewModel)
                    AmpRackScreen.RECORDER -> RecorderScreen(viewModel = viewModel)
                    AmpRackScreen.GUIDE -> ConnectGuideScreen(viewModel = viewModel)
                    AmpRackScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
