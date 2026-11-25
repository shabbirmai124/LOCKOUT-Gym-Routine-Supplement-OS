package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.VaultCategory
import com.example.data.VaultItemEntity
import com.example.ui.components.ObsidianCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusPill
import com.example.ui.theme.LockoutColors
import com.example.viewmodel.LockoutUiState

@Composable
fun VaultScreen(
    state: LockoutUiState,
    onToggleGearBag: (VaultItemEntity) -> Unit,
    onAddSupplementToStack: (VaultItemEntity) -> Unit
) {
    val palette = LockoutColors.current
    val haptics = LocalHapticFeedback.current
    var selectedCategory by remember { mutableStateOf(VaultCategory.INSTRUMENT) }
    var searchQuery by remember { mutableStateOf("") }
    var expandedItemId by remember { mutableStateOf<Int?>(null) }

    val totalAccessories = remember(state.vaultItems) {
        state.vaultItems.count { it.category == VaultCategory.ACCESSORY }
    }
    val ownedAccessories = remember(state.vaultItems) {
        state.vaultItems.count { it.category == VaultCategory.ACCESSORY && it.isInMyGearBag }
    }

    val filteredItems = remember(state.vaultItems, selectedCategory, searchQuery) {
        val q = searchQuery.trim().lowercase()
        state.vaultItems.filter { item ->
            val matchesCat = if (q.isEmpty()) item.category == selectedCategory else true
            val matchesQuery = q.isEmpty() ||
                item.name.lowercase().contains(q) ||
                item.subTag.lowercase().contains(q) ||
                item.primaryBenefit.lowercase().contains(q) ||
                item.howToUseOrSetup.lowercase().contains(q)
            matchesCat && matchesQuery
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                eyebrow = "OFFLINE ENCYCLOPEDIA • INSTRUMENTS • GEAR • SUPPS • APPAREL",
                title = stringResource(R.string.label_gym_vault),
                trailing = {
                    StatusPill(
                        text = "$ownedAccessories/$totalAccessories IN BAG",
                        color = palette.emerald
                    )
                }
            )
        }

        // Photorealistic Powerlifting Gear Flat-Lay Hero Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(114.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, palette.border, RoundedCornerShape(16.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_vault_gear_hero_1795606979378),
                    contentDescription = stringResource(R.string.cd_vault_hero),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0x440B0D11), Color(0xDC0B0D11))
                            )
                        )
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "OFFLINE EQUIPMENT & GEAR SPECS",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.cyan
                        )
                        Text(
                            text = "${state.vaultItems.size} Pro Guides Ready Offline",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFF8FAFC)
                        )
                    }
                    StatusPill(
                        text = "$ownedAccessories PACKED",
                        color = palette.emerald
                    )
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vault_search_input"),
                placeholder = { Text("Search machines, belts, creatine, squat shoes...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.cd_search_vault),
                        tint = palette.volt
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.cd_clear_search),
                                tint = palette.textSecondary
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = palette.volt,
                    unfocusedBorderColor = palette.border,
                    focusedContainerColor = palette.surface1,
                    unfocusedContainerColor = palette.surface1
                )
            )
        }

        // 4 Category Selector Pills
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VaultCategory.entries.forEach { category ->
                    val selected = selectedCategory == category && searchQuery.isBlank()
                    Surface(
                        onClick = {
                            searchQuery = ""
                            selectedCategory = category
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("vault_tab_${category.name.lowercase()}"),
                        shape = RoundedCornerShape(12.dp),
                        color = if (selected) palette.volt else palette.surface1,
                        border = BorderStroke(1.dp, if (selected) palette.volt else palette.border)
                    ) {
                        Text(
                            text = category.shortLabel,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) palette.onVolt else palette.textSecondary,
                            modifier = Modifier.padding(vertical = 11.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }

        // Contextual Gear Bag Hint when viewing Accessories
        if (selectedCategory == VaultCategory.ACCESSORY && searchQuery.isBlank()) {
            item {
                ObsidianCard(
                    accentBorderColor = palette.emerald.copy(alpha = 0.5f),
                    elevatedSurface = true
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Backpack, contentDescription = null, tint = palette.emerald)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("MY SMART GYM BAG ($ownedAccessories / $totalAccessories PACKED)", style = MaterialTheme.typography.labelLarge, color = palette.emerald)
                            Text(
                                text = "Check off gear you own. LOCKOUT automatically syncs your bag to heavy working sets in Training.",
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.textSecondary
                            )
                        }
                    }
                }
            }
        }

        // Vault Item Cards
        items(filteredItems, key = { it.id }) { item ->
            val isExpanded = expandedItemId == item.id
            val tagColor = when {
                item.subTag.contains("Tier S") -> palette.volt
                item.subTag.contains("Tier A") -> palette.cyan
                item.subTag.contains("Tier F") -> palette.crimson
                item.category == VaultCategory.ACCESSORY && item.isInMyGearBag -> palette.emerald
                else -> palette.amber
            }

            ObsidianCard(
                accentBorderColor = if (item.category == VaultCategory.ACCESSORY && item.isInMyGearBag) {
                    palette.emerald.copy(alpha = 0.55f)
                } else null,
                onClick = { expandedItemId = if (isExpanded) null else item.id },
                modifier = Modifier.testTag("vault_card_${item.id}")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        StatusPill(text = item.subTag.uppercase(), color = tagColor)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = palette.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.primaryBenefit,
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textSecondary
                        )
                    }
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = palette.textSecondary
                    )
                }

                // Expanded 30-Second Setup / Clinical Spec / Mistake / Pro Rule
                AnimatedVisibility(visible = isExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GuideDetailBlock(
                            label = when (item.category) {
                                VaultCategory.INSTRUMENT -> "30-SECOND SETUP CUE"
                                VaultCategory.ACCESSORY -> "WHEN & HOW TO USE"
                                VaultCategory.SUPPLEMENT -> "CLINICAL DOSE & TIMING"
                                VaultCategory.APPAREL -> "MECHANICAL & FABRIC SPEC"
                            },
                            body = item.howToUseOrSetup,
                            accent = palette.cyan
                        )
                        GuideDetailBlock(
                            label = "COMMON MISTAKE FIXED BY LOCKOUT",
                            body = item.commonMistake,
                            accent = palette.crimson,
                            isWarning = true
                        )
                        GuideDetailBlock(
                            label = "PRO RULE",
                            body = item.proRecommendation,
                            accent = palette.volt
                        )
                    }
                }

                // Direct Action Footer for Accessories (My Gear Bag) and Supplements (Add to Stack)
                if (item.category == VaultCategory.ACCESSORY) {
                    Spacer(modifier = Modifier.height(10.dp))
                    if (item.isInMyGearBag) {
                        OutlinedButton(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onToggleGearBag(item)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("gear_bag_toggle_${item.id}"),
                            border = BorderStroke(1.dp, palette.emerald),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = palette.emerald, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("PACKED IN MY GEAR BAG", color = palette.emerald, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onToggleGearBag(item)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("gear_bag_toggle_${item.id}"),
                            colors = ButtonDefaults.buttonColors(containerColor = palette.surface2),
                            border = BorderStroke(1.dp, palette.border),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = palette.textPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ADD TO MY GEAR BAG", color = palette.textPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                } else if (item.category == VaultCategory.SUPPLEMENT && !item.subTag.contains("Tier F")) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onAddSupplementToStack(item)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("vault_add_supp_${item.id}"),
                        border = BorderStroke(1.dp, palette.volt.copy(alpha = 0.7f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = palette.volt, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ADD TO MY DAILY STACK", color = palette.volt, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun GuideDetailBlock(
    label: String,
    body: String,
    accent: androidx.compose.ui.graphics.Color,
    isWarning: Boolean = false
) {
    val palette = LockoutColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(palette.canvas)
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isWarning) Icons.Default.Warning else Icons.Default.Verified,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = accent)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(body, style = MaterialTheme.typography.bodySmall, color = palette.textPrimary)
    }
}
