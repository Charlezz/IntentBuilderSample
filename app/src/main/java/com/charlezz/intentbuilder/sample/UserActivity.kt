package com.charlezz.intentbuilder.sample

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.charlezz.intentbuilder.IntentExtraInjector
import com.charlezz.intentbuilder.annotation.Extra
import com.charlezz.intentbuilder.annotation.IntentBuilder
import com.charlezz.intentbuilder.sample.R


@IntentBuilder
class UserActivity : AppCompatActivity() {

    @Extra
    var name: String? = null

    @Extra
    var age: Int = 0

    var test:String = "hello world"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        this.name = intent.getStringExtra("name")
//        this.age = intent.getIntExtra("age",0)
        IntentExtraInjector.inject(this)

        setContentView(R.layout.activity_user)

        findViewById<TextView>(R.id.name).text = name
        findViewById<TextView>(R.id.age).text = age.toString()
    }
}