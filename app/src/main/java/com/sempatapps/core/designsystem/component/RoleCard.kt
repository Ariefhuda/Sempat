package com.sempatapps.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sempatapps.R
import com.sempatapps.core.designsystem.Caption
import com.sempatapps.core.designsystem.Heading2
import com.sempatapps.core.designsystem.PaleTeal
import com.sempatapps.core.designsystem.Primary
import com.sempatapps.core.designsystem.PrimaryDarker
import com.sempatapps.core.designsystem.SecondaryLight
import com.sempatapps.core.designsystem.ShadowFaint

private val CardShape = RoundedCornerShape(24.dp)

/**
 * Kartu pilihan peran (Figma: "Choose Role Guides").
 * Belum dipilih: latar hijau pucat, teks hijau tua.
 * Dipilih: latar hijau, garis tepi hijau tua, teks krem.
 */
@Composable
fun RoleCard(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
    illustration: @Composable BoxScope.() -> Unit,
) {
    val containerColor by animateColorAsState(if (isSelected) Primary else PaleTeal, label = "roleCardContainer")
    val contentColor by animateColorAsState(if (isSelected) SecondaryLight else PrimaryDarker, label = "roleCardContent")
    val borderColor by animateColorAsState(if (isSelected) PrimaryDarker else Color.Transparent, label = "roleCardBorder")

    Box(
        modifier = modifier
            .height(185.dp)
            .shadow(elevation = 4.dp, shape = CardShape, spotColor = ShadowFaint, ambientColor = ShadowFaint)
            .clip(CardShape)
            .background(containerColor)
            .border(width = 2.dp, color = borderColor, shape = CardShape)
            .selectable(selected = isSelected, role = Role.RadioButton, onClick = onClick),
    ) {
        illustration()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = 8.dp),
        ) {
            Text(
                text = title,
                style = Heading2,
                color = contentColor,
                textAlign = textAlign,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = description,
                style = Caption,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = contentColor,
                textAlign = textAlign,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun RoleCardPreview() {
    Row(
        modifier = Modifier.padding(17.dp),
        horizontalArrangement = Arrangement.spacedBy(19.dp),
    ) {
        RoleCard(
            title = "Donatur",
            description = "Salurkan pangan berlebihmu",
            isSelected = true,
            onClick = {},
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        ) {
            Image(
                painter = painterResource(R.drawable.role_donatur),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 7.dp),
            )
        }
        RoleCard(
            title = "Penerima",
            description = "Temukan pangan surplus di sekitarmu",
            isSelected = false,
            onClick = {},
            modifier = Modifier.weight(1f),
        ) {
            Image(
                painter = painterResource(R.drawable.role_penerima),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 18.dp),
            )
        }
    }
}
