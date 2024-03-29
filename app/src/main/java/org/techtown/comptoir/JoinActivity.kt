package org.techtown.comptoir

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class JoinActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: FirebaseDatabase
    private lateinit var firestore: FirebaseFirestore

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join)

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val nameEditText = findViewById<EditText>(R.id.join_name)
        val emailEditText = findViewById<EditText>(R.id.join_email)
        val passwordEditText = findViewById<EditText>(R.id.join_pw)
        val passwordCheckEditText = findViewById<EditText>(R.id.join_pw_check)
        val passwordCheckText = findViewById<TextView>(R.id.join_pw_check_text)
        val btnSave = findViewById<Button>(R.id.btn_account_save)
        val btnHome = findViewById<Button>(R.id.btn_back_home)

        passwordCheckEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (passwordEditText.text.toString() == p0.toString()) {
                    passwordCheckText.setText("비밀번호가 일치합니다.")
                } else {
                    passwordCheckText.setText("비밀번호가 일치하지 않습니다.")
                }
            }

            override fun afterTextChanged(p0: Editable?) {
                if (passwordEditText.text.toString() == p0.toString()) {
                    btnSave.isEnabled = true
                } else {
                    btnSave.isEnabled = false
                }
            }
        })


        btnSave.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val name = nameEditText.text.toString()
            val selectedGender = getSelectedGender()

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        saveDataToFirebase(name, email, selectedGender)
                        Toast.makeText(this@JoinActivity, "회원가입 성공", Toast.LENGTH_SHORT).show()
                        moveToLoginActivity()
                    } else {
                        Toast.makeText(this@JoinActivity, "회원가입 실패", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        btnHome.setOnClickListener {
            val intent = Intent(this, HomeFragment::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun getSelectedGender(): String {
        val radioBtnId = findViewById<RadioGroup>(R.id.radio_group_gender)
        val selectedRadioBtnId = radioBtnId.checkedRadioButtonId

        return when (selectedRadioBtnId) {
            R.id.radio_btn_man -> "남자"
            R.id.radio_btn_woman -> "여자"
            else -> ""
        }
    }

    private fun saveDataToFirebase(name: String, email: String, selectedGender: String) {
        val currentUser = auth.currentUser

        currentUser?.let {
            val userId = it.uid
            val userDocument = firestore.collection("users").document(userId)
            userDocument.set(
                mapOf(
                    "selected_gender" to selectedGender,
                    "name" to name,
                    "email" to email
                )
            )
        }
    }

    private fun moveToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}