package com.starqr.starcatcher // Убедитесь, что имя пакета ваше

import android.animation.*
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

class MainActivity : AppCompatActivity() {

    private lateinit var consentInformation: ConsentInformation
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // --- Шаг 1: Запрос согласия на обработку данных (GDPR) ---
        // Этот код должен выполняться перед инициализацией рекламы
        val params = ConsentRequestParameters.Builder().build()
        consentInformation = UserMessagingPlatform.getConsentInformation(this)
        consentInformation.requestConsentInfoUpdate(
            this,
            params,
            {
                // Запрос обновлен. Проверяем, нужно ли показывать форму.
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(this) { loadAndShowError ->
                    if (loadAndShowError != null) {
                        Log.w(TAG, "Consent form error: ${loadAndShowError.message}")
                    }
                    // Согласие получено или не требуется.
                    // Теперь можно безопасно инициализировать AdMob.
                    initializeMobileAdsAndLoadBanner()
                }
            },
            { requestConsentError ->
                // Ошибка запроса. Все равно инициализируем рекламу.
                Log.w(TAG, "Consent request error: ${requestConsentError.message}")
                initializeMobileAdsAndLoadBanner()
            }
        )

        // --- Шаг 2: Настройка интерфейса и анимаций ---
        // Этот код выполняется параллельно с запросом согласия
        setupUIAndAnimations()
    }

    private fun initializeMobileAdsAndLoadBanner() {
        // Убедимся, что инициализация происходит в основном потоке
        runOnUiThread {
            // Инициализация AdMob SDK
            MobileAds.initialize(this) {}

            // Загрузка рекламного баннера
            val adView = findViewById<AdView>(R.id.adView)
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
        }
    }

    private fun setupUIAndAnimations() {
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

    // --- Функции для анимаций ---

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

    private fun startPlanetAnimation(planet: View) {
        val rotation = ObjectAnimator.ofFloat(planet, "rotation", 0f, 360f).apply {
            duration = 30000
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
        }
        val translationY = ObjectAnimator.ofFloat(planet, "translationY", 0f, -40f, 0f).apply {
            duration = 10000
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }
        AnimatorSet().apply {
            playTogether(rotation, translationY)
            start()
        }
    }

    private fun startCometAnimation(comet: View) {
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        val screenHeight = resources.displayMetrics.heightPixels.toFloat()

        val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 5000
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                val progress = it.animatedValue as Float
                comet.alpha = 1 - kotlin.math.abs(0.5f - progress) * 2
                comet.translationX = -200f + (screenWidth + 200f) * progress
                comet.translationY = -200f + (screenHeight + 200f) * progress
                comet.rotation = 45f
            }
        }

        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                animator.start()
                handler.postDelayed(this, 8000)
            }
        }
        handler.post(runnable)
    }

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