package com.starqr.starcatcher // Убедитесь, что имя пакета ваше

import android.animation.*
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Находим все наши объекты
        val cloud = findViewById<ImageView>(R.id.cloud)
        val planet = findViewById<ImageView>(R.id.centralPlanet)
        val comet = findViewById<ImageView>(R.id.comet)
        val playButton = findViewById<Button>(R.id.playButton)

        // Запускаем все анимации
        startFloatingAnimation(cloud, 10000L, 30f, 50f)
        startPlanetAnimation(planet)
        startCometAnimation(comet)
        startButtonAnimation(playButton)

        playButton.setOnClickListener {
            val intent = Intent(this, LevelSelectActivity::class.java)
            startActivity(intent)
        }
    }

    // Анимация "плавания" для облака
    private fun startFloatingAnimation(view: View, duration: Long, translationX: Float, translationY: Float) {
        val animatorX = ObjectAnimator.ofFloat(view, "translationX", translationX).apply {
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
        }
        val animatorY = ObjectAnimator.ofFloat(view, "translationY", translationY).apply {
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
        }
        AnimatorSet().apply {
            playTogether(animatorX, animatorY)
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    // НОВАЯ АНИМАЦИЯ: Медленное вращение и дрейф планеты
    private fun startPlanetAnimation(planet: View) {
        // Вращение вокруг своей оси
        val rotation = ObjectAnimator.ofFloat(planet, "rotation", 0f, 360f).apply {
            duration = 30000 // Очень медленно, 30 секунд на оборот
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator() // Равномерное вращение
        }
        // Плавное движение вверх-вниз
        val translationY = ObjectAnimator.ofFloat(planet, "translationY", 0f, -40f, 0f).apply {
            duration = 10000 // 10 секунд на цикл
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }
        AnimatorSet().apply {
            playTogether(rotation, translationY)
            start()
        }
    }

    // НОВАЯ АНИМАЦИЯ: Полет кометы
    private fun startCometAnimation(comet: View) {
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        val screenHeight = resources.displayMetrics.heightPixels.toFloat()

        val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 5000 // 5 секунд на полет
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                val progress = it.animatedValue as Float
                comet.alpha = 1 - kotlin.math.abs(0.5f - progress) * 2 // Появляется и исчезает
                comet.translationX = -200f + (screenWidth + 200f) * progress
                comet.translationY = -200f + (screenHeight + 200f) * progress
                comet.rotation = 45f // Наклон кометы
            }
        }

        // Повторяем полет каждые 8 секунд
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                animator.start()
                handler.postDelayed(this, 8000) // 5с полет + 3с пауза
            }
        }
        handler.post(runnable)
    }

    // Анимация "дыхания" для кнопки
    private fun startButtonAnimation(button: View) {
        ObjectAnimator.ofPropertyValuesHolder(
            button,
            PropertyValuesHolder.ofFloat("scaleX", 1.0f, 1.05f, 1.0f),
            PropertyValuesHolder.ofFloat("scaleY", 1.0f, 1.05f, 1.0f)
        ).apply {
            duration = 2500
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }
}