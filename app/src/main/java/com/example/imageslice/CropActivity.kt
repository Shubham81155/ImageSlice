package com.example.imageslice

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/** Shows the single grid box the user selected, cropped from the original image. */
class CropActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_crop)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.cropRoot)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val pad = (16 * resources.displayMetrics.density).toInt()
            v.setPadding(bars.left + pad, bars.top + pad, bars.right + pad, bars.bottom + pad)
            insets
        }

        val uri = intent.data
        val rows = intent.getIntExtra(EXTRA_ROWS, 1).coerceAtLeast(1)
        val cols = intent.getIntExtra(EXTRA_COLS, 1).coerceAtLeast(1)
        val row = intent.getIntExtra(EXTRA_ROW, 0).coerceIn(0, rows - 1)
        val col = intent.getIntExtra(EXTRA_COL, 0).coerceIn(0, cols - 1)

        val bitmap = uri?.let { ImageLoader.load(this, it) }
        if (bitmap == null) {
            Toast.makeText(this, R.string.load_failed, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Crop: integer boundaries so adjacent boxes tile with no gaps/overlap.
        val left = col * bitmap.width / cols
        val right = (col + 1) * bitmap.width / cols
        val top = row * bitmap.height / rows
        val bottom = (row + 1) * bitmap.height / rows
        val cropped = Bitmap.createBitmap(bitmap, left, top, right - left, bottom - top)

        findViewById<ImageView>(R.id.croppedImage).setImageBitmap(cropped)
        findViewById<TextView>(R.id.cropInfo).text =
            getString(R.string.crop_info, row + 1, col + 1, rows, cols, cropped.width, cropped.height)
        findViewById<android.widget.Button>(R.id.backButton).setOnClickListener { finish() }
    }

    companion object {
        const val EXTRA_ROWS = "rows"
        const val EXTRA_COLS = "cols"
        const val EXTRA_ROW = "row"
        const val EXTRA_COL = "col"
    }
}
