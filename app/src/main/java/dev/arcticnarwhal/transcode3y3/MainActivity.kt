package dev.arcticnarwhal.transcode3y3

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup.MarginLayoutParams
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dev.arcticnarwhal.transcode3y3.databinding.ActivityMainBinding
import java.util.function.IntUnaryOperator
import java.util.stream.IntStream


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var clipMan: ClipboardManager
    private lateinit var dialogAbout: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insetsSystemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val insetsIme = windowInsets.getInsets(WindowInsetsCompat.Type.ime())
            // Apply the insetsSystemBars as a margin to the view. This solution sets
            // only the bottom, left, and right dimensions, but you can apply whichever
            // insetsSystemBars are appropriate to your layout. You can also update the view padding
            // if that's more appropriate.

            view.updateLayoutParams<MarginLayoutParams> {
                leftMargin = insetsSystemBars.left
                bottomMargin = if (insetsIme.bottom != 0) insetsIme.bottom else insetsSystemBars.bottom
                rightMargin = insetsSystemBars.right
            }

            windowInsets
        }

        setSupportActionBar(binding.toolbar)

        clipMan = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

        setSupportActionBar(binding.toolbar)

        binding.fab.setOnClickListener { view ->
            val clip = ClipData.newPlainText("Y3Y Output", binding.output.text)
            clipMan.setPrimaryClip(clip)
            Snackbar.make(view, getString(R.string.snackbar_copied), Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        binding.input.doAfterTextChanged {
            binding.output.setText(transcode3y3(binding.input.text.toString()))
        }

        val builder = MaterialAlertDialogBuilder(this)
        builder
            .setMessage(R.string.dialog_about)
            .setTitle(R.string.dialog_about_title)
            .setPositiveButton(getString(R.string.btn_close)) { _, _ -> }

        dialogAbout = builder.create()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_about -> onAboutClicked()
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun onAboutClicked(): Boolean {
        dialogAbout.show()
        return true
    }

    //Originally implemented by Twilight Sparkle (@yourcompanionAI). See https://synthetic.garden/3y3.htm
    private var from3y3: IntUnaryOperator = IntUnaryOperator { cp -> if (cp in 0xe0001..0xe007e) cp - 0xe0000 else cp }
    private var to3y3: IntUnaryOperator = IntUnaryOperator { cp -> if (cp in 0x01..0x7e) cp + 0xe0000 else cp }

    private fun transcode3y3(text: String): String {
        if (text.isEmpty()) {
            binding.inputLayout.helperText = getString(R.string.helper_empty)
            return ""
        }
        for (cp in text.codePoints()) {
            if (cp in 0xe0001..0xe007e) {
                binding.inputLayout.helperText = getString(R.string.helper_decoding)
                return text.codePoints().map(from3y3).codePointsToString()
            }
        }
        binding.inputLayout.helperText = getString(R.string.helper_encoding)
        return text.codePoints().map(to3y3).codePointsToString()
    }

    private fun IntStream.codePointsToString(): String = buildString {
        this@codePointsToString.forEach { cp ->
            appendCodePoint(cp)
        }
    }
}