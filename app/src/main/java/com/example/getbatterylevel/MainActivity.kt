package com.example.getbatterylevel

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.BatteryManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.getbatterylevel.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val TAG : String? = MainActivity::class.simpleName
    private var tvBatteryLevel : TextView? =null
    private var bayrak=1
    private lateinit var binding: ActivityMainBinding



    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val prefences = getSharedPreferences("chargeSMSdata", Context.MODE_PRIVATE)
        val editor = prefences.edit()
        binding = ActivityMainBinding.inflate(layoutInflater)

        Toast.makeText(applicationContext,"ŞARJ =  ${prefences.getInt("chargelevel",0)} \n"+
        "NUMARA = ${prefences.getString("smsnumber","DEFAULT_VALUE")}",Toast.LENGTH_LONG).show()

        savenumber.text="Mesaj Gönderilecek Kayıtlı Telefon = "+prefences.getString("smsnumber","DEFAULT_VALUE")
        saveChargeLevel.text="Mesaj Gönderilecek Şarj Seviyesi = "+prefences.getInt("chargelevel",0)


        saveButton.setOnClickListener {
            editor.putInt("chargelevel",inputcharge.text.toString().toInt())
            editor.putString("smsnumber",inputnumber.text.toString())
            editor.apply()

            Toast.makeText(applicationContext,"Kayıt Başarılı...",Toast.LENGTH_SHORT).show()
            val intent = intent
            finish()
            startActivity(intent)
        }



        if (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECEIVE_SMS,android.Manifest.permission.SEND_SMS),111)
        }else{
            receiveMag()
        }
        tvBatteryLevel = findViewById(R.id.tv_battery)


        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batteryBroadCastReceiver,intentFilter)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode==111 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
            receiveMag()
    }

    private fun receiveMag() {

    }

    private val batteryBroadCastReceiver : BroadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {

            val prefences = getSharedPreferences("chargeSMSdata", Context.MODE_PRIVATE)
            val editor = prefences.edit()
            if (intent?.action == "android.intent.action.BATTERY_CHANGED"){
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL,-1)
                Log.d(TAG,"onReceive: batery level $level")

                tvBatteryLevel?.post{
                    tvBatteryLevel!!.text = level.toString()
                }

                if (level.toInt()==prefences.getInt("chargelevel",0)&& bayrak==1){
                    sendSMS(prefences.getString("smsnumber","DEFAULT_VALUE").toString(),"Kalan Şarjım = "+level.toString()+" Telefonum Kapanabilir")
                    bayrak=0
                }else{
                    bayrak=1
                }

            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(batteryBroadCastReceiver)
    }

    private fun sendSMS(phoneNumber: String, message: String) {
        val sentPI: PendingIntent = PendingIntent.getBroadcast(this, 0, Intent("SMS_SENT"), 0)
        SmsManager.getDefault().sendTextMessage(phoneNumber, null, message, sentPI, null)
    }





}