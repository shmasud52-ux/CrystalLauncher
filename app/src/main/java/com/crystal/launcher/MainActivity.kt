package com.crystal.launcher

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class LauncherApp(
    val label: String,
    val packageName: String,
    val icon: android.graphics.drawable.Drawable
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { CrystalLauncherApp() }
    }
}

@Composable
private fun CrystalLauncherApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val packageManager = context.packageManager
    var darkMode by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }
    var showDrawer by remember { mutableStateOf(false) }

    val installedApps = remember {
        packageManager.queryIntentActivities(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),
            PackageManager.MATCH_ALL
        ).mapNotNull { resolveInfo ->
            val activityInfo = resolveInfo.activityInfo ?: return@mapNotNull null
            LauncherApp(
                label = resolveInfo.loadLabel(packageManager).toString(),
                packageName = activityInfo.packageName,
                icon = resolveInfo.loadIcon(packageManager)
            )
        }.distinctBy { it.packageName }.sortedBy { it.label.lowercase(Locale.getDefault()) }
    }

    val textColor = if (darkMode) Color(0xFFF4F7FC) else Color(0xFF172B3B)
    val mutedColor = if (darkMode) Color(0xFF9BAFC0) else Color(0xFF687D8D)
    val glass = if (darkMode) Color(0xFF1D2B3A).copy(alpha = 0.78f)
                else Color.White.copy(alpha = 0.64f)
    val accent = Color(0xFF63D5CB)
    val background = if (darkMode) {
        listOf(Color(0xFF0B1420), Color(0xFF172B3A), Color(0xFF101A29))
    } else {
        listOf(Color(0xFFD5F0EE), Color(0xFFE9EDF7), Color(0xFFDCEAF2))
    }

    MaterialTheme(
        colorScheme = if (darkMode) darkColorScheme(primary = accent)
                      else lightColorScheme(primary = Color(0xFF159E98))
    ) {
        Box(
            Modifier.fillMaxSize().background(Brush.linearGradient(background))
        ) {
            Column(
                Modifier.fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(top = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date()),
                            color = mutedColor,
                            fontSize = 13.sp
                        )
                        Text(
                            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                            color = textColor,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Light
                        )
                    }
                    GlassIcon(Icons.Rounded.DarkMode, glass, textColor) {
                        darkMode = !darkMode
                    }
                }

                Spacer(Modifier.height(22.dp))

                GlassPanel(glass, Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Your space. Your flow.",
                                color = textColor,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "A calmer way to launch.",
                                color = mutedColor,
                                fontSize = 13.sp
                            )
                        }
                        Icon(
                            Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (showDrawer) "All apps" else "Your apps",
                        color = textColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    Text("${installedApps.size} apps", color = mutedColor, fontSize = 12.sp)
                }
                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search apps", color = mutedColor) },
                    leadingIcon = { Icon(Icons.Rounded.Search, null, tint = mutedColor) },
                    trailingIcon = {
                        if (search.isNotEmpty()) {
                            IconButton(onClick = { search = "" }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(22.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = glass,
                        focusedContainerColor = glass,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
                        focusedBorderColor = accent,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor
                    )
                )

                Spacer(Modifier.height(12.dp))
                val filteredApps = installedApps.filter { it.label.contains(search, ignoreCase = true) }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredApps, key = { it.packageName }) { app ->
                        Column(
                            Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .clickable {
                                    packageManager.getLaunchIntentForPackage(app.packageName)
                                        ?.let(context::startActivity)
                                }
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                bitmap = remember(app.packageName) { app.icon.toBitmapImage() },
                                contentDescription = app.label,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                app.label,
                                color = textColor,
                                fontSize = 11.sp,
                                maxLines = 1,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                GlassPanel(glass, Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DockItem(Icons.Rounded.Home, "Home", !showDrawer, accent, textColor) {
                            showDrawer = false
                            search = ""
                        }
                        DockItem(Icons.Rounded.GridView, "Apps", showDrawer, accent, textColor) {
                            showDrawer = !showDrawer
                            search = ""
                        }
                        DockItem(Icons.Rounded.Palette, "Theme", false, accent, textColor) {
                            darkMode = !darkMode
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun GlassPanel(
    color: Color,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier.clip(RoundedCornerShape(26.dp))
            .background(color)
            .padding(2.dp),
        content = content
    )
}

@Composable
private fun GlassIcon(
    icon: ImageVector,
    panel: Color,
    tint: Color,
    onClick: () -> Unit
) {
    Box(
        Modifier.size(48.dp).clip(CircleShape)
            .background(panel).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint)
    }
}

@Composable
private fun DockItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    accent: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Column(
        Modifier.clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (selected) accent else textColor,
            modifier = Modifier.size(23.dp)
        )
        Text(label, color = if (selected) accent else textColor, fontSize = 10.sp)
    }
}

private fun android.graphics.drawable.Drawable.toBitmapImage(): ImageBitmap {
    val bitmap = android.graphics.Bitmap.createBitmap(
        intrinsicWidth.coerceAtLeast(1),
        intrinsicHeight.coerceAtLeast(1),
        android.graphics.Bitmap.Config.ARGB_8888
    )
    val canvas = android.graphics.Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap.asImageBitmap()
}
