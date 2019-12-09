package jp.ac.asojuku.st.chirusapo

import android.graphics.Color
import android.graphics.ColorSpace
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import io.realm.Realm
import io.realm.RealmResults
import io.realm.kotlin.where
import jp.ac.asojuku.st.chirusapo.apis.Api
import jp.ac.asojuku.st.chirusapo.apis.ApiError
import jp.ac.asojuku.st.chirusapo.apis.ApiParam
import jp.ac.asojuku.st.chirusapo.apis.ApiPostTask
import kotlinx.android.synthetic.main.activity_check_growth.*
import kotlinx.android.synthetic.main.layout_group_join.*
import org.spongycastle.asn1.x500.style.RFC4519Style.description

class CheckGrowthActivity : AppCompatActivity(), OnChartValueSelectedListener {
    //Typefaceの設定、後ほど使用します。
    private var mTypeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
    private val chartDataCount = 20
    lateinit var realm:Realm


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        realm = Realm.getDefaultInstance()



//        //子供追加ボタンのリスト表示
//        val listChild:ListView = findViewById()

        setupLineChart()
        lineChart.data = lineDataWithCount(chartDataCount, 100f)
    }

    override fun onResume() {
        super.onResume()

        //トークンとグループidを送って子供情報を取得する
        onTokenSend()

        //子供リストをListViewにセット
        val ChildListView = findViewById<ListView>(R.id.ChildList)


        //アレルギー情報をListViewにセット
        val AllergylistView = findViewById<ListView>(R.id.AllergyDataList)
        val Allergy = realm.where<Allergy>().findAll()
        var AllergyData: MutableList<String?> = listOf(Allergy[0]!!.allergy_name).toMutableList()
        for(i in 1 until Allergy.size){
            AllergyData.add(Allergy[i]!!.allergy_name)
        }
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,AllergyData)
        AllergylistView.adapter = adapter

        //子供リストとなりの+ボタンで子供追加(画面遷移の為、下の処理は仮)
        ChildAdd.setOnClickListener { onChildAdd() }

        //情報表示する子供を変更
//        ChildList.setOnItemClickListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    private fun setupLineChart(){
        lineChart.apply {
            setOnChartValueSelectedListener(this@CheckGrowthActivity)
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            isScaleXEnabled = true
            setPinchZoom(false)
            setDrawGridBackground(false)

            //データラベルの表示
            legend.apply {
                form = Legend.LegendForm.LINE
                typeface = mTypeface
                textSize = 11f
                textColor = Color.BLACK
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
            }

            //y軸右側の表示
            axisRight.isEnabled = false

            //X軸表示
            xAxis.apply {
                typeface = mTypeface
                setDrawLabels(false)
                setDrawGridLines(true)
            }

            //y軸左側の表示
            axisLeft.apply {
                typeface = mTypeface
                textColor = Color.BLACK
                setDrawGridLines(true)
            }
        }
    }

    override fun onValueSelected(e: Entry, h: Highlight) {
        Log.i("Entry selected", e.toString())
    }

    override fun onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.")
    }

    //    LineChart用のデータ作成
    private fun lineDataWithCount(count: Int, range: Float):LineData {

        val values = mutableListOf<Entry>()

        for (i in 0 until count) {
            val value = (Math.random() * (range)).toFloat()
            values.add(Entry(i.toFloat(), value))
        }
        // create a dataset and give it a type
        val yVals = LineDataSet(values, "SampleLineData").apply {
            axisDependency =  YAxis.AxisDependency.LEFT
            color = Color.BLACK
            highLightColor = Color.YELLOW
            setDrawCircles(false)
            setDrawCircleHole(false)
            setDrawValues(false)
            lineWidth = 2f
        }
        val data = LineData(yVals)
        data.setValueTextColor(Color.BLACK)
        data.setValueTextSize(9f)
        return data
    }

    private fun onTokenSend(){
        val account: Account? = realm.where<Account>().findFirst()
        val JoinGroup: JoinGroup? = realm.where<JoinGroup>().findFirst()
        val id = account!!.Ruser_id
        val group_id = JoinGroup!!.Rgroup_id
        ApiPostTask{
            if(it == null){
                Toast.makeText(applicationContext, "APIとの通信に失敗しました", Toast.LENGTH_SHORT).show()
            }
            else{
                when(it.getString("status")){
                    "200" -> {
                        val ChildID = it.getJSONObject("data").getJSONObject("timeline_data").getString("user_id")
                        val ChildName = it.getJSONObject("data").getJSONObject("timeline_data").getString("user_name")
                        val ChildBirthday = it.getJSONObject("data").getJSONObject("timeline_data").getString("user_birthday")
                        val ChildGender = it.getJSONObject("data").getJSONObject("timeline_data").getString("user_gender")
                        val ChildBloodtype = it.getJSONObject("data").getJSONObject("timeline_data").getString("blood_type")
                        val ChildIcon = it.getJSONObject("data").getJSONObject("timeline_data").getString("user_icon")
                        val ChildBodyHeight = it.getJSONObject("data").getJSONObject("timeline_data").getString("body_height")
                        val ChildBodyWeight = it.getJSONObject("data").getJSONObject("timeline_data").getString("body_weight")
                        val ChildClothesSize = it.getJSONObject("data").getJSONObject("timeline_data").getString("clothes_size")
                        val ChildShoesSize = it.getJSONObject("data").getJSONObject("timeline_data").getString("shoes_size")
                        val ChildVaccine = it.getJSONObject("data").getJSONObject("timeline_data").getJSONArray("vaccination").getString(3)
                        val ChildAllergy = it.getJSONObject("data").getJSONObject("timeline_data").getJSONArray("allergy").getString(2)

                        //取得したデータを画面に反映
                        child_name.text = ChildName
                        child_birthday.text = ChildBirthday
                        child_blood.text = ChildBloodtype
                        child_gender.text = ChildGender
                        child_height.text = ChildBodyHeight
                        child_weight.text = ChildBodyWeight
                        child_clothesSize.text = ChildClothesSize
                        child_shoesSize.text = ChildShoesSize

//                        val Allergy = realm.where<Allergy>().findFirst()
//                        val allergy_name = Allergy!!.allergy_name
//                        val AllergyData = listOf(allergy_name)

//                        //アレルギーのリスト表示
//                        val listAllergy: ListView = findViewById(R.id.AllergyData)
//                        val AllergyArray = arrayOf(Allergy.toString())
//                        val AllergyAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, AllergyArray)
//                        listAllergy.adapter = AllergyAdapter

                    }
                    "400" -> {
                        val errorArray = it.getJSONArray("message")
                        for(i in 0 until errorArray.length()){
                            when(errorArray.getString(i)){
                                ApiError.REQUIRED_PARAM -> {
                                    ApiError.showToast(this,errorArray.getString(i),LENGTH_LONG)
                                }
                                ApiError.UNKNOWN_TOKEN -> {
                                    ApiError.showToast(this,errorArray.getString(i),LENGTH_LONG)
                                }
                                ApiError.UNKNOWN_GROUP -> {
                                    ApiError.showToast(this,errorArray.getString(i),LENGTH_LONG)
                                }
                                ApiError.UNREADY_BELONG_GROUP -> {
                                    ApiError.showToast(this,errorArray.getString(i),LENGTH_LONG)
                                }
                            }
                        }
                    }
                }
            }
        }.execute(
            ApiParam(
                Api.SLIM + "child/list",
                hashMapOf("token" to id,"group_id" to group_id)
            )
        )
    }

    private fun onChildAdd() {

    }




}
