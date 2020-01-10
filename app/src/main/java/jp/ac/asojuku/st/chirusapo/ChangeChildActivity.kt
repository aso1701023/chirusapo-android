package jp.ac.asojuku.st.chirusapo

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import io.realm.Realm
import io.realm.RealmResults
import io.realm.kotlin.where
import jp.ac.asojuku.st.chirusapo.R.id.vaccine_date_array
import jp.ac.asojuku.st.chirusapo.apis.*
import kotlinx.android.synthetic.main.activity_change_child.*
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class ChangeChildActivity : AppCompatActivity() {
    lateinit var realm: Realm

    private var vaccineArray = 0
    private var allergyArray = 0

    private val calender = Calendar.getInstance()
    private val year = calender.get(Calendar.YEAR)
    private val month = calender.get(Calendar.MONTH)
    private val day = calender.get(Calendar.DAY_OF_MONTH)

    private var VaccineNameTexts = ArrayList<String>(vaccineArray)
    private var VaccineDateTexts = ArrayList<String>(vaccineArray)
    private val AllergyNameTexts = ArrayList<String>(allergyArray)

    private var VaccineTextarray = 0
    private var AllergyTextarray = 0

    private var userIcon: Bitmap? = null
    private val userIconRequestCode = 1000

    private var VaccineFLG = false
    private var AllergyFLG = false
    private var IconFLG = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_child)

        val allergy = realm.where<Allergy>().findAll()
        val vaccine = realm.where<Vaccine>().findAll()

        realm = Realm.getDefaultInstance()
        vaccineArray = vaccine!!.size
        allergyArray = allergy.size

        supportActionBar?.let {
            it.title = "子供情報変更"
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeButtonEnabled(true)
        }

        val VaccineAddBtn = findViewById<View>(R.id.Vaccine_Add) as Button
        val AllergyAddBtn = findViewById<View>(R.id.Allergy_Add) as Button
        // clickイベント追加
        VaccineAddBtn.setOnClickListener {
            // ダイアログクラスをインスタンス化
            val VaccineDialog = VaccineName(vaccine)

            // 表示  getFagmentManager()は固定、sampleは識別タグ
            VaccineDialog.show()
        }
        AllergyAddBtn.setOnClickListener {
            val AllergyDialog = Allergy(allergy)
            AllergyDialog.show()
        }
        ChildIcon.setOnClickListener {
            selectPhoto()
        }
        ChangeButton.setOnClickListener {
            onChildChange()
        }
    }

    override fun onResume() {
        super.onResume()
        
    }

    private fun VaccineName(vaccine: RealmResults<Vaccine>): Dialog {
        val layoutName: LinearLayout = findViewById(R.id.Vaccine_Name_Array)
        val text_vaccineName = TextView(this)
        // ダイアログ生成  AlertDialogのBuilderクラスを指定してインスタンス化します
        val dialogBuilder = AlertDialog.Builder(this)

        // リスト項目生成
        val items = arrayOfNulls<String>(vaccineArray)
        for (i in 0 until vaccineArray) {
            //ダイアログ内のリストにワクチン一覧をセット
            items[i] = vaccine[i]!!.vaccine_name
        }
        // タイトル設定
        dialogBuilder.setTitle("ワクチンを選択してください")
        // リスト項目を設定 & クリック時の処理を設定
        dialogBuilder.setItems(
            items
        ) { _, which ->
            // whichには選択したリスト項目の順番が入っているので、それを使用して値を取得
            val selectedVal = items[which]
            text_vaccineName.text = items[which]
            VaccineNameTexts.add(selectedVal.toString())
            VaccineDate(VaccineNameTexts,which)
            layoutName.addView(text_vaccineName,
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }
        // dialogBulderを返す
        return dialogBuilder.create()
    }


    private fun VaccineDate(VaccineNameTexts: java.util.ArrayList<String>, which: Int) {
        val layoutDate: LinearLayout = findViewById(R.id.Vaccine_Date_Array)
        val text_vaccineDate = TextView(this)
        DatePickerDialog(this, DatePickerDialog.OnDateSetListener{ _, y, m, d ->
            val year = y.toString()
            var month = (m+1).toString()
            var day = d.toString()
            if(m < 9 ){
                month = "0$month"
            }
            if(d < 10){
                day = "0$day"
            }

            VaccineDateTexts.add("%s-%s-%s".format(year, month, day))
            text_vaccineDate.text = "%s-%s-%s".format(year, month, day)
            layoutDate.addView(text_vaccineDate,
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))

            VaccineTextarray += 1
        }, year,month,day
        ).show()
        VaccineFLG = true
    }
    private fun Allergy(allergy: RealmResults<Allergy>): Dialog {
        val layoutAllergy: LinearLayout = findViewById(R.id.Allergy_List)
        val text_allergyName = TextView(this)
        // ダイアログ生成  AlertDialogのBuilderクラスを指定してインスタンス化します
        val dialogBuilder = AlertDialog.Builder(this)

        // リスト項目生成
        val items = arrayOfNulls<String>(allergyArray)
        for (i in 0 until allergyArray) {
            //ダイアログ内のリストにワクチン一覧をセット
            items[i] = allergy[i]!!.allergy_name
        }
        // タイトル設定
        dialogBuilder.setTitle("アレルギーを選択してください")
        // リスト項目を設定 & クリック時の処理を設定
        dialogBuilder.setItems(
            items
        ) { _, which ->
            // whichには選択したリスト項目の順番が入っているので、それを使用して値を取得
            val selectedVal = items[which]
            AllergyNameTexts.add(selectedVal.toString())
            text_allergyName.text = selectedVal
            layoutAllergy.addView(text_allergyName,
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            AllergyTextarray += 1
        }
        AllergyFLG = true
        // dialogBulderを返す
        return dialogBuilder.create()
        
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
        IconFLG = true
        startActivityForResult(intent, userIconRequestCode)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            if (data != null) {
                when (requestCode) {
                    userIconRequestCode -> {
                        val uri = data.data as Uri
                        try {
                            val bitmap: Bitmap = getBitmapFromUri(uri)
                            userIcon = bitmap
                            ChildIcon.apply {
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

    private fun onChildChange(){
//        変更点があるかチェック
        var check = false
        if(VaccineFLG)check = true
        if(AllergyFLG)check = true
        if(IconFLG)check = true
        if (!check){
            Toast.makeText(applicationContext, "変更点がありません", Toast.LENGTH_SHORT).show()
            return
        }

//        TODO ワクチン、アレルギーの配列追加送信処理
        val account: Account? = realm.where<Account>().findFirst()
        val childId = intent.getStringExtra("user_id")
        val token = account!!.Rtoken
        val paramImage = arrayListOf<ApiParamImage>()
        if(userIcon != null){
            val paramItem = ApiParamImage("image/jpg","Child01.jpg","user_icon",userIcon!!)
            paramImage.add(paramItem)
        }
        ApiPostTask{
            if(it == null){
                //応答null
                Toast.makeText(applicationContext, "APIとの通信に失敗しました", Toast.LENGTH_SHORT).show()
            }
            else{
                when(it.getString("status")){
                    "200" -> {
                        Toast.makeText(applicationContext,"変更しました",Toast.LENGTH_SHORT).show()
                    }
                    "400" -> {
                        val errorArray = it.getJSONArray("message")
                        for (i in 0 until errorArray.length()){
                            when (errorArray.getString(i)){
                                ApiError.REQUIRED_PARAM -> {
                                    ApiError.showToast(this,errorArray.getString(i), Toast.LENGTH_LONG)
                                }
                                ApiError.UNKNOWN_TOKEN -> {
                                    ApiError.showToast(this,errorArray.getString(i), Toast.LENGTH_LONG)
                                }
                                ApiError.UNKNOWN_CHILD -> {
                                    ApiError.showToast(this,errorArray.getString(i),Toast.LENGTH_LONG)
                                }
                                ApiError.UNREADY_BELONG_GROUP -> {
                                    ApiError.showToast(this,errorArray.getString(i),Toast.LENGTH_LONG)
                                }
                                ApiError.VALIDATION_VACCINATION -> {
                                    ApiError.showToast(this,errorArray.getString(i),Toast.LENGTH_LONG)
                                }
                                ApiError.VALIDATION_ALLERGY -> {
                                    ApiError.showToast(this,errorArray.getString(i),Toast.LENGTH_LONG)
                                }
                                ApiError.UNAUTHORIZED_OPERATION -> {
                                    ApiError.showToast(this,errorArray.getString(i),Toast.LENGTH_LONG)
                                }
                                ApiError.ALLOW_EXTENSION -> {
                                    ApiError.showToast(this,errorArray.getString(i),Toast.LENGTH_LONG)
                                }
                                ApiError.UPLOAD_FAILED -> {
                                    ApiError.showToast(this,errorArray.getString(i),Toast.LENGTH_LONG)
                                }
                            }
                        }
                    }
                }
            }
        }.execute(
//            TODO 送信するデータとその送信先
        )
    }



}
