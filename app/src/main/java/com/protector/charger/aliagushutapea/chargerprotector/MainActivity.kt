package com.protector.charger.aliagushutapea.chargerprotector

import android.content.BroadcastReceiver
import android.content.Context
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.SeekBar
import com.protector.charger.aliagushutapea.chargerprotector.lib.WaveHelper
import com.protector.charger.aliagushutapea.chargerprotector.lib.WaveView
import kotlinx.android.synthetic.main.activity_main.*
import android.os.BatteryManager
import android.content.Intent
import android.content.IntentFilter
import android.util.Log


class MainActivity : AppCompatActivity() {

    private var borderWidth = 3
    private var borderColor = R.color.greenBorderColor
    private var waveHelper: WaveHelper? = null
    private val TAG = MainActivity::class.java.simpleName
    private var level = 0
    private val mBatInfoReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctxt: Context, intent: Intent) {
            level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            setLevelBattery(level)
            _txt_battery_level.text = "$level%"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setComponent()
        this.registerReceiver(this.mBatInfoReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    private fun setComponent() {
        //waveView.setBorder(borderWidth, borderColor)
        waveHelper = WaveHelper(waveView)
        shapeChoice.setOnCheckedChangeListener { _, i ->
            when (i) {
                R.id.shapeCircle -> {
                    waveView.setShapeType(WaveView.ShapeType.CIRCLE)
                }

                R.id.shapeSquare -> {
                    waveView.setShapeType(WaveView.ShapeType.SQUARE)
                }
            }
        }
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, i: Int, p2: Boolean) {
                borderWidth = i
                waveView.setBorder(borderWidth, borderColor)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })

        colorChoice.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.colorRed -> {
                    setWaveViewColor(
                        ContextCompat.getColor(this, R.color.redDark),
                        ContextCompat.getColor(this, R.color.redDark1),
                        ContextCompat.getColor(this, R.color.redBorderColor)
                    )
                }

                R.id.colorGreen -> {
                    setWaveViewColor(
                        ContextCompat.getColor(this, R.color.greenDark),
                        ContextCompat.getColor(this, R.color.greenDark1),
                        ContextCompat.getColor(this, R.color.greenBorderColor)
                    )
                }

                R.id.colorBlue -> {
                    setWaveViewColor(
                        ContextCompat.getColor(this, R.color.yellowDark),
                        ContextCompat.getColor(this, R.color.yellowDark1),
                        ContextCompat.getColor(this, R.color.yellowBorderColor)
                    )
                }
            }
        }
    }

    private fun setWaveViewColor(behindColor: Int, fontColor: Int, borderColor: Int) {
        this.borderColor = borderColor
        waveView.setBorder(borderWidth, borderColor)
        waveView.setWaveColor(behindColor, fontColor)
    }

    override fun onPause() {
        super.onPause()
        waveHelper?.cancel()
    }

    override fun onResume() {
        super.onResume()
        waveHelper?.start()
    }

    private fun setLevelBattery(level: Int) {
        when (level) {
            in 0..49 -> setWaveViewColor(
                ContextCompat.getColor(this, R.color.red),
                ContextCompat.getColor(this, R.color.redDark1),
                ContextCompat.getColor(this, R.color.redBorderColor)
            )

            in 50..74 -> setWaveViewColor(
                ContextCompat.getColor(this, R.color.yellow),
                ContextCompat.getColor(this, R.color.yellowDark1),
                ContextCompat.getColor(this, R.color.yellowBorderColor)
            )

            in 74..98 -> setWaveViewColor(
                ContextCompat.getColor(this, R.color.green),
                ContextCompat.getColor(this, R.color.greenDark1),
                ContextCompat.getColor(this, R.color.greenBorderColor)
            )
        }
    }

    override fun onStop() {
        super.onStop()
        this.unregisterReceiver(mBatInfoReceiver)
    }
}
