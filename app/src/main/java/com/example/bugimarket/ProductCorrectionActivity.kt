package hansung.ac.gomo7

import android.os.Bundle
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ProductCorrectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_correction)

        val saleSwitch = findViewById<Switch>(R.id.sale_switch)
        val saleIndicator = findViewById<TextView>(R.id.sale_state_indicator)

        saleSwitch.setOnCheckedChangeListener { _, isChecked ->
            saleIndicator.text = if (isChecked) "판매중" else "판매완료"
        }
    }
}
