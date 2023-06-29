package tech.realycorp.letro.ui.custom

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.realycorp.letro.ui.theme.LetroTheme
import tech.realycorp.letro.ui.theme.PrimaryMain

@Composable
fun LetroButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryMain,
            contentColor = Color.White,
        ),
        onClick = onClick,
    ) {
        Text(
            text = text,
        )
    }
}

@Preview
@Composable
fun CustomViewsPreview() {
    LetroTheme {
        Column {
            LetroButton(text = "Button") {

            }
        }
    }
}
