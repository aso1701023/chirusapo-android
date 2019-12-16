package jp.ac.asojuku.st.chirusapo

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_listof_friend.*
import java.io.IOException

class ListofFriendActivity : AppCompatActivity() {
    lateinit var realm:Realm

    private var gender = 0

    private var Friend_Icon:Bitmap? = null
    private val friendIconRequestCode = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listof_friend)

        realm = Realm.getDefaultInstance()
        Friend_Add.setOnClickListener { onFriendAdd() }
    }

    override fun onResume() {
        super.onResume()
        friend_gender.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val spinner = findViewById<Spinner>(R.id.friend_gender)
                when (spinner.selectedItem.toString()) {
                    "男性" -> gender = 1
                    "女性" -> gender = 2
                    "性別" -> gender = 0
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                gender = 0
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
    @Throws(IOException::class)
    private fun getBitmapFromUri(uri: Uri): Bitmap {
        val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor = parcelFileDescriptor!!.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        return image
    }


    //画像選択の為にライブラリを開く
    private fun selectPhoto(){
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(intent,friendIconRequestCode)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            if (data != null) {
                when (requestCode) {
                    friendIconRequestCode -> {
                        val uri = data.data as Uri
                        try {
                            val bitmap: Bitmap = getBitmapFromUri(uri)
                            Friend_Icon = bitmap
                            imageView.apply {
                                setImageBitmap(bitmap)
                                scaleType = ImageView.ScaleType.FIT_CENTER
                            }
                        } catch (e: IOException) {
                            Toast.makeText(this, "画像を取得できませんでした", Toast.LENGTH_SHORT).show()
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    private fun onFriendAdd(){}
}
