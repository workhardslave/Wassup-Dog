package com.pyo.safe_guard

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {


    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 파이어베이스 통합 관리 객체 생성
        auth = FirebaseAuth.getInstance()

        sign_Up.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
//            finish()
        }

        loginBtn.setOnClickListener {
            doLogin()
        }

    }

    // 이메일로 로그인
    private fun doLogin() {
        if (sign_Up_Email.text.toString().isEmpty()) {
            sign_Up_Email.error = "이메일을 입력해주세요."
            sign_Up_Email.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(sign_Up_Email.text.toString()).matches()) {
            sign_Up_Email.error = "유효한 이메일을 입력해주세요."
            sign_Up_Email.requestFocus()
            return
        }

        if (sign_Up_Password.text.toString().isEmpty()) {
            sign_Up_Password.error = "패스워드를 입력해주세요."
            sign_Up_Password.requestFocus()
            return
        }

        auth.signInWithEmailAndPassword(sign_Up_Email.text.toString(), sign_Up_Password.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    Toast.makeText(baseContext, "로그인에 실패했습니다." ,Toast.LENGTH_LONG).show()
                    updateUI(null)
                }
            }
    }

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun updateUI(currentUser: FirebaseUser?) {

        if (currentUser == null) {
            //
        } else {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
