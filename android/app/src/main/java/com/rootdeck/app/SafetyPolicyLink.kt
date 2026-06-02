package com.rootdeck.app

import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/** Single source of truth for the policy URL. Mirror of policy.ts → POLICY_URL. */
const val POLICY_URL = "https://rootdeck.app/#policy"

@Composable
fun SafetyPolicyLink(label: String = "View safety policy") {
    val context = LocalContext.current
    OutlinedButton(onClick = {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(POLICY_URL))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }) {
        Icon(Icons.Outlined.OpenInNew, contentDescription = null)
        Text(label)
    }
}
