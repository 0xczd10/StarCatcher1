package com.starqr.starcatcher

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

class CarouselPageTransformer : ViewPager2.PageTransformer {

    override fun transformPage(page: View, position: Float) {
        val pageMarginPx = 100
        val offsetPx = 140

        val offset = position * -(2 * offsetPx + pageMarginPx)
        page.translationX = offset

        // Масштабируем страницы, которые не по центру
        val scaleFactor = 1 - abs(position * 0.3f)
        page.scaleX = scaleFactor
        page.scaleY = scaleFactor

        // Делаем страницы по бокам полупрозрачными
        val alphaFactor = 1 - abs(position * 0.5f)
        page.alpha = alphaFactor
    }
}