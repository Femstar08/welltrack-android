package com.beaconledger.welltrack.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    onSignUpClick: () -> Unit,
    onSignInClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Logo/Icon placeholder
        Card(
            modifier = Modifier.size(120.dp),
            shape = RoundedCornerShape(60.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "WT",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // App Name
        Text(
            text = "WellTrack",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // App Description
        Text(
            text = "Your comprehensive health and nutrition companion. Track meals, monitor fitness, and optimize your wellness journey.",
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            lineHeight = 24.sp
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Sign Up Button
        Button(
            onClick = onSignUpClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(
                text = "Get Started",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Sign In Button
        OutlinedButton(
            onClick = onSignInClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(
                text = "Sign In",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Terms and Privacy
        Text(
            text = "By continuing, you agree to our Terms of Service and Privacy Policy",
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            lineHeight = 16.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    MaterialTheme {
        WelcomeScreen(
            onSignUpClick = {},
            onSignInClick = {}
        )
    }
}