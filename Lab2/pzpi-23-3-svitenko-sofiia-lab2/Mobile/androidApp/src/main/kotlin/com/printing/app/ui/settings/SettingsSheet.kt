package com.printing.app.ui.settings

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.printing.app.R
import com.printing.app.util.LocaleHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    onDismiss: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val currentLang = LocaleHelper.getSaved(context)

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = stringResource(R.string.settings),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            // Language
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.language),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LangButton(
                        label = "English",
                        selected = currentLang == LocaleHelper.LANG_EN,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (currentLang != LocaleHelper.LANG_EN) {
                                LocaleHelper.save(context, LocaleHelper.LANG_EN)
                                (context as? Activity)?.recreate()
                            }
                        }
                    )
                    LangButton(
                        label = "Українська",
                        selected = currentLang == LocaleHelper.LANG_UK,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (currentLang != LocaleHelper.LANG_UK) {
                                LocaleHelper.save(context, LocaleHelper.LANG_UK)
                                (context as? Activity)?.recreate()
                            }
                        }
                    )
                }
            }

            HorizontalDivider()

            // Logout
            OutlinedButton(
                onClick = {
                    onDismiss()
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.logout))
            }
        }
    }
}

@Composable
private fun LangButton(
    label: String,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    if (selected) {
        Button(onClick = onClick, modifier = modifier) { Text(label) }
    } else {
        OutlinedButton(onClick = onClick, modifier = modifier) { Text(label) }
    }
}
