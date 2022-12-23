package com.example.HangiBitki

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.HangiBitki.model.UserModelClass
import java.io.IOException
import java.nio.charset.Charset
import kotlinx.android.synthetic.main.activity_next.rvUsersList
import org.json.JSONException
import org.json.JSONObject


class NextActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_next)

        // Instance of users list using the data model class.
        val usersList: ArrayList<UserModelClass> = ArrayList()
        val bitkiId = intent.getStringExtra("BITKI_ID")

        val geriButton = findViewById<Button>(R.id.geri_btn)
        geriButton.setOnClickListener {
            val intent2 = Intent(this, MainActivity::class.java)
            startActivity(intent2)
        }


        try {
            // As we have JSON object, so we are getting the object
            //Here we are calling a Method which is returning the JSON object
            val obj = JSONObject(getJSONFromAssets()!!)
            // fetch JSONArray named users by using getJSONArray
            val usersArray = obj.getJSONArray("bitkiler")
            // Get the users data using for loop i.e. id, name, email and so on

            for (i in 0 until usersArray.length()) {
                // Create a JSONObject for fetching single User's Data
                val flower = usersArray.getJSONObject(i)

                // Fetch id store it in variable
                val id = flower.getString("id")

                val bitkiAdi = flower.getString("bitkiAdi")
                val sicaklik = flower.getString("sicaklik")
                val sulama = flower.getString("sulama")
                val toprakTuru = flower.getString("toprakTuru")
                val gunesIsigi = flower.getString("gunesIsigi")
                val saksiDegisim = flower.getString("saksiDegisim")
                val destekBesin = flower.getString("destekBesin")
                if (bitkiAdi == bitkiId) {
                    // Now add all the variables to the data model class and the data model class to the array list.
                    val userDetails = UserModelClass(
                        id = id,
                        bitkiAdi = bitkiAdi,
                        sicaklik = sicaklik,
                        sulama = sulama,
                        toprakTuru = toprakTuru,
                        gunesIsigi = gunesIsigi,
                        saksiDegisim = saksiDegisim,
                        destekBesin = destekBesin
                    )
                    // add the details in the list
                    usersList.add(userDetails)
                }

            }
        } catch (e: JSONException) {
            //exception
            e.printStackTrace()
        }

        // Set the LayoutManager that this RecyclerView will use.
        rvUsersList.layoutManager = LinearLayoutManager(this)
        // Adapter class is initialized and list is passed in the param.
        val itemAdapter = UserAdapter(this, usersList)
        // adapter instance is set to the recyclerview to inflate the items.
        rvUsersList.adapter = itemAdapter

    }


    /**
     * Method to load the JSON from the Assets file and return the object
     */
    fun getJSONFromAssets(): String? {

        var json: String? = null
        val charset: Charset = Charsets.UTF_8
        try {
            val myUsersJSONFile = assets.open("Users.json")
            val size = myUsersJSONFile.available()
            val buffer = ByteArray(size)
            myUsersJSONFile.read(buffer)
            myUsersJSONFile.close()
            json = String(buffer, charset)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
        return json
    }


}
