package com.example.imageslice

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var gridView: GridImageView
    private lateinit var rowsLabel: TextView
    private lateinit var colsLabel: TextView
    private lateinit var viewButton: Button
    private lateinit var hint: TextView

    private var imageUri: Uri? = null

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) loadImage(uri)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        gridView = findViewById(R.id.gridView)
        rowsLabel = findViewById(R.id.rowsLabel)
        colsLabel = findViewById(R.id.colsLabel)
        viewButton = findViewById(R.id.viewButton)
        hint = findViewById(R.id.hint)
        val rowsSeek = findViewById<SeekBar>(R.id.rowsSeek)
        val colsSeek = findViewById<SeekBar>(R.id.colsSeek)

        findViewById<Button>(R.id.pickButton).setOnClickListener {
            pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        val listener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(bar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateGrid(rowsSeek.progress + 1, colsSeek.progress + 1)
            }
            override fun onStartTrackingTouch(bar: SeekBar?) {}
            override fun onStopTrackingTouch(bar: SeekBar?) {}
        }
        rowsSeek.setOnSeekBarChangeListener(listener)
        colsSeek.setOnSeekBarChangeListener(listener)
        updateGrid(rowsSeek.progress + 1, colsSeek.progress + 1)

        gridView.onSelectionChanged = { selected ->
            viewButton.isEnabled = selected
            hint.setText(if (selected) R.string.hint_selected else R.string.hint_tap_box)
        }

        viewButton.setOnClickListener {
            val uri = imageUri ?: return@setOnClickListener
            if (!gridView.hasSelection) return@setOnClickListener
            val intent = Intent(this, CropActivity::class.java).apply {
                data = uri
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra(CropActivity.EXTRA_ROWS, gridView.rows)
                putExtra(CropActivity.EXTRA_COLS, gridView.cols)
                putExtra(CropActivity.EXTRA_ROW, gridView.selectedRow)
                putExtra(CropActivity.EXTRA_COL, gridView.selectedCol)
            }
            startActivity(intent)
        }

        // Restore state after rotation
        savedInstanceState?.getParcelable<Uri>(STATE_URI)?.let { loadImage(it) }
    }

    private fun updateGrid(rows: Int, cols: Int) {
        rowsLabel.text = getString(R.string.rows_label, rows)
        colsLabel.text = getString(R.string.cols_label, cols)
        gridView.setGrid(rows, cols)
    }

    private fun loadImage(uri: Uri) {
        val bitmap = ImageLoader.load(this, uri)
        if (bitmap == null) {
            Toast.makeText(this, R.string.load_failed, Toast.LENGTH_SHORT).show()
            return
        }
        imageUri = uri
        gridView.setImage(bitmap)
        hint.setText(R.string.hint_tap_box)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(STATE_URI, imageUri)
    }

    private companion object {
        const val STATE_URI = "image_uri"
    }
}
