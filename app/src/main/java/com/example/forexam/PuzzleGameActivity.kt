package com.example.forexam

import android.content.Intent
import android.os.Bundle
import android.view.DragEvent
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.EditText

class PuzzleGameActivity : AppCompatActivity() {

    private var userId: Int = 0
    private lateinit var database: AppDatabase
    private lateinit var scoreTextView: TextView
    private lateinit var gridLayout: GridLayout
    private var score = 0
    private lateinit var puzzlePieces: MutableList<ImageView>
    private lateinit var puzzleImages: MutableList<Bitmap>
    private lateinit var correctImages: MutableList<Bitmap>
    private var currentLevel = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_puzzle_game)

        //  получаем userId, если оно передано в Intent
        userId = intent.getIntExtra("USER_ID", 0)

        // если userId = 0, значит пользователь не авторизован
        if (userId == 0) {
            // переход на экран входа, если пользователь не авторизован
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()  // закрытие экрана игры
        }

        // инициализация базы данных
        database = AppDatabase.getDatabase(this)

        scoreTextView = findViewById(R.id.tvScore)
        gridLayout = findViewById(R.id.gridLayout)

        // скрываем элементы для логина, если пользователь авторизован
        findViewById<EditText>(R.id.etUsername).visibility = View.GONE
        findViewById<EditText>(R.id.etPassword).visibility = View.GONE
        findViewById<Button>(R.id.btnLogin).visibility = View.GONE

        findViewById<GridLayout>(R.id.gridLayout).visibility = View.VISIBLE

        loadUserData()

        // обработчик кнопки завершить игру
        findViewById<Button>(R.id.btnFinishGame).setOnClickListener {
            checkPuzzleAndProceed()
        }

        // обработчик кнопки выйти
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            logout()
        }
    }

    // метод для выхода из аккаунта
    private fun logout() {
        // делаю userId нулем и возврат на экран логина
        userId = 0
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()  // Закрытие текущего экрана
    }


    // загрузка данных пользователя (счет и уровень)
    private fun loadUserData() {
        lifecycleScope.launch {
            val user = database.userDao().getUserById(userId)
            user?.let {
                score = it.score
                currentLevel = it.currentLevel
                scoreTextView.text = "Счет: $score"
                startLevel(currentLevel)  // начинаем игру с уровня, на котором остановился пользователь
            }
        }
    }

    // запуск уровня игры
    private fun startLevel(level: Int) {
        when (level) {
            in 1..3 -> updateGridLayout(3, 3)
            in 4..6 -> updateGridLayout(4, 4)
            in 7..9 -> updateGridLayout(5, 5)
            else -> updateGridLayout(6, 6)
        }

        val puzzleBitmap = when {
            score < 10 -> BitmapFactory.decodeResource(resources, R.drawable.puzzle_piece_1)
            score < 20 -> BitmapFactory.decodeResource(resources, R.drawable.puzzle_piece_2)
            score < 30 -> BitmapFactory.decodeResource(resources, R.drawable.puzzle_piece_3)
            score < 40 -> BitmapFactory.decodeResource(resources, R.drawable.puzzle_piece_4)
            score < 50 -> BitmapFactory.decodeResource(resources, R.drawable.puzzle_piece_5)
            score < 60 -> BitmapFactory.decodeResource(resources, R.drawable.puzzle_piece_6)
            score < 70 -> BitmapFactory.decodeResource(resources, R.drawable.puzzle_piece_7)
            score < 80 -> BitmapFactory.decodeResource(resources, R.drawable.puzzle_piece_8)
            score < 90 -> BitmapFactory.decodeResource(resources, R.drawable.puzzle_piece_9)
            else -> BitmapFactory.decodeResource(resources, R.drawable.puzzle_piece_10)
        }


        divideImage(puzzleBitmap)
        setupPuzzlePieces()
    }

    private fun updateGridLayout(rowCount: Int, columnCount: Int) {
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        val padding = (16 * displayMetrics.density).toInt() * 2 // 16dp в пикселях
        val availableWidth = screenWidth - padding
        val availableHeight = screenHeight - padding

        val scaleFactor = 0.95f

        val pieceWidth = (availableWidth / columnCount * scaleFactor).toInt()
        val pieceHeight = (availableHeight / rowCount * scaleFactor).toInt()
        val pieceSize = minOf(pieceWidth, pieceHeight)

        gridLayout.rowCount = rowCount
        gridLayout.columnCount = columnCount
        gridLayout.removeAllViews()

        puzzlePieces = mutableListOf()

        val totalPieces = rowCount * columnCount
        for (i in 0 until totalPieces) {
            val imageView = ImageView(this).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = pieceSize
                    height = pieceSize
                    setMargins(4, 4, 4, 4) // Отступы между элементами
                }
            }
            gridLayout.addView(imageView)
            puzzlePieces.add(imageView)
        }
    }




    private fun divideImage(puzzleBitmap: Bitmap) {
        puzzleImages = mutableListOf()
        correctImages = mutableListOf()

        val rows = gridLayout.rowCount
        val cols = gridLayout.columnCount
        val pieceWidth = puzzleBitmap.width / cols
        val pieceHeight = puzzleBitmap.height / rows

        for (i in 0 until rows) {
            for (j in 0 until cols) {
                val x = j * pieceWidth
                val y = i * pieceHeight
                val piece = Bitmap.createBitmap(puzzleBitmap, x, y, pieceWidth, pieceHeight)
                puzzleImages.add(piece)
            }
        }
        correctImages.addAll(puzzleImages)
    }


    // настройка пазлов (отображение и перетаскивание)
    private fun setupPuzzlePieces() {
        puzzleImages.shuffle()

        for (i in puzzlePieces.indices) {
            puzzlePieces[i].setImageBitmap(puzzleImages[i])
            puzzlePieces[i].setOnLongClickListener { view ->
                val shadowBuilder = View.DragShadowBuilder(view)
                view.startDragAndDrop(null, shadowBuilder, view, 0)
                true
            }

            puzzlePieces[i].setOnDragListener { v, event ->
                when (event.action) {
                    DragEvent.ACTION_DROP -> {
                        val droppedView = event.localState as View
                        val newIndex = puzzlePieces.indexOf(v)
                        val oldIndex = puzzlePieces.indexOf(droppedView)

                        if (oldIndex != newIndex) {
                            val temp = puzzleImages[oldIndex]
                            puzzleImages[oldIndex] = puzzleImages[newIndex]
                            puzzleImages[newIndex] = temp
                            updatePuzzleViews()
                        }
                    }
                }
                true
            }
        }
    }

    // обновление изображений пазлов после перетаскивания
    private fun updatePuzzleViews() {
        for (i in puzzlePieces.indices) {
            puzzlePieces[i].setImageBitmap(puzzleImages[i])
        }
    }

    // проверка правильности пазла и переход к следующему уровню
    private fun checkPuzzleAndProceed() {
        if (puzzleImages == correctImages) {
            score += 10
            scoreTextView.text = "Счет: $score"

            lifecycleScope.launch {
                val user = database.userDao().getUserById(userId)
                user?.let {
                    val updatedUser = it.copy(score = score, currentLevel = currentLevel)
                    database.userDao().updateUser(updatedUser)
                }
            }

            currentLevel++
            if (currentLevel <= 10) {
                startLevel(currentLevel)
            } else {
                scoreTextView.text = "Поздравляем, вы прошли игру!"
            }
        } else {
            scoreTextView.text = "Неверно, попробуйте снова!"
        }
    }
}
