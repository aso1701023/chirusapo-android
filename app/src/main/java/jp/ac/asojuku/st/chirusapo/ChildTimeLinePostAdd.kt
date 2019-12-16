package jp.ac.asojuku.st.chirusapo

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import io.realm.kotlin.where
import jp.ac.asojuku.st.chirusapo.apis.Api
import jp.ac.asojuku.st.chirusapo.apis.ApiMediaPostTask
import jp.ac.asojuku.st.chirusapo.apis.ApiParam
import jp.ac.asojuku.st.chirusapo.apis.ApiParamImage
import kotlinx.android.synthetic.main.activity_child_tima_line_post_add.*
import kotlinx.android.synthetic.main.content_main_post_add.*
import java.io.IOException

class ChildTimeLinePostAdd : AppCompatActivity() {

    private lateinit var userToken: String
    private lateinit var group_id: String
    private val resultRequestPickImage01 = 1001
    private val resultRequestPickImage02 = 1002
    private val resultRequestPickImage03 = 1003
    private val resultRequestPickImage04 = 1004
    private var resultPickImage01: Bitmap? = null
    private var resultPickImage02: Bitmap? = null
    private var resultPickImage03: Bitmap? = null
    private var resultPickImage04: Bitmap? = null

    lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child_tima_line_post_add)
        realm = Realm.getDefaultInstance()

        val pref = getSharedPreferences("data", MODE_PRIVATE)

        //Tokenの取得
        val account: Account? = realm.where<Account>().findFirst()
        //Tokenが存在するか？
        if (account == null) {
            // 新規登録orログインが行われていないのでSignInActivityに遷移
            val intent = Intent(this,SignInActivity::class.java)
            startActivity(intent)
        }else {
            val token = account.Rtoken
            //現在見ているグループIDの取得
            val test = 1
            val group:JoinGroup? =
                realm.where<JoinGroup>().equalTo("Rgroup_flag", test).findFirst()
            if (group == null){
                val intent = Intent(this,SignInActivity::class.java)
                startActivity(intent)
            }else{
                userToken = account.Rtoken
                group_id = group.Rgroup_id
            }
        }
        loading_background.visibility = View.GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)

        if (resultCode == RESULT_OK) {
            if (resultData != null) {
                when (requestCode) {
                    resultRequestPickImage01 -> {
                        val uri = resultData.data as Uri
                        try {
                            val bitmap: Bitmap = getBitmapFromUri(uri)
                            resultPickImage01 = bitmap
                            button_image_select_1.setImageBitmap(bitmap)
                            button_image_select_1.scaleType = ImageView.ScaleType.FIT_CENTER
                            button_image_select_2.visibility = View.VISIBLE
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    resultRequestPickImage02 -> {
                        val uri = resultData.data as Uri
                        try {
                            val bitmap: Bitmap = getBitmapFromUri(uri)
                            resultPickImage02 = bitmap
                            button_image_select_2.setImageBitmap(bitmap)
                            button_image_select_2.scaleType = ImageView.ScaleType.FIT_CENTER
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun getBitmapFromUri(uri: Uri): Bitmap {
        val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor = parcelFileDescriptor!!.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        return image
    }

    override fun onResume() {
        super.onResume()

        val childId = intent.getStringExtra("user_id")
        Log.d("TEST", childId)

        button_image_select_1.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, resultRequestPickImage01)
        }

        button_image_select_1.setOnLongClickListener {
            AlertDialog.Builder(this)
                .setMessage("選択を解除しますか？")
                .setPositiveButton("解除") { _, _ ->
                    resultPickImage01 = null
                    button_image_select_1.setImageBitmap(null)
                }
                .setNegativeButton("キャンセル", null)
                .create()
                .show()
            return@setOnLongClickListener true
        }

        button_image_select_2.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, resultRequestPickImage02)
        }

        button_image_select_2.setOnLongClickListener {
            AlertDialog.Builder(this)
                .setMessage("選択を解除しますか？")
                .setPositiveButton("解除") { _, _ ->
                    resultPickImage02 = null
                    button_image_select_2.setImageBitmap(null)
                    button_image_select_2.visibility = View.INVISIBLE
                }
                .setNegativeButton("キャンセル", null)
                .create()
                .show()
            return@setOnLongClickListener true
        }

        button_post_add_children.setOnClickListener { view ->
            if (text_input_post_content.text.isNullOrEmpty() || userToken.isNotEmpty()) {
                loading_background.visibility = View.VISIBLE
                text_input_post_content.visibility = View.INVISIBLE
                image_area.visibility = View.INVISIBLE
                button_post_add_children.visibility= View.INVISIBLE

                val param = hashMapOf(
                    "token" to userToken,
                    "text" to text_input_post_content.text.toString(),
                    "child_id" to childId
                )
                val image = arrayListOf<ApiParamImage>()

                if (resultPickImage01 != null) {
                    image.add(
                        ApiParamImage(
                            "image/jpg",
                            "image01.jpg",
                            "image01",
                            resultPickImage01!!
                        )
                    )

                    if (resultPickImage02 != null) {
                        image.add(
                            ApiParamImage(
                                "image/jpg",
                                "image02.jpg",
                                "image02",
                                resultPickImage02!!
                            )
                        )

                        if (resultPickImage03 != null) {
                            image.add(
                                ApiParamImage(
                                    "image/jpg",
                                    "image03.jpg",
                                    "image03",
                                    resultPickImage03!!
                                )
                            )

                            if (resultPickImage04 != null) {
                                image.add(
                                    ApiParamImage(
                                        "image/jpg",
                                        "image04.jpg",
                                        "image04",
                                        resultPickImage04!!
                                    )
                                )
                            }
                        }
                    }
                }

                ApiMediaPostTask {
                    if (it == null) {
                        Snackbar.make(view, "APIとの通信に失敗しました", Snackbar.LENGTH_SHORT).show()
                    } else {
                        when (it.getString("status")) {
                            "200" -> {
                                finish()
                            }
                            "400" -> {
                                val msgArray = it.getJSONArray("message")
                                for (i in 0 until msgArray.length()) {
                                    when (msgArray.getString(i)) {
                                        "REQUIRED_PARAM" -> Snackbar.make(
                                            view,
                                            "必要な値が見つかりませんでした",
                                            Snackbar.LENGTH_SHORT
                                        ).show()
                                        "UNKNOWN_TOKEN" -> {
                                            Toast.makeText(
                                                this,
                                                "ログイントークンが不明です",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            Intent(
                                                this, SignInActivity::class.java
                                            ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        "POST_CONTENT_LENGTH_OVER" -> Snackbar.make(
                                            view,
                                            "投稿できる最大文字数を超えています",
                                            Snackbar.LENGTH_SHORT
                                        ).show()
                                        else -> Snackbar.make(
                                            view,
                                            "不明なエラーが発生しました!",
                                            Snackbar.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                            else -> Snackbar.make(
                                view,
                                "不明なエラーが発生しました",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }

                    loading_background.visibility = View.GONE
                    text_input_post_content.visibility = View.VISIBLE
                    image_area.visibility = View.VISIBLE
                    button_post_add_children.visibility = View.VISIBLE
                }.execute(
                    ApiParam(
                        //書き換え
                        Api.SLIM + "/child/growth/diary/post",
                        param,
                        image
                    )
                )
            }
        }
    }
}
