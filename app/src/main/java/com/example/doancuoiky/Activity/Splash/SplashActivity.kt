package com.example.doancuoiky.Activity.Splash

import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.setContent
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import com.example.doancuoiky.Activity.Auth.LoginActivity
import com.example.doancuoiky.Activity.Auth.RegisterActivity
import com.example.doancuoiky.Activity.BaseActivity
import com.example.doancuoiky.R
import com.example.doancuoiky.Activity.Dashboard.MainActivity

class SplashActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SplashScreen(
                onGetStartedClick = {
                    startActivity(Intent(this, RegisterActivity::class.java))
                },
                onLoginClick = {
                    startActivity(Intent(this, LoginActivity::class.java))
                },
                onHomeClick = {
                    startActivity(Intent(this, MainActivity::class.java))
                }
            )
        }
    }
}

@Composable
fun SplashScreen(
    onGetStartedClick: () -> Unit,
    onLoginClick: () -> Unit,
    onHomeClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(R.color.darkBrown))
    ) {
        ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
            val (homeButton, backgroundImg, logoImg) = createRefs()

            // House Button in Top-Right Corner, above the image
            Image(
                painter = painterResource(id = R.drawable.home), // Assuming R.drawable.home exists
                contentDescription = "Home",
                modifier = Modifier
                    .constrainAs(homeButton) {
                        top.linkTo(parent.top, margin = 16.dp) // Position at the top of the screen
                        end.linkTo(parent.end, margin = 16.dp)
                    }
                    .size(40.dp)
                    .background(Color.White, shape = RoundedCornerShape(50.dp))
                    .border(1.dp, Color.White, shape = RoundedCornerShape(50.dp))
                    .clickable(onClick = onHomeClick)
            )

            // Background Image
            Image(
                painter = painterResource(id = R.drawable.intro_pic),
                contentDescription = null,
                modifier = Modifier
                    .constrainAs(backgroundImg) {
                        top.linkTo(homeButton.bottom) // Place image below the home button
                        start.linkTo(parent.start)
                    }
                    .fillMaxWidth()
            )

            // Logo Image
            Image(
                painter = painterResource(id = R.drawable.br),
                contentDescription = null,
                modifier = Modifier
                    .constrainAs(logoImg) {
                        top.linkTo(backgroundImg.top)
                        bottom.linkTo(backgroundImg.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                contentScale = ContentScale.Fit
            )
        }

        val styledText = buildAnnotatedString {
            append("Welcome to your ")
            withStyle(style = SpanStyle(color = colorResource(R.color.orange))) {
                append("food\nparadise ")
            }
            append("experience food perfection delivered")
        }
        Text(
            text = styledText,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .padding(top = 32.dp)
                .padding(horizontal = 16.dp)
        )
        Text(
            text = stringResource(R.string.splashSubtitle),
            fontSize = 16.sp,
            color = Color.White,
            modifier = Modifier
                .padding(16.dp)
        )

        GetStartedButton(
            onClick = onGetStartedClick,
            onLoginClick = onLoginClick,
            modifier = Modifier
                .padding(top = 16.dp)
        )
    }
}

@Preview
@Composable
fun PreviewSplashScreen() {
    SplashScreen(
        onGetStartedClick = {},
        onLoginClick = {},
        onHomeClick = {}
    )
}