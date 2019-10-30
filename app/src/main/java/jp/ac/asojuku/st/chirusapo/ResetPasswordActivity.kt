package jp.ac.asojuku.st.chirusapo

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_reset_password.*
import android.content.Intent
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import jp.ac.asojuku.st.chirusapo.apis.ApiError
import jp.ac.asojuku.st.chirusapo.apis.ApiParam
import jp.ac.asojuku.st.chirusapo.apis.ApiPostTask


class ResetPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)
    }

    override fun onResume() {
        super.onResume()

        PasswordReset_Button.setOnClickListener { onPasswordReset() }
    }

    private fun onNewPasswordCheck():Boolean{
        val userNewPass = new_password.editText?.text.toString().trim()
        return when {
            userNewPass.count() < 2 -> {
                new_password.error = "新しいパスワードの文字数が不正です"
                false
            }
            userNewPass.count() > 30 -> {
                new_password.error = "新しいパスワードの文字数が不正です"
                false
            }
            userNewPass.equals("\"^[0-9a-zA-Z]+\$") -> {
                new_password.error = null
                true
            }
            else -> {
                new_password.error = "使用できない文字が含まれています"
                false
            }
        }
    }

    private fun onPasswordReset(){
        var check = true
        if(!onNewPasswordCheck())check = false

        if(!check) return

        ApiPostTask{
            if(it == null){
                Toast.makeText(applicationContext, "APIとの通信に失敗しました", Toast.LENGTH_SHORT).show()
            }
            else {
                when(it.getString("status")){
                    "200" -> {
                        startActivity(
                            Intent(
                                this, MainActivity::class.java
                            )
                        )
                    }
                    "400" -> {
                        val errorArray = it.getJSONArray("message")
                        for (i in 0 until errorArray.length()) {
                            when (errorArray.getString(i)) {
                                ApiError.REQUIRED_PARAM -> {
                                    ApiError.showToast(this,errorArray.getString(i),LENGTH_LONG)
                                }
                                ApiError.VALIDATION_OLD_PASSWORD -> {
                                    ApiError.showEditTextError(old_password,errorArray.getString(i))
                                }
                                ApiError.VALIDATION_NEW_PASSWORD -> {
                                    ApiError.showEditTextError(new_password,errorArray.getString(i))
                                }
                                ApiError.UNKNOWN_TOKEN -> {
                                    ApiError.showToast(this,errorArray.getString(i),LENGTH_LONG)
                                }
                                ApiError.VERIFY_PASSWORD_FAILED -> {
                                    ApiError.showToast(this,errorArray.getString(i),LENGTH_LONG)
                                }
                            }
                        }
                    }
                }
            }
        }.execute(
            ApiParam("account/password-change")
        )
    }
}
