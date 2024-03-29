package org.techtown.comptoir

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val btn_login = findViewById<Button>(R.id.btn_login)
        val btn_join = findViewById<Button>(R.id.btn_join)
        val emailEditText = findViewById<EditText>(R.id.login_id)
        val pwEditText = findViewById<EditText>(R.id.login_pw)

        btn_login.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = pwEditText.text.toString()
            try {
                logIn(email, password)
            } catch (e: Exception) {
                Log.e("LoginActivity", "오류: ${e.message}")
            }
        }

        btn_join.setOnClickListener {
            val intent = Intent(this, JoinActivity::class.java)
            startActivity(intent)
        }
    }

    private fun logIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if(task.isSuccessful) {
                    Toast.makeText(this@LoginActivity, "환영합니다!", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "로그인 실패", Toast.LENGTH_SHORT).show()
                }
            }
    }
}