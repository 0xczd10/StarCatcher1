package com.starqr.starcatcher

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2

class LevelSelectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_level_select)

        val levelsViewPager: ViewPager2 = findViewById(R.id.levelsViewPager)
        GameProgress.initialize(this)

        // Настройка ViewPager2
        levelsViewPager.adapter = LevelAdapter(50, GameProgress.getHighestLevelUnlocked())
        levelsViewPager.offscreenPageLimit = 3 // Важно для плавной работы
        levelsViewPager.setPageTransformer(CarouselPageTransformer())

        // Добавляем отступы, чтобы были видны соседние карточки
        val pageMarginPx = 100
        val pagerWidth = resources.displayMetrics.widthPixels - 2 * pageMarginPx
        val screenWidth = resources.displayMetrics.widthPixels
        val offsetPx = screenWidth - pagerWidth - pageMarginPx
        levelsViewPager.setPadding(offsetPx, 0, offsetPx, 0)

        // Переходим к последнему открытому уровню
        levelsViewPager.setCurrentItem(GameProgress.getHighestLevelUnlocked() - 1, false)
    }

    override fun onResume() {
        super.onResume()
        // Обновляем адаптер, когда возвращаемся на экран
        val levelsViewPager: ViewPager2 = findViewById(R.id.levelsViewPager)
        val currentPosition = levelsViewPager.currentItem
        levelsViewPager.adapter = LevelAdapter(50, GameProgress.getHighestLevelUnlocked())
        levelsViewPager.setCurrentItem(currentPosition, false)
    }
}