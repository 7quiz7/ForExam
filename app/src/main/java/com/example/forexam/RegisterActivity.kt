package com.example.forexam

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // инициализация базы данных
        database = AppDatabase.getDatabase(this)

        // обработка  кнопки зарегатся
        findViewById<Button>(R.id.btnRegister).setOnClickListener {
            val username = findViewById<EditText>(R.id.etUsername).text.toString()
            val password = findViewById<EditText>(R.id.etPassword).text.toString()

            if (username.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Поля не должны быть пустыми", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 8) {
                Toast.makeText(this, "Пароль должен быть не менее 8 символов", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // проверка на существование пользователя с таким именем
            lifecycleScope.launch {
                try {
                    val existingUser = database.userDao().getUserByUsername(username)
                    if (existingUser != null) {
                        Toast.makeText(this@RegisterActivity, "Пользователь с таким именем уже существует", Toast.LENGTH_SHORT).show()
                    } else {
                        // Создаем нового пользователя с нулевым счетом и уровнем 1
                        val newUser = User(username = username, password = password, score = 0, currentLevel = 1)
                        database.userDao().insert(newUser)
                        Toast.makeText(this@RegisterActivity, "Регистрация успешна!", Toast.LENGTH_SHORT).show()

                        // Переход на экран входа
                        startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                        finish()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@RegisterActivity, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
