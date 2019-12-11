package jp.ac.asojuku.st.chirusapo

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_registration_child.*
import java.util.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import io.realm.Realm
import io.realm.kotlin.where
import jp.ac.asojuku.st.chirusapo.apis.*
import kotlinx.android.synthetic.main.activity_registration_child.child_clothesSize
import kotlinx.android.synthetic.main.activity_registration_child.child_shoesSize
import java.time.LocalDate
import java.time.Period
import java.util.regex.Pattern
import android.widget.TextView
import io.realm.RealmResults


class RegistrationChildActivity : AppCompatActivity() {
    lateinit var realm:Realm

    private var gender = 0
    private var bloodType = 0
    private val calender = Calendar.getInstance()
    private val year = calender.get(Calendar.YEAR)
    private val month = calender.get(Calendar.MONTH)
    private val day = calender.get(Calendar.DAY_OF_MONTH)

    private var AllergyData = arrayListOf<String>()



    private var VaccineTextarray = 0

    private var userIcon:Bitmap? = null

    companion object {
        const val READ_REQUEST_CODE = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration_child)

        realm = Realm.getDefaultInstance()

        val vaccine = realm.where<Vaccine>().findAll()

        supportActionBar?.let {
            it.title = "子供情報登録"
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeButtonEnabled(true)
        }
        // idがdialogButtonのButtonを取得
        val dialogBtn = findViewById<View>(R.id.VaccineData_Add) as Button
        // clickイベント追加
        dialogBtn.setOnClickListener {
            // ダイアログクラスをインスタンス化
            val dialog = VaccineName(vaccine)

            // 表示  getFagmentManager()は固定、sampleは識別タグ
            dialog.show()
        }
        AllergyData_Add.setOnClickListener { Allergy() }
        Child_Icon.setOnClickListener{selectPhoto()}
        Child_Birthday.setOnClickListener { onBirthdaySetting() }
        ChildAdd_Button.setOnClickListener { onChildAdd() }
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    override fun onResume() {
        super.onResume()
        Child_Birthday.isFocusable = false
        //性別選択
        Child_gender.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val spinner = findViewById<Spinner>(R.id.Child_gender)
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
        //血液型選択
        Child_Blood.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val spinner = findViewById<Spinner>(R.id.Child_Blood)
                when (spinner.selectedItem.toString()) {
                    "A型" -> bloodType = 1
                    "B型" -> bloodType = 2
                    "O型" -> bloodType = 3
                    "AB型" -> bloodType = 4
                    "血液型" -> bloodType = 0
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                bloodType = 0
            }
        }
    }


    private fun setTextView(value: String) {
        val Vaccine_Name = findViewById<View>(R.id.vaccine_name) as TextView
        Vaccine_Name.text = value
    }

    private fun onBirthdaySetting(){
        val birthday = findViewById<EditText>(R.id.Child_Birthday)
        DatePickerDialog(this,DatePickerDialog.OnDateSetListener{ _, y, m, d ->
            val year = y.toString()
            var month = (m+1).toString()
            var day = d.toString()
            if(m < 9 ){
                month = "0$month"
            }
            if(d < 10){
                day = "0$day"
            }
            birthday.setText("%s-%s-%s".format(year, month, day))
            //入力された誕生日から誕生日を計算して反映
            val today = LocalDate.now()
            val birthday = LocalDate.parse("%s-%s-%s".format(year, month, day))
            val Age = Period.between(birthday, today).years.toString()
            Child_Age.text = Age
        }, year,month,day
        ).show()
    }


    private fun onChildNameCheck(): Boolean {
        val ChildName = Child_Name.editText?.text.toString().trim()

        return when {
            ChildName.isEmpty() -> {
                Child_Name.error = "名前が入力されていません"
                false
            }
            ChildName.count() < 2 -> {
                Child_Name.error = "名前の文字数が不正です"
                false
            }
            ChildName.count() > 30 -> {
                Child_Name.error = "名前の文字数が不正です"
                false
            }
            else -> {
                Child_Name.error = null
                true
            }
        }
    }

    private fun ChildIDCheck():Boolean{
        val ChildID = Child_Id.editText?.text.toString().trim()

        return when {
            ChildID.isEmpty() -> {
                Child_Id.error = "ユーザーIDが入力されていません"
                false
            }
            ChildID.count() < 5  ->{
                Child_Id.error = "ユーザーIDの文字数が不正です"
                false
            }
            ChildID.count() > 30  ->{
                Child_Id.error = "ユーザーIDの文字数が不正です"
                false
            }
            !Pattern.compile("^[a-zA-Z0-9-_]*\$").matcher(ChildID).find()-> {
                Child_Id.error = "使用できない文字が含まれています"
                false
            }
            else -> {
                Child_Id.error = null
                true
            }
        }
    }

    private fun onChildHeightCheck(): Boolean {
        val ChildHeight = Child_Height.editText?.text.toString().trim()

        return when{
            ChildHeight.isEmpty() -> {
                Child_Height.error = "身長が入力されていません"
                false
            }
            ChildHeight.toDouble() < 10 -> {
                Child_Height.error = "身長は10～200までの間で入力してください"
                false
            }
            ChildHeight.toDouble() > 200 -> {
                Child_Height.error = "身長は10～200までの間で入力してください"
                false
            }
            else -> {
                Child_Height.error = null
                true
            }
        }
    }

    private fun onChildWeightCheck(): Boolean {
        val ChildWeight = Child_Weight.editText?.text.toString().trim()

        return when{
            ChildWeight.isEmpty() -> {
                Child_Weight.error = "体重が入力されていません"
                false
            }
            ChildWeight.toDouble() < 1 -> {
                Child_Weight.error = "体重は1～150までの間で入力してください"
                false
            }
            ChildWeight.toDouble() > 150 -> {
                Child_Weight.error = "体重は1～150までの間で入力してください"
                false
            }
            else -> {
                Child_Weight.error = null
                true
            }
        }
    }

    private fun onChildShoesSizeCheck(): Boolean{
        val ChildShoes = child_shoesSize.editText?.text.toString().trim()

        return when{
            ChildShoes.isEmpty() -> {
                child_shoesSize.error = "靴のサイズが入力されていません"
                false
            }
            ChildShoes.toDouble() < 5 -> {
                child_shoesSize.error = "靴のサイズは5～30までの間で入力してください"
                false
            }
            ChildShoes.toDouble() > 30 -> {
                child_shoesSize.error = "靴のサイズは5～30までの間で入力してください"
                false
            }
            else -> {
                child_shoesSize.error = null
                true
            }
        }
    }

    private fun ChildBirthdayCheck():Boolean {
        val ChildBirthday = Child_Birthday.text.toString().trim()
        return when {
            ChildBirthday.isEmpty() -> {
                Child_Birthday.error = ""
                birthday_error.text = "誕生日が未入力です"
                false
            }
            else -> {
                Child_Birthday.error = null
                birthday_error.text = null
                true
            }
        }
    }

    private fun VaccineName(vaccine: RealmResults<Vaccine>?): Dialog {
        //ワクチン全ての種類数
        val vaccineArray = vaccine!!.size
        val VaccineNameTexts: Array<TextView?> = arrayOfNulls(vaccineArray)
        // ダイアログ生成  AlertDialogのBuilderクラスを指定してインスタンス化します
        val dialogBuilder = AlertDialog.Builder(this)

        // リスト項目生成
        val items = arrayOfNulls<String>(vaccineArray)
        for (i in 0 until vaccineArray) {
            //ダイアログ内のリストにワクチン一覧をセット
            items[i] = vaccine[i]!!.vaccine_name
        }
        // タイトル設定
        dialogBuilder.setTitle("ワクチン")
        // リスト項目を設定 & クリック時の処理を設定
        dialogBuilder.setItems(
            items
        ) { _, which ->
            // whichには選択したリスト項目の順番が入っているので、それを使用して値を取得
            val selectedVal = items[which]
            VaccineNameTexts[VaccineTextarray]!!.setText(selectedVal)
            VaccineDate(vaccine,vaccineArray,VaccineNameTexts)
        }
        // dialogBulderを返す
        return dialogBuilder.create()
    }

    private fun VaccineDate(vaccine: RealmResults<Vaccine>?, vaccineArray: Int, VaccineNameTexts: Array<TextView?>){
        val layoutName: LinearLayout = findViewById(R.id.vaccine_name_array)
        val layoutDate: LinearLayout = findViewById(R.id.vaccine_date_array)
        val VaccineDateTexts: Array<TextView?> = arrayOfNulls(vaccineArray)

        DatePickerDialog(this,DatePickerDialog.OnDateSetListener{ _, y, m, d ->
                                val year = y.toString()
                    var month = (m+1).toString()
                    var day = d.toString()
                    if(m < 9 ){
                        month = "0$month"
                    }
                    if(d < 10){
                        day = "0$day"
                    }
                    VaccineDateTexts[VaccineTextarray]!!.setText("%s-%s-%s".format(year, month, day))
            layoutName.addView(VaccineNameTexts[VaccineTextarray])
            layoutDate.addView(VaccineDateTexts[VaccineTextarray])
            VaccineTextarray = VaccineTextarray + 1
                }, year,month,day
                ).show()
    }
    private fun Allergy(){
        val Allergyadapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, AllergyData)
        AllergyList.adapter = Allergyadapter
        val AllergyAdd = findViewById<Button>(R.id.AllergyData_Add)
        AllergyAdd.setOnClickListener {
            val myedit = EditText(this)
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("アレルギー名を入力してください")
            dialog.setView(myedit)
            dialog.setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
                // OKボタン押したときの処理
                val AllergyText = myedit.text.toString()
                AllergyData.add(AllergyText)
            })
            dialog.setNegativeButton("キャンセル", null)
            dialog.show()
        }
    }

    //画像選択の為にライブラリを開く
    private fun selectPhoto(){
        val ChildIcon = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        startActivityForResult(ChildIcon, READ_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (resultCode != RESULT_OK) {
            return
        }
        when (requestCode) {
            READ_REQUEST_CODE -> {
                try {
                    resultData?.data?.also { uri ->
                        val inputStream = contentResolver?.openInputStream(uri)
//                        Log.i("uri表示", uri.toString())
                        val image = BitmapFactory.decodeStream(inputStream)
                        var imageView = findViewById<ImageView>(R.id.ChildIcon)
                        imageView.setImageBitmap(image)
                        userIcon = image
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "エラーが発生しました", Toast.LENGTH_LONG).show()
                }
            }

        }
    }

    private fun onChildAdd(){
        var check = true
        if(!onChildNameCheck())check = false
        if(!ChildIDCheck())check = false
        if(!onChildHeightCheck())check = false
        if(!onChildWeightCheck())check = false
        if(!onChildShoesSizeCheck())check = false
        if(!ChildBirthdayCheck())check = false

        //服のサイズが未入力の場合(必要?)
        child_clothesSize.onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val spinner = findViewById<Spinner>(R.id.child_clothesSize)
                when (spinner.selectedItem.toString()) {
                    "服のサイズ" -> {
                        Toast.makeText(applicationContext, "服のサイズが未入力です", Toast.LENGTH_SHORT).show()
                        return
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                Toast.makeText(applicationContext, "服のサイズが未入力です", Toast.LENGTH_SHORT).show()
                return
            }
        }
        if(!check)return

        val account: Account? = realm.where<Account>().findFirst()
        val JoinGroup: JoinGroup? = realm.where<JoinGroup>().findFirst()
        val group_id = JoinGroup!!.Rgroup_id
        val token = account!!.Rtoken

        //TODO ワクチン、アレルギー、アイコン情報の追加
        val params = hashMapOf(
            "token" to token,
            "group_id" to group_id,
            "user_name" to Child_Name.editText?.text.toString(),
            "user_id" to Child_Id.editText?.text.toString(),
            "birthday" to Child_Birthday.text.toString().trim(),
            "age" to Child_Age.text.toString().trim(),
            "gender" to gender.toString(),
            "blood_type" to bloodType.toString(),
            "body_height" to Child_Height.editText?.text.toString(),
            "body_weight" to Child_Weight.editText?.text.toString(),
            "clothes_size" to child_clothesSize.toString(),
            "shoes_size" to child_shoesSize.editText?.text.toString()
        )


        val paramImage = arrayListOf<ApiParamImage>()

        val paramItem = ApiParamImage("image/jpg","Child01.jpg","user_icon",userIcon!!)
        paramImage.add(paramItem)

        ApiPostTask{
            if(it == null){
                //応答null
                Toast.makeText(applicationContext, "APIとの通信に失敗しました", Toast.LENGTH_SHORT).show()
            }
            else{
                when(it.getString("status")){
                    "200" -> {
                        // TODO realmにワクチン、アレルギーの登録
                        Toast.makeText(applicationContext, "登録しました", Toast.LENGTH_SHORT).show()
//                        val intent = Intent(this, CheckGrowthActivity::class.java).apply {
//                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
//                        }
//                        startActivity(intent)
                    }
                    "400" -> {
                        val errorArray = it.getJSONArray("message")
                        for (i in 0 until errorArray.length()) {
                            when (errorArray.getString(i)) {
                                ApiError.REQUIRED_PARAM -> {
                                    ApiError.showToast(this,errorArray.getString(i), Toast.LENGTH_LONG)
                                }
                                ApiError.UNKNOWN_TOKEN -> {
                                    ApiError.showToast(this,errorArray.getString(i), Toast.LENGTH_LONG)
                                }
                                ApiError.UNKNOWN_GROUP -> {
                                    ApiError.showToast(this,errorArray.getString(i), Toast.LENGTH_LONG)
                                }
                                ApiError.UNREADY_BELONG_GROUP -> {
                                    ApiError.showToast(this,errorArray.getString(i), Toast.LENGTH_LONG)
                                }
                                ApiError.VALIDATION_USER_ID -> {
                                    ApiError.showToast(this,errorArray.getString(i), Toast.LENGTH_LONG)
                                }
                                ApiError.VALIDATION_USER_NAME -> {
                                    ApiError.showEditTextError(Child_Name,errorArray.getString(i))
                                }
                                ApiError.VALIDATION_BIRTHDAY -> {
                                    ApiError.showToast(this,errorArray.getString(i), Toast.LENGTH_LONG)
                                }
                                ApiError.VALIDATION_AGE -> {
                                    ApiError.showToast(this,errorArray.getString(i), Toast.LENGTH_LONG)
                                }
                                ApiError.VALIDATION_GENDER -> {
                                    ApiError.showToast(this,errorArray.getString(i), Toast.LENGTH_LONG)
                                }
                                ApiError.VALIDATION_BLOOD_TYPE -> {
                                    ApiError.showToast(this,errorArray.getString(i), Toast.LENGTH_LONG)
                                }
                                ApiError.VALIDATION_BODY_HEIGHT -> {
                                    ApiError.showEditTextError(Child_Height,errorArray.getString(i))
                                }
                                ApiError.VALIDATION_BODY_WEIGHT -> {
                                    ApiError.showEditTextError(Child_Weight,errorArray.getString(i))
                                }
                                ApiError.VALIDATION_CLOTHES_SIZE -> {
                                    ApiError.showToast(this,errorArray.getString(i), Toast.LENGTH_LONG)
                                }
                                ApiError.VALIDATION_SHOES_SIZE -> {
                                    ApiError.showEditTextError(child_shoesSize,errorArray.getString(i))
                                }
                                ApiError.VALIDATION_VACCINATION -> {
                                    ApiError.showToast(this,errorArray.getString(i), Toast.LENGTH_LONG)
                                }
                                ApiError.VALIDATION_ALLERGY -> {
                                    ApiError.showToast(this,errorArray.getString(i), Toast.LENGTH_LONG)
                                }
                                ApiError.ALLOW_EXTENSION -> {
                                    ApiError.showToast(this,errorArray.getString(i), Toast.LENGTH_LONG)
                                }
                                ApiError.UPLOAD_FAILED -> {
                                    ApiError.showToast(this,errorArray.getString(i), Toast.LENGTH_LONG)
                                }
                                ApiError.ALREADY_USER_ID -> {
                                    ApiError.showEditTextError(Child_Id,errorArray.getString(i))
                                }
                            }
                        }
                    }
                }
            }
        }.execute(
            ApiParam(
                Api.SLIM + "child/add",
                params,paramImage
            )
        )

    }


}