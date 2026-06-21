package ipt.pt.mygarage.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ipt.pt.mygarage.R
import ipt.pt.mygarage.presentation.onboarding.OnboardingUiState
import ipt.pt.mygarage.presentation.onboarding.OnboardingViewModel
import ipt.pt.mygarage.ui.theme.MyGarageColors
import kotlinx.coroutines.launch

// ── Page keys ────────────────────────────────────────────────────────────────
private const val PAGE_WELCOME = 0
private const val PAGE_AUTH_FORK = 1
private const val PAGE_SETUP = 2
private const val PAGE_COUNT = 3

/**
 * 3-step onboarding flow with authentication decision gate.
 *
 * Page 1 — Cinematic welcome with bold typography.
 * Page 2 — Auth fork: sign in to sync, or continue as guest.
 * Page 3 — Guest workspace setup (user + garage name).
 *
 * Manual swiping is disabled — users must use the provided buttons.
 *
 * @param viewModel The [OnboardingViewModel] managing form state and navigation events.
 * @param onOnboardingComplete Callback invoked after successful guest setup.
 * @param onNavigateToAuth Callback invoked when the user chooses to sign in.
 */
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onOnboardingComplete: () -> Unit,
    onNavigateToAuth: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val onboardingCompleted by viewModel.onboardingCompleted.collectAsStateWithLifecycle()
    val navigateToAuth by viewModel.navigateToAuth.collectAsStateWithLifecycle()
    val advanceToSetupPage by viewModel.advanceToSetupPage.collectAsStateWithLifecycle()

    // ── Side-effect observers ────────────────────────────────────────────

    LaunchedEffect(onboardingCompleted) {
        if (onboardingCompleted) {
            onOnboardingComplete()
        }
    }

    LaunchedEffect(navigateToAuth) {
        if (navigateToAuth) {
            onNavigateToAuth()
        }
    }

    val pagerState = rememberPagerState(pageCount = { PAGE_COUNT })
    val coroutineScope = rememberCoroutineScope()

    // Programmatic scroll to setup page when "Continue as Guest" is selected
    LaunchedEffect(advanceToSetupPage) {
        if (advanceToSetupPage) {
            pagerState.animateScrollToPage(PAGE_SETUP)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MyGarageColors.background)
    ) {
        // ── Pager ────────────────────────────────────────────────────────
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { page ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(400)),
                exit = fadeOut(animationSpec = tween(200))
            ) {
                when (page) {
                    PAGE_WELCOME -> WelcomePage(
                        onNext = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(PAGE_AUTH_FORK)
                            }
                        }
                    )
                    PAGE_AUTH_FORK -> AuthForkPage(
                        onSignIn = viewModel::onSignInClicked,
                        onContinueAsGuest = viewModel::onContinueAsGuest
                    )
                    PAGE_SETUP -> SetupPage(
                        uiState = uiState,
                        onUserNameChanged = viewModel::onUserNameChanged,
                        onGarageNameChanged = viewModel::onGarageNameChanged,
                        onFinish = viewModel::onFinishClicked
                    )
                }
            }
        }

        // ── Dot indicator ────────────────────────────────────────────────
        PageIndicator(
            pageCount = PAGE_COUNT,
            currentPage = pagerState.currentPage,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
    }
}

// ── PAGE 1: CINEMATIC WELCOME ────────────────────────────────────────────────

@Composable
private fun WelcomePage(
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Decorative Racing Blue accent strip
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MyGarageColors.primary,
                            MyGarageColors.primaryContainer
                        )
                    )
                )
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(id = R.string.onboarding_welcome_title),
            style = MaterialTheme.typography.displayLarge,
            color = MyGarageColors.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(id = R.string.onboarding_welcome_subtitle),
            style = MaterialTheme.typography.headlineLarge,
            color = MyGarageColors.primary,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(id = R.string.onboarding_welcome_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MyGarageColors.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(56.dp))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MyGarageColors.primary
            )
        ) {
            Text(
                text = stringResource(id = R.string.onboarding_action_next),
                style = MaterialTheme.typography.labelSmall,
                color = MyGarageColors.surfaceContainerLowest
            )
        }
    }
}

// ── PAGE 2: AUTH FORK ────────────────────────────────────────────────────────

