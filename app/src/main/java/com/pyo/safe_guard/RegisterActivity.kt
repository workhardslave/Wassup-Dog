package com.pyo.safe_guard

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pyo.safe_guard.navigation.model.User
import kotlinx.android.synthetic.main.activity_register.*


class RegisterActivity : AppCompatActivity() {

    lateinit var auth : FirebaseAuth
    lateinit var firestore : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        register_button_register.setOnClickListener {
            signUpUser()
        }

        already_have_account_text_view.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

    }

    // 이메일 회원가입
    private fun signUpUser() {
        if (email_edittext_register.text.toString().isEmpty()) {
            email_edittext_register.error = "이메일을 입력해주세요."
            email_edittext_register.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email_edittext_register.text.toString()).matches()) {
            email_edittext_register.error = "유효한 이메일을 입력해주세요."
            email_edittext_register.requestFocus()
            return
        }

        if (password_edittext_register.text.toString().isEmpty()) {
            password_edittext_register.error = "패스워드를 입력해주세요."
            password_edittext_register.requestFocus()
            return
        }

        if (username_edittext_register.text.toString().isEmpty()) {
            username_edittext_register.error = "이름을 입력해주세요."
            username_edittext_register.requestFocus()
            return
        }

//        if (check_pw.text.toString().isEmpty()) {
//            register_pw.error = "패스워드 확인을 입력해주세요."
//            register_pw.requestFocus()
//            return
//        }

//        if(register_pw.text.toString() != check_pw.text.toString()) {
//            Toast.makeText(this, "패스워드와 패스워드 확인이 일치하지 않습니다.", Toast.LENGTH_LONG).show()
//            return
//        }

        auth.createUserWithEmailAndPassword(email_edittext_register.text.toString(), password_edittext_register.text.toString())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    saveUserToFirebaseFiretore()


                } else {
                    // 에러 메세지 출력
                    Toast.makeText(baseContext, "회원인증에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // 파이어스토어에 유저 정보 저장
    private fun saveUserToFirebaseFiretore() {
        val uid = FirebaseAuth.getInstance().uid ?: ""

        val db = FirebaseFirestore.getInstance().collection("users")

        val user = User(uid, username_edittext_register.text.toString())

        db.document(uid)
            .set(user)
            .addOnSuccessListener {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
            }
    }

}







