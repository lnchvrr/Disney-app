package fr.isen.chevrier.disney_app.ui.common

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat
import fr.isen.chevrier.disney_app.R

fun isDrawableSupportedByComposePainter(@DrawableRes resId: Int, context: Context): Boolean {
    return try {
        val d = ContextCompat.getDrawable(context, resId) ?: return false
        isDrawableCompatibleWithComposePainter(d)
    } catch (_: Exception) {
        false
    }
}

private fun isDrawableCompatibleWithComposePainter(d: Drawable): Boolean {
    if (d is BitmapDrawable || d is VectorDrawable) return true
    // Compat vector (sans dépendance directe à androidx.vectordrawable)
    val n = d.javaClass.name
    return n.contains("VectorDrawable") && !n.contains("AnimatedVectorDrawable")
}

/**
 * [Painter] sûr : sélectionne une ressource compatible avant d'appeler [painterResource]
 * (le compilateur Compose interdit try/catch autour des @Composable).
 */
@Composable
fun rememberSafePainterResource(
    @DrawableRes resId: Int,
    @DrawableRes fallbackResId: Int = R.drawable.universe_default
): Painter {
    val context = LocalContext.current
    val safeId = remember(resId, fallbackResId, context) {
        if (isDrawableSupportedByComposePainter(resId, context)) resId else fallbackResId
    }
    return painterResource(safeId)
}
