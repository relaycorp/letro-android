package tech.relaycorp.letro.utils.android

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect

fun Bitmap.toCircle(): Bitmap {
    val output = Bitmap.createBitmap(
        this.width,
        this.height,
        Bitmap.Config.ARGB_8888,
    )
    val canvas = Canvas(output)
    val color = -0xbdbdbe
    val paint = Paint()
    val rect = Rect(
        0,
        0,
        this.width,
        this.height,
    )
    paint.isAntiAlias = true
    canvas.drawARGB(0, 0, 0, 0)
    paint.color = color
    canvas.drawCircle(
        (this.width / 2).toFloat(),
        (
            this.height / 2
            ).toFloat(),
        (this.width / 2).toFloat(),
        paint,
    )
    paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_IN))
    canvas.drawBitmap(this, rect, rect, paint)
    return output
}