@Composable
private fun AuthForkPage(
    onSignIn: () -> Unit,
    onContinueAsGuest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ═══ Headline ═══
        Text(
            text = stringResource(id = R.string.onboarding_authfork_title),
            style = MaterialTheme.typography.displayLarge,
            color = MyGarageColors.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ═══ Subtitle ═══
        Text(
            text = stringResource(id = R.string.onboarding_authfork_subtitle),
            style = MaterialTheme.typography.headlineLarge,
            color = MyGarageColors.primary,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        // ═══ Primary: Sign In / Create Account ═══
        Button(
            onClick = onSignIn,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MyGarageColors.primary
            )
        ) {
            Text(
                text = stringResource(id = R.string.onboarding_authfork_sign_in),
                style = MaterialTheme.typography.labelSmall,
                color = MyGarageColors.surfaceContainerLowest
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ═══ Secondary: Continue as Guest ═══
        OutlinedButton(
            onClick = onContinueAsGuest,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MyGarageColors.onSurfaceVariant
            ),
            border = BorderStroke(
                width = 1.dp,
                color = MyGarageColors.outlineVariant.copy(alpha = 0.3f)
            )
        ) {
            Text(
                text = stringResource(id = R.string.onboarding_authfork_continue_guest),
                style = MaterialTheme.typography.labelSmall,
                color = MyGarageColors.onSurfaceVariant
            )
        }
    }
}

// ── PAGE 3: GUEST SETUP FORM ─────────────────────────────────────────────────

@Composable
private fun SetupPage(
    uiState: OnboardingUiState,
    onUserNameChanged: (String) -> Unit,
    onGarageNameChanged: (String) -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.onboarding_setup_title),
            style = MaterialTheme.typography.displayLarge,
            color = MyGarageColors.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(id = R.string.onboarding_setup_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MyGarageColors.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ── User Name ────────────────────────────────────────────────────
        OutlinedTextField(
            value = uiState.userName,
            onValueChange = onUserNameChanged,
            placeholder = {
                Text(stringResource(id = R.string.onboarding_setup_user_name_placeholder))
            },
            isError = uiState.formErrors.containsKey(OnboardingViewModel.FIELD_USER_NAME),
            supportingText = uiState.formErrors[OnboardingViewModel.FIELD_USER_NAME]?.let { resId ->
                { Text(text = stringResource(id = resId)) }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MyGarageColors.surfaceContainerLow,
                unfocusedContainerColor = MyGarageColors.surfaceContainerLow,
                errorContainerColor = MyGarageColors.surfaceContainerHighest,
                focusedBorderColor = MyGarageColors.primary,
                unfocusedBorderColor = MyGarageColors.outlineVariant.copy(alpha = 0.15f),
                errorBorderColor = MyGarageColors.error,
                errorSupportingTextColor = MyGarageColors.error
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Garage Name ──────────────────────────────────────────────────
        OutlinedTextField(
            value = uiState.garageName,
            onValueChange = onGarageNameChanged,
            placeholder = {
                Text(stringResource(id = R.string.onboarding_setup_garage_name_placeholder))
            },
            isError = uiState.formErrors.containsKey(OnboardingViewModel.FIELD_GARAGE_NAME),
            supportingText = uiState.formErrors[OnboardingViewModel.FIELD_GARAGE_NAME]?.let { resId ->
                { Text(text = stringResource(id = resId)) }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MyGarageColors.surfaceContainerLow,
                unfocusedContainerColor = MyGarageColors.surfaceContainerLow,
                errorContainerColor = MyGarageColors.surfaceContainerHighest,
                focusedBorderColor = MyGarageColors.primary,
                unfocusedBorderColor = MyGarageColors.outlineVariant.copy(alpha = 0.15f),
                errorBorderColor = MyGarageColors.error,
                errorSupportingTextColor = MyGarageColors.error
            )
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MyGarageColors.primary
            )
        ) {
            Text(
                text = stringResource(id = R.string.onboarding_action_finish),
                style = MaterialTheme.typography.labelSmall,
                color = MyGarageColors.surfaceContainerLowest
            )
        }
    }
}

// ── DOT PAGE INDICATOR ───────────────────────────────────────────────────────

@Composable
private fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val isActive = index == currentPage

            val size by animateDpAsState(
                targetValue = if (isActive) 10.dp else 6.dp,
                animationSpec = tween(durationMillis = 300),
                label = "dotSize"
            )

            val alpha by androidx.compose.animation.core.animateFloatAsState(
                targetValue = if (isActive) 1f else 0.4f,
                animationSpec = tween(durationMillis = 300),
                label = "dotAlpha"
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(size)
                    .clip(CircleShape)
                    .background(
                        if (isActive) MyGarageColors.primary
                        else MyGarageColors.onSurfaceVariant.copy(alpha = alpha)
                    )
            )
        }
    }
}
