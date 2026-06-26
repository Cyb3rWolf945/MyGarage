package pt.ipt.dama2026.mygarage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pt.ipt.dama2026.mygarage.R
import pt.ipt.dama2026.mygarage.ui.theme.MyGarageColors

/**
 * Terms & Conditions screen — required by Google Play Store policy.
 * Displays the app's terms in a scrollable, premium-styled layout.
 */
@Composable
fun TermsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MyGarageColors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp, bottom = 48.dp)
        ) {
            // ── Back Row ───────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onBackClick() }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = MyGarageColors.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.terms_back),
                    style = MaterialTheme.typography.labelSmall,
                    color = MyGarageColors.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Title ──────────────────────────────────────────────────
            Text(
                text = stringResource(R.string.terms_title),
                style = MaterialTheme.typography.headlineLarge,
                color = MyGarageColors.onBackground,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.terms_last_updated),
                style = MaterialTheme.typography.labelSmall,
                color = MyGarageColors.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            HorizontalDivider(color = MyGarageColors.outlineVariant)
            Spacer(modifier = Modifier.height(24.dp))

            // ── Sections ───────────────────────────────────────────────

            // 1. Acceptance
            TermsSection(
                title = stringResource(R.string.terms_section1_title),
                content = stringResource(R.string.terms_section1_content)
            )

            // 2. Account
            TermsSection(
                title = stringResource(R.string.terms_section2_title),
                content = stringResource(R.string.terms_section2_content)
            )

            // 3. Data & Privacy
            TermsSection(
                title = stringResource(R.string.terms_section3_title),
                content = stringResource(R.string.terms_section3_content)
            )

            // 4. Intellectual Property
            TermsSection(
                title = stringResource(R.string.terms_section4_title),
                content = stringResource(R.string.terms_section4_content)
            )

            // 5. Limitation of Liability
            TermsSection(
                title = stringResource(R.string.terms_section5_title),
                content = stringResource(R.string.terms_section5_content)
            )

            // 6. Termination
            TermsSection(
                title = stringResource(R.string.terms_section6_title),
                content = stringResource(R.string.terms_section6_content)
            )

            // 7. Changes
            TermsSection(
                title = stringResource(R.string.terms_section7_title),
                content = stringResource(R.string.terms_section7_content)
            )

            // 8. Contact
            TermsSection(
                title = stringResource(R.string.terms_section8_title),
                content = stringResource(R.string.terms_section8_content)
            )
        }
    }
}

@Composable
private fun TermsSection(title: String, content: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineLarge,
        color = MyGarageColors.onBackground,
        fontWeight = FontWeight.SemiBold
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = content,
        style = MaterialTheme.typography.bodyMedium,
        color = MyGarageColors.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(24.dp))
}
