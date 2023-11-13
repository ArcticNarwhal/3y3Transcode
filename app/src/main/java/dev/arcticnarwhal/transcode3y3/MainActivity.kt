package dev.arcticnarwhal.transcode3y3

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.snackbar.Snackbar
import dev.arcticnarwhal.transcode3y3.databinding.ActivityMainBinding
import java.util.function.IntUnaryOperator
import java.util.stream.IntStream


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var clipMan: ClipboardManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        clipMan = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

        setSupportActionBar(binding.toolbar)

        binding.fab.setOnClickListener { view ->
            val clip = ClipData.newPlainText("Y3Y Output", binding.output.text)
            clipMan.setPrimaryClip(clip)
            Snackbar.make(view, "Copied output", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        binding.input.doAfterTextChanged {
            binding.output.setText(transcode3y3(binding.input.text.toString()))
        }
    }

    //Originally implemented by Twilight Sparkle (@yourcompanionAI). See https://synthetic.garden/3y3.htm

    private var from3y3: IntUnaryOperator = IntUnaryOperator { cp -> if (cp in 0xe0001..0xe007e) cp - 0xe0000 else cp }
    private var to3y3: IntUnaryOperator = IntUnaryOperator { cp -> if (cp in 0x01..0x7e) cp + 0xe0000 else cp }

    private fun transcode3y3(text: String): String {
        for (cp in text.codePoints()) {
            if (cp in 0xe0001..0xe007e) {
                return text.codePoints().map(from3y3).codePointsToString()
            }
        }
        return text.codePoints().map(to3y3).codePointsToString()
    }

    private fun IntStream.codePointsToString(): String = buildString {
        this@codePointsToString.forEach { cp ->
            appendCodePoint(cp)
        }
    }
}