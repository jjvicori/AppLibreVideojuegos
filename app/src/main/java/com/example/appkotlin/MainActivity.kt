package com.example.appkotlin

import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import kotlinx.android.synthetic.main.activity_main.*
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.*
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private var videojuegos: ArrayList<Videojuego?> = ArrayList()
      val EXTRA = "VJ"
    private lateinit var adapter: AdapterVideojuego

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)


        database = FirebaseDatabase.getInstance().reference.child("videojuegos")
/*

*/
        cargarDatosFirebase()

        fab.setOnClickListener { view ->
            val intent = Intent(applicationContext, InsertarActivity::class.java)
            startActivity(intent)


        }
    }
    fun cargarDatosFirebase(){

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                videojuegos.clear()
                for (videojuegoSnapshot in dataSnapshot.children) {
                    val videojuego = videojuegoSnapshot.getValue(Videojuego::class.java)
                    videojuegos.add(videojuego)
                }
                loadRecyclerView()

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("aa", "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        })

    }
   fun loadRecyclerView(){
       var recycler: RecyclerView = findViewById(R.id.rvVideojuego)
       recycler.setHasFixedSize(true)
       recycler.layoutManager =  LinearLayoutManager(this)
       adapter = AdapterVideojuego( this,videojuegos) { videojuego ->
           editar(videojuego)
       }
       recycler.adapter = adapter



   }

    private fun editar(videojuego: Videojuego){
        val intent = Intent(applicationContext, InsertarActivity::class.java)
        intent.putExtra("VIDEOJUEGO",videojuego)
        startActivity(intent)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
