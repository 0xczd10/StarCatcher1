package com.starqr.starcatcher

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import kotlin.math.pow
import kotlin.math.sqrt

class ConstellationView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // --- Кисти для рисования ---
    private val starPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#ADD323")
        strokeWidth = 8f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val tempLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 6f
        style = Paint.Style.STROKE
    }
    private val blackHolePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val hintLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.CYAN
        strokeWidth = 10f
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(30f, 15f), 0f) // Пунктирная линия
    }

    // --- Ресурсы и константы ---
    private val starBitmap: Bitmap
    private val touchRadius = 80f // Увеличенный радиус для удобства нажатия

    // --- Состояние игры ---
    private var currentLevel: ConstellationLevel? = null
    private val userConnections = mutableSetOf<Connection>()
    private var startingStar: Star? = null
    private var tempLineEnd: Pair<Float, Float>? = null
    private var tempLineColor: Int = Color.GRAY

    // --- Анимации ---
    private var lineAnimator: ValueAnimator? = null
    private var animatedConnection: Connection? = null
    private var animationProgress = 1f
    private var hintAnimator: ValueAnimator? = null
    private var hintConnection: Connection? = null
    private var hintAlpha = 0
    var onLevelCompleteListener: (() -> Unit)? = null

    init {
        // Загружаем SVG-звезду и конвертируем в Bitmap для быстрой отрисовки
        val starDrawable = ContextCompat.getDrawable(context, R.drawable.ic_star)
        starBitmap = starDrawable!!.toBitmap(60, 60)
    }

    fun setLevel(level: ConstellationLevel) {
        currentLevel = level
        userConnections.clear()
        lineAnimator?.cancel()
        hintAnimator?.cancel()
        animatedConnection = null
        hintConnection = null
        invalidate() // Перерисовать View с новым уровнем
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (currentLevel == null) return

        // 1. Рисуем черные дыры (на заднем плане)
        currentLevel!!.blackHoles.forEach { hole ->
            val cx = hole.x * width
            val cy = hole.y * height
            val radius = hole.radius * width
            blackHolePaint.shader = RadialGradient(
                cx, cy, radius,
                intArrayOf(Color.TRANSPARENT, Color.BLACK, Color.BLACK),
                floatArrayOf(0f, 0.8f, 1f),
                Shader.TileMode.CLAMP
            )
            canvas.drawCircle(cx, cy, radius, blackHolePaint)
        }

        // 2. Рисуем уже нарисованные линии
        userConnections.forEach { connection ->
            val star1 = findStarById(connection.starId1)
            val star2 = findStarById(connection.starId2)
            if (star1 != null && star2 != null) {
                canvas.drawLine(star1.x * width, star1.y * height, star2.x * width, star2.y * height, linePaint)
            }
        }

        // 3. Рисуем анимированную подсказку
        if (hintConnection != null) {
            val star1 = findStarById(hintConnection!!.starId1)
            val star2 = findStarById(hintConnection!!.starId2)
            if (star1 != null && star2 != null) {
                hintLinePaint.alpha = hintAlpha
                canvas.drawLine(star1.x * width, star1.y * height, star2.x * width, star2.y * height, hintLinePaint)
            }
        }

        // 4. Анимация рисования текущей линии
        if (animatedConnection != null) {
            val star1 = findStarById(animatedConnection!!.starId1)
            val star2 = findStarById(animatedConnection!!.starId2)
            if (star1 != null && star2 != null) {
                val startX = star1.x * width; val startY = star1.y * height
                val endX = startX + (star2.x * width - startX) * animationProgress
                val endY = startY + (star2.y * height - startY) * animationProgress
                canvas.drawLine(startX, startY, endX, endY, linePaint)
            }
        }

        // 5. Временная линия при перетаскивании
        if (startingStar != null && tempLineEnd != null) {
            tempLinePaint.color = tempLineColor
            canvas.drawLine(startingStar!!.x * width, startingStar!!.y * height, tempLineEnd!!.first, tempLineEnd!!.second, tempLinePaint)
        }

        // 6. Рисуем все звезды поверх всего
        currentLevel!!.stars.forEach { star ->
            canvas.drawBitmap(starBitmap, star.x * width - starBitmap.width / 2, star.y * height - starBitmap.height / 2, starPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val star = findStarAt(touchX, touchY)
                if (star != null) {
                    startingStar = star
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (startingStar != null) {
                    tempLineEnd = Pair(touchX, touchY)
                    validateTempLine(touchX, touchY)
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                if (startingStar != null) {
                    val endStar = findStarAt(touchX, touchY)
                    if (endStar != null && endStar.id != startingStar!!.id) {
                        val newConnection = Connection(startingStar!!.id, endStar.id)

                        if (currentLevel?.requiredConnections?.contains(newConnection) == true &&
                            !userConnections.contains(newConnection) &&
                            isConnectionValid(startingStar!!, endStar)) {
                            animateLine(newConnection)
                        }
                    }
                }
                startingStar = null
                tempLineEnd = null
                tempLineColor = Color.GRAY
                invalidate()
            }
        }
        return true
    }

    // --- Публичные методы для управления из GameActivity ---

    fun showHint() {
        val nextConnection = currentLevel?.requiredConnections?.firstOrNull { !userConnections.contains(it) }
        if (nextConnection != null) {
            hintConnection = nextConnection
            hintAnimator?.cancel()
            hintAnimator = ValueAnimator.ofInt(0, 255, 0).apply {
                duration = 1500
                addUpdateListener {
                    hintAlpha = it.animatedValue as Int
                    invalidate()
                }
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        hintConnection = null
                        invalidate()
                    }
                })
            }
            hintAnimator?.start()
        }
    }

    fun startWinAnimation() {
        ValueAnimator.ofArgb(Color.parseColor("#ADD323"), Color.WHITE, Color.parseColor("#ADD323")).apply {
            duration = 1000
            repeatCount = 1
            addUpdateListener {
                linePaint.color = it.animatedValue as Int
                linePaint.strokeWidth = 12f
                invalidate()
            }
            start()
        }
    }

    // --- Внутренние методы ---

    private fun validateTempLine(endX: Float, endY: Float) {
        val endPointStar = findStarAt(endX, endY)
        if (startingStar != null && endPointStar != null && startingStar!!.id != endPointStar.id) {
            tempLineColor = if (isConnectionValid(startingStar!!, endPointStar)) Color.GREEN else Color.RED
        } else {
            tempLineColor = Color.GRAY
        }
    }

    private fun isConnectionValid(startStar: Star, endStar: Star): Boolean {
        val p1 = Point(startStar.x * width, startStar.y * height)
        val p2 = Point(endStar.x * width, endStar.y * height)

        if (currentLevel!!.blackHoles.any { isLineIntersectingCircle(p1, p2, Point(it.x * width, it.y * height), it.radius * width) }) {
            return false
        }

        if (!currentLevel!!.allowLineCrossing) {
            if (userConnections.any {
                    val star3 = findStarById(it.starId1)
                    val star4 = findStarById(it.starId2)
                    star3 != null && star4 != null && linesIntersect(p1, p2, Point(star3.x * width, star3.y * height), Point(star4.x * width, star4.y * height))
                }) {
                return false
            }
        }
        return true
    }

    private fun animateLine(connection: Connection) {
        animatedConnection = connection
        lineAnimator?.cancel()
        lineAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 300
            interpolator = LinearInterpolator()
            addUpdateListener {
                animationProgress = it.animatedValue as Float
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    userConnections.add(connection)
                    animatedConnection = null
                    checkWinCondition()
                    invalidate()
                }
            })
        }
        lineAnimator?.start()
    }

    private fun checkWinCondition() {
        if (currentLevel != null && userConnections.size == currentLevel!!.requiredConnections.size) {
            onLevelCompleteListener?.invoke()
        }
    }

    private fun findStarAt(x: Float, y: Float): Star? = currentLevel?.stars?.firstOrNull { star ->
        val starX = star.x * width; val starY = star.y * height
        sqrt((x - starX).pow(2) + (y - starY).pow(2)) <= touchRadius
    }

    private fun findStarById(id: Int): Star? = currentLevel?.stars?.find { it.id == id }

    // --- Геометрические Хелперы для проверок пересечений ---
    private data class Point(val x: Float, val y: Float)

    private fun linesIntersect(p1: Point, q1: Point, p2: Point, q2: Point): Boolean {
        if (p1 == p2 || p1 == q2 || q1 == p2 || q1 == q2) return false
        fun orientation(p: Point, q: Point, r: Point): Int {
            val value = (q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y)
            return when {
                value == 0f -> 0 // Collinear
                value > 0f -> 1 // Clockwise
                else -> 2 // Counterclockwise
            }
        }
        val o1 = orientation(p1, q1, p2); val o2 = orientation(p1, q1, q2)
        val o3 = orientation(p2, q2, p1); val o4 = orientation(p2, q2, q1)
        return o1 != o2 && o3 != o4
    }

    private fun isLineIntersectingCircle(p1: Point, p2: Point, center: Point, radius: Float): Boolean {
        val dx = p2.x - p1.x; val dy = p2.y - p1.y
        val a = dx * dx + dy * dy
        val b = 2 * (dx * (p1.x - center.x) + dy * (p1.y - center.y))
        val c = (p1.x - center.x).pow(2) + (p1.y - center.y).pow(2) - radius.pow(2)
        var discriminant = b * b - 4 * a * c
        if (discriminant < 0) return false
        discriminant = sqrt(discriminant)
        val t1 = (-b - discriminant) / (2 * a); val t2 = (-b + discriminant) / (2 * a)
        return (t1 in 0.0..1.0) || (t2 in 0.0..1.0)
    }
}