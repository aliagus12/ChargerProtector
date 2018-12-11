package com.protector.charger.aliagushutapea.chargerprotector.lib

import android.content.Context
import android.graphics.*
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import com.protector.charger.aliagushutapea.chargerprotector.R

class WaveView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
    View(context, attrs, defStyle) {
    /**
     * +------------------------+
     * |<--wave length->        |______
     * |   /\          |   /\   |  |
     * |  /  \         |  /  \  | amplitude
     * | /    \        | /    \ |  |
     * |/      \       |/      \|__|____
     * |        \      /        |  |
     * |         \    /         |  |
     * |          \  /          |  |
     * |           \/           | water level
     * |                        |  |
     * |                        |  |
     * +------------------------+__|____
     */


    private val TAG = WaveView::class.java.simpleName
    private val defaultAmplitudeRatio = 0.05f
    private val defaultWaterLevelRatio = 0.5f
    private val defaultWaveLengthRatio = 1.0f
    private val defaultWaveShiftRatio = 0.0f

    private val defaultBehindWaveColor = Color.parseColor("#28FFFFFF")
    private val defaultFrontWaveColor = ContextCompat.getColor(context, R.color.green)
    private val defaultWaveShape = ShapeType.CIRCLE

    enum class ShapeType {
        CIRCLE,
        SQUARE
    }

    // if true, the shader will display the wave
   private var mShowWave: Boolean = false

    // shader containing repeated waves
    private var mWaveShader: BitmapShader? = null
    // shader matrix
    private var mShaderMatrix: Matrix? = null
    // paint to draw wave
    private var mViewPaint: Paint? = null
    // paint to draw border
    private var mBorderPaint: Paint? = null

    private var mDefaultAmplitude: Float = 0.toFloat()
    private var mDefaultWaterLevel: Float = 0.toFloat()
    private var mDefaultWaveLength: Float = 0.toFloat()
    private var mDefaultAngularFrequency: Double = 0.toDouble()

    private var mAmplitudeRatio = defaultAmplitudeRatio
    private var mWaveLengthRatio = defaultWaveLengthRatio
    private var mWaterLevelRatio = defaultWaterLevelRatio
    private var mWaveShiftRatio = defaultWaveShiftRatio

    private var mBehindWaveColor = defaultBehindWaveColor
    private var mFrontWaveColor = defaultFrontWaveColor
    private var mShapeType = defaultWaveShape

    init {
        mShaderMatrix = Matrix()
        mViewPaint = Paint()
        mViewPaint?.isAntiAlias = true
    }

    fun getWaveShiftRatio(): Float {
        return mWaveShiftRatio
    }

    /**
     * Shift the wave horizontally according to `waveShiftRatio`.
     *
     * @param waveShiftRatio Should be 0 ~ 1. Default to be 0.
     * Result of waveShiftRatio multiples width of WaveView is the length to shift.
     */
    fun setWaveShiftRatio(waveShiftRatio: Float) {
        if (mWaveShiftRatio != waveShiftRatio) {
            mWaveShiftRatio = waveShiftRatio
            invalidate()
        }
    }

    fun getWaterLevelRatio(): Float {
        return mWaterLevelRatio
    }

    /**
     * Set water level according to `waterLevelRatio`.
     *
     * @param waterLevelRatio Should be 0 ~ 1. Default to be 0.5.
     * Ratio of water level to WaveView height.
     */
    fun setWaterLevelRatio(waterLevelRatio: Float) {
        if (mWaterLevelRatio != waterLevelRatio) {
            mWaterLevelRatio = waterLevelRatio
            invalidate()
        }
    }

    fun getAmplitudeRatio(): Float {
        return mAmplitudeRatio
    }

    /**
     * Set vertical size of wave according to `amplitudeRatio`
     *
     * @param amplitudeRatio Default to be 0.05. Result of amplitudeRatio + waterLevelRatio should be less than 1.
     * Ratio of amplitude to height of WaveView.
     */
    fun setAmplitudeRatio(amplitudeRatio: Float) {
        if (mAmplitudeRatio != amplitudeRatio) {
            mAmplitudeRatio = amplitudeRatio
            invalidate()
        }
    }

    fun getWaveLengthRatio(): Float {
        return mWaveLengthRatio
    }

    /**
     * Set horizontal size of wave according to `waveLengthRatio`
     *
     * @param waveLengthRatio Default to be 1.
     * Ratio of wave length to width of WaveView.
     */
    fun setWaveLengthRatio(waveLengthRatio: Float) {
        mWaveLengthRatio = waveLengthRatio
    }

    fun isShowWave(): Boolean {
        return mShowWave
    }

    fun setShowWave(showWave: Boolean) {
        mShowWave = showWave
    }

    fun setBorder(width: Int, color: Int) {
        if (mBorderPaint == null) {
            mBorderPaint = Paint()
            mBorderPaint?.isAntiAlias = true
            mBorderPaint?.style = Paint.Style.STROKE
        }
        mBorderPaint?.color = color
        mBorderPaint?.strokeWidth = width.toFloat()
        invalidate()
    }

    fun setWaveColor(behindWaveColor: Int, frontWaveColor: Int) {
        mBehindWaveColor = behindWaveColor
        mFrontWaveColor = frontWaveColor
        if (width > 0 && height > 0) {
            mWaveShader = null
            createShader()
            invalidate()
        }
    }

    fun setShapeType(shapeType: ShapeType) {
        mShapeType = shapeType
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        createShader()
    }

    /**
     * Create the shader with default waves which repeat horizontally, and clamp vertically
     */
    private fun createShader() {
        mDefaultAngularFrequency = 2.0f * Math.PI / defaultWaveLengthRatio.toDouble() / width.toDouble()
        mDefaultAmplitude = height * defaultAmplitudeRatio
        mDefaultWaterLevel = height * defaultWaterLevelRatio
        mDefaultWaveLength = width.toFloat()

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val wavePaint = Paint()
        wavePaint.strokeWidth = 2f
        wavePaint.isAntiAlias = true

        // Draw default waves into the bitmap
        // y=Asin(ωx+φ)+h
        val endX = width + 1
        val endY = height + 1

        val waveY = FloatArray(endX)

        wavePaint.color = mBehindWaveColor
        for (beginX in 0 until endX) {
            val wx = beginX * mDefaultAngularFrequency
            val beginY = (mDefaultWaterLevel + mDefaultAmplitude * Math.sin(wx)).toFloat()
            canvas.drawLine(beginX.toFloat(), beginY, beginX.toFloat(), endY.toFloat(), wavePaint)
            waveY[beginX] = beginY
        }

        wavePaint.color = mFrontWaveColor
        val wave2Shift = (mDefaultWaveLength / 4).toInt()
        for (beginX in 0 until endX) {
            canvas.drawLine(
                beginX.toFloat(),
                waveY[(beginX + wave2Shift) % endX],
                beginX.toFloat(),
                endY.toFloat(),
                wavePaint
            )
        }

        // use the bitamp to create the shader
        mWaveShader = BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.CLAMP)
        mViewPaint?.shader = mWaveShader
    }

    override fun onDraw(canvas: Canvas) {
        // modify paint shader according to mShowWave state
        if (mShowWave && mWaveShader != null) {

            // first call after mShowWave, assign it to our paint
            if (mViewPaint?.shader == null) {
                mViewPaint?.shader = mWaveShader
            }

            // sacle shader according to mWaveLengthRatio and mAmplitudeRatio
            // this decides the size(mWaveLengthRatio for width, mAmplitudeRatio for height) of waves
            mShaderMatrix?.setScale(
                mWaveLengthRatio / defaultWaveLengthRatio,
                mAmplitudeRatio / defaultAmplitudeRatio,
                0f,
                mDefaultWaterLevel
            )
            // translate shader according to mWaveShiftRatio and mWaterLevelRatio
            // this decides the start position(mWaveShiftRatio for x, mWaterLevelRatio for y) of waves
            mShaderMatrix?.postTranslate(
                mWaveShiftRatio * width,
                (defaultWaterLevelRatio - mWaterLevelRatio) * height
            )

            // assign matrix to invalidate the shader
            mWaveShader?.setLocalMatrix(mShaderMatrix)

            val borderWidth = if (mBorderPaint == null) 0f else mBorderPaint?.strokeWidth
            borderWidth?.let {
                when (mShapeType) {
                    WaveView.ShapeType.CIRCLE -> {
                        if (it > 0) {
                            canvas.drawCircle(
                                width / 2f, height / 2f,
                                (width - it) / 2f - 1f, mBorderPaint
                            )
                        }
                        val radius = width / 2f - it
                        canvas.drawCircle(width / 2f, height / 2f, radius, mViewPaint)
                    }
                    WaveView.ShapeType.SQUARE -> {
                        if (it > 0) {
                            canvas.drawRect(
                                it / 2f,
                                it / 2f,
                                width.toFloat() - it / 2f - 0.5f,
                                height.toFloat() - it / 2f - 0.5f,
                                mBorderPaint
                            )
                        }
                        canvas.drawRect(
                            it, it, width - it,
                            height - it, mViewPaint
                        )
                    }
                }
            }
        } else {
            mViewPaint?.shader = null
        }
    }
}