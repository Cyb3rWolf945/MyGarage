package pt.ipt.dama2026.mygarage.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import pt.ipt.dama2026.mygarage.R
import pt.ipt.dama2026.mygarage.presentation.auth.AuthViewModel
import pt.ipt.dama2026.mygarage.ui.theme.MyGarageColors

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val viewModel: AuthViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val authSuccess by viewModel.authSuccess.collectAsStateWithLifecycle()

    LaunchedEffect(authSuccess) {
        if (authSuccess) {
            viewModel.clearAuthSuccess()
            onAuthSuccess()
        }
    }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MyGarageColors.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(64.dp))

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
            text = if (state.isLogin)
                stringResource(id = R.string.auth_title_sign_in)
            else
                stringResource(id = R.string.auth_title_create_account),
            style = MaterialTheme.typography.displayLarge,
            color = MyGarageColors.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        AnimatedVisibility(visible = state.errorMessage != null) {
            state.errorMessage?.let { message ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MyGarageColors.error.copy(alpha = 0.1f))
                        .padding(16.dp)
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MyGarageColors.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        AnimatedVisibility(visible = !state.isLogin) {
            Column {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = viewModel::onNameChanged,
                    placeholder = { Text(stringResource(id = R.string.auth_name_placeholder)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MyGarageColors.surfaceContainerLow,
                        unfocusedContainerColor = MyGarageColors.surfaceContainerLow,
                        focusedBorderColor = MyGarageColors.primary,
                        unfocusedBorderColor = MyGarageColors.outlineVariant.copy(alpha = 0.15f)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.garageName,
                    onValueChange = viewModel::onGarageNameChanged,
                    placeholder = { Text(stringResource(id = R.string.auth_garage_name_placeholder)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MyGarageColors.surfaceContainerLow,
                        unfocusedContainerColor = MyGarageColors.surfaceContainerLow,
                        focusedBorderColor = MyGarageColors.primary,
                        unfocusedBorderColor = MyGarageColors.outlineVariant.copy(alpha = 0.15f)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        OutlinedTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChanged,
            placeholder = { Text(stringResource(id = R.string.auth_email_placeholder)) },
            isError = state.formErrors.containsKey(AuthViewModel.FIELD_EMAIL),
            supportingText = state.formErrors[AuthViewModel.FIELD_EMAIL]?.let { resId ->
                { Text(text = stringResource(id = resId)) }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MyGarageColors.surfaceContainerLow,
                unfocusedContainerColor = MyGarageColors.surfaceContainerLow,
                errorContainerColor = MyGarageColors.surfaceContainerHighest,
                focusedBorderColor = MyGarageColors.primary,
                unfocusedBorderColor = MyGarageColors.outlineVariant.copy(alpha = 0.15f),
                errorBorderColor = MyGarageColors.error
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.password,
            onValueChange = viewModel::onPasswordChanged,
            placeholder = { Text(stringResource(id = R.string.auth_password_placeholder)) },
            isError = state.formErrors.containsKey(AuthViewModel.FIELD_PASSWORD),
            supportingText = state.formErrors[AuthViewModel.FIELD_PASSWORD]?.let { resId ->
                { Text(text = stringResource(id = resId)) }
            },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
            trailingIcon = {
                TextButton(onClick = { passwordVisible = !passwordVisible }) {
                    Text(
                        text = if (passwordVisible) stringResource(R.string.auth_password_hide)
                            else stringResource(R.string.auth_password_show),
                        style = MaterialTheme.typography.labelSmall,
                        color = MyGarageColors.primary
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = if (state.isLogin) ImeAction.Done else ImeAction.Next
            ),
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MyGarageColors.surfaceContainerLow,
                unfocusedContainerColor = MyGarageColors.surfaceContainerLow,
                errorContainerColor = MyGarageColors.surfaceContainerHighest,
                focusedBorderColor = MyGarageColors.primary,
                unfocusedBorderColor = MyGarageColors.outlineVariant.copy(alpha = 0.15f),
                errorBorderColor = MyGarageColors.error
            )
        )

        AnimatedVisibility(visible = !state.isLogin) {
            Column {
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.confirmPassword,
                    onValueChange = viewModel::onConfirmPasswordChanged,
                    placeholder = { Text(stringResource(id = R.string.auth_confirm_password_placeholder)) },
                    isError = state.formErrors.containsKey(AuthViewModel.FIELD_CONFIRM_PASSWORD),
                    supportingText = state.formErrors[AuthViewModel.FIELD_CONFIRM_PASSWORD]?.let { resId ->
                        { Text(text = stringResource(id = resId)) }
                    },
                    singleLine = true,
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                    trailingIcon = {
                        TextButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Text(
                                text = if (confirmPasswordVisible) stringResource(R.string.auth_password_hide)
                                    else stringResource(R.string.auth_password_show),
                                style = MaterialTheme.typography.labelSmall,
                                color = MyGarageColors.primary
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MyGarageColors.surfaceContainerLow,
                        unfocusedContainerColor = MyGarageColors.surfaceContainerLow,
                        errorContainerColor = MyGarageColors.surfaceContainerHighest,
                        focusedBorderColor = MyGarageColors.primary,
                        unfocusedBorderColor = MyGarageColors.outlineVariant.copy(alpha = 0.15f),
                        errorBorderColor = MyGarageColors.error
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = viewModel::onSubmit,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MyGarageColors.primary,
                disabledContainerColor = MyGarageColors.primary.copy(alpha = 0.4f)
            ),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 24.dp,
                vertical = 18.dp
            )
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    color = MyGarageColors.surfaceContainerLowest,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(
                text = if (state.isLogin)
                    stringResource(id = R.string.auth_action_sign_in)
                else
                    stringResource(id = R.string.auth_action_create_account),
                style = MaterialTheme.typography.labelSmall,
                color = MyGarageColors.surfaceContainerLowest
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = viewModel::onToggleMode,
            enabled = !state.isLoading
        ) {
            Text(
                text = if (state.isLogin)
                    stringResource(id = R.string.auth_toggle_to_register)
                else
                    stringResource(id = R.string.auth_toggle_to_login),
                style = MaterialTheme.typography.bodyMedium,
                color = MyGarageColors.primary,
                fontWeight = FontWeight.Medium
            )
        }

        if (onBackClick != null) {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onBackClick, enabled = !state.isLoading) {
                Text(
                    text = stringResource(id = R.string.action_cancel),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MyGarageColors.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
