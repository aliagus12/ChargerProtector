package com.protector.charger.aliagushutapea.chargerprotector.lib

import android.animation.Animator
import android.view.animation.LinearInterpolator
import android.animation.ValueAnimator
import android.animation.ObjectAnimator
import android.view.animation.DecelerateInterpolator
import android.animation.AnimatorSet


class WaveHelper(private val waveView: WaveView) {

    var animatorSet: AnimatorSet? = null

    init {
        initAnimation()
    }

    fun start() {
        waveView.setShowWave(true)
        animatorSet?.start()
    }

    fun cancel() {
        animatorSet?.end()
    }

    private fun initAnimation() {
        val listAnimator = ArrayList<Animator>()

        // horizontal animation.
        // wave waves infinitely.
        val waveShiftAnim = ObjectAnimator.ofFloat(waveView, "waveShiftRatio", 0f, 1f)
        waveShiftAnim.repeatCount = ValueAnimator.INFINITE
        waveShiftAnim.duration = 1000
        waveShiftAnim.interpolator = LinearInterpolator()
        listAnimator.add(waveShiftAnim)

        // vertical animation.
        // water level increases from 0 to center of WaveView
        val waterLevelAnim = ObjectAnimator.ofFloat(waveView, "waterLevelRatio", 0f, 0.5f)
        waterLevelAnim.duration = 10000
        waterLevelAnim.interpolator = DecelerateInterpolator()
        listAnimator.add(waterLevelAnim)

        // amplitude animation.
        // wave grows big then grows small, repeatedly
        val amplitudeAnim = ObjectAnimator.ofFloat(waveView, "amplitudeRatio", 0.0001f, 0.05f)
        amplitudeAnim.repeatCount = ValueAnimator.INFINITE
        amplitudeAnim.repeatMode = ValueAnimator.REVERSE
        amplitudeAnim.duration = 5000
        amplitudeAnim.interpolator = LinearInterpolator()
        listAnimator.add(amplitudeAnim)

        animatorSet = AnimatorSet()
        animatorSet?.playTogether(listAnimator)
    }
}