package com.starqr.starcatcher

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.floatingactionbutton.FloatingActionButton

class GameActivity : AppCompatActivity(), GameResultDialogFragment.GameResultDialogListener {

    private lateinit var constellationView: ConstellationView
    private lateinit var timerTextView: TextView
    private lateinit var scoreTextView: TextView
    private lateinit var hintButton: FloatingActionButton
    private lateinit var hintsCountTextView: TextView

    // --- Игровые переменные ---
    private var timer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 0
    private var score = 0
    private var hintsLeft = 3

    // --- Переменная для межстраничной рекламы ---
    private var mInterstitialAd: InterstitialAd? = null
    private val TAG = "GameActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        // 1. Загружаем межстраничную рекламу заранее, пока идет игра.
        loadInterstitialAd()

        // Инициализация всех View-элементов
        constellationView = findViewById(R.id.constellationView)
        timerTextView = findViewById(R.id.timerTextView)
        scoreTextView = findViewById(R.id.scoreTextView)
        hintButton = findViewById(R.id.hintButton)
        hintsCountTextView = findViewById(R.id.hintsCountTextView)

        val levelId = intent.getIntExtra("LEVEL_ID", 1)
        val level = LevelGenerator.generateLevel(levelId)
        constellationView.setLevel(level)

        timeLeftInMillis = (60000L - (levelId - 1) * 1000).coerceAtLeast(15000L)
        startTimer()
        updateHintsCount()
        updateScore(0)

        // Обработчик победы в уровне
        constellationView.onLevelCompleteListener = {
            timer?.cancel()
            val timeBonus = (timeLeftInMillis / 1000) * 10
            updateScore(timeBonus.toInt())
            GameProgress.levelCompleted(levelId + 1)
            showGameDialog("You Win!", "Your score: $score", true)
        }

        // Обработчик нажатия на кнопку подсказки
        hintButton.setOnClickListener {
            if (hintsLeft > 0) {
                hintsLeft--
                updateHintsCount()
                updateScore(-250)
                constellationView.showHint()
            }
        }
    }

    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        // ↓↓↓ ВАШ РЕАЛЬНЫЙ ID РЕКЛАМНОГО БЛОКА ТЕПЕРЬ ЗДЕСЬ ↓↓↓
        val adUnitId = "ca-app-pub-4132747243117791/6019107613"

        InterstitialAd.load(this, adUnitId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                // Реклама успешно загружена и готова к показу.
                Log.d(TAG, "Ad was loaded.")
                mInterstitialAd = interstitialAd
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                // Ошибка загрузки, ничего страшного. Просто не будем показывать рекламу.
                Log.d(TAG, loadAdError.toString())
                mInterstitialAd = null
            }
        })
    }

    private fun showInterstitialAd() {
        // Показываем рекламу, только если она успела загрузиться
        if (mInterstitialAd != null) {
            // Устанавливаем обработчик, чтобы знать, когда пользователь закроет рекламу
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                // Вызывается после закрытия рекламы
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Ad dismissed fullscreen content.")
                    finish() // Возвращаемся на экран уровней ПОСЛЕ рекламы
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "Ad failed to show fullscreen content.")
                    finish() // Если реклама не показалась, все равно возвращаемся
                }
            }
            mInterstitialAd?.show(this)
        } else {
            Log.d(TAG, "The interstitial ad wasn't ready yet.")
            finish() // Если реклама не загрузилась, просто возвращаемся на экран уровней
        }
    }

    // Этот метод вызывается из диалога, когда пользователь нажимает кнопку
    override fun onDialogDismissed() {
        // 2. Вместо того, чтобы сразу закрывать Activity, мы сначала пытаемся показать рекламу
        showInterstitialAd()
    }

    private fun startTimer() {
        timer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                val minutes = (timeLeftInMillis / 1000) / 60
                val seconds = (timeLeftInMillis / 1000) % 60
                timerTextView.text = String.format("%d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                showGameDialog("Time's Up!", "Try again.", false)
            }
        }.start()
    }

    private fun updateScore(points: Int) {
        score = (score + points).coerceAtLeast(0)
        scoreTextView.text = "Score: $score"
    }

    private fun updateHintsCount() {
        hintsCountTextView.text = hintsLeft.toString()
        hintButton.isEnabled = hintsLeft > 0
    }

    private fun showGameDialog(title: String, message: String, isWin: Boolean) {
        val dialog = GameResultDialogFragment.newInstance(title, message, isWin)
        dialog.listener = this
        dialog.show(supportFragmentManager, "GameResultDialog")
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}