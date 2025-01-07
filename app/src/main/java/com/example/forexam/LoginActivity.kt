package com.example.forexam

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        database = AppDatabase.getDatabase(this)

        usernameEditText = findViewById(R.id.etUsername)
        passwordEditText = findViewById(R.id.etPassword)
        loginButton = findViewById(R.id.btnLogin)

        // кнопка войти
        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (username.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Поля не должны быть пустыми", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // проверяю пользователей в бд
            lifecycleScope.launch {
                try {
                    val user = database.userDao().authenticateUser(username, password)
                    if (user != null) {
                        Toast.makeText(this@LoginActivity, "Добро пожаловать, ${user.username}!", Toast.LENGTH_SHORT).show()

                        // скрываю  поля ввода и кнопку
                        hideLoginFields()

                        // переход на экран игры
                        val intent = Intent(this@LoginActivity, PuzzleGameActivity::class.java)
                        intent.putExtra("USER_ID", user.id) // Передаем userId
                        startActivity(intent)  // Переход в PuzzleGameActivity
                        finish()  // Закрытие экрана входа
                    } else {
                        Toast.makeText(this@LoginActivity, "Неверное имя пользователя или пароль", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // переход на экран регистрации
        findViewById<Button>(R.id.btnGoToRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    // метод для скрытия полей ввода и кнопки
    private fun hideLoginFields() {
        usernameEditText.visibility = EditText.GONE
        passwordEditText.visibility = EditText.GONE
        loginButton.visibility = Button.GONE
    }
}
