package com.charlezz.intentbuilder.sample

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.submit).setOnClickListener {

            val name = findViewById<TextInputEditText>(R.id.input_name).text.toString()
            val age = findViewById<TextInputEditText>(R.id.input_age).text.toString().toIntOrNull()?:0

//            startActivity(Intent().apply {
//                putExtra("name",name)
//                putExtra("age",age)
//            })

            // UserActivityBuilder를 이용한 인텐트 만들기
            val intent:Intent = UserActivityBuilder(
                this,
                name,
                age
            ).build()

            // UserActivity 액티비티 호출
            startActivity(intent)
        }

    }
}