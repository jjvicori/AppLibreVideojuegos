package com.example.appkotlin

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID.randomUUID
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.widget.Button
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_insertar.*
import java.io.IOException
import java.lang.Exception
import java.util.*


class InsertarActivity : AppCompatActivity() {

    lateinit var etNombre: EditText
    lateinit var etCategoria: EditText
    lateinit var etDescripcion: EditText
    lateinit var btnInsertar: Button
    lateinit var btnEliminar: Button
    lateinit var imImage: ImageView
    lateinit var imagen: String
    lateinit var uniqueID: String
    private lateinit var database: DatabaseReference
    private val PICK_IMAGE_REQUEST = 71
    private var filePath: Uri? = null
    private var storageReference: StorageReference? = null
    private var withImage: Boolean = false
    private lateinit var videojuego: Videojuego
    private var esModificar: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insertar)
        init()
        var bundle = intent.extras
        if(bundle!=null){
            videojuego = bundle.getParcelable<Videojuego>("VIDEOJUEGO")!!
            esModificar = true
            initModificar()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if(data == null || data.data == null){
                return
            }

            filePath = data.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                imImage.setImageBitmap(bitmap)
                withImage = true
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    fun init(){
        uniqueID =  UUID.randomUUID().toString()
        imagen = "default.png"
        database = FirebaseDatabase.getInstance().reference.child("videojuegos")
        storageReference = FirebaseStorage.getInstance().reference
        etNombre = findViewById(R.id.etNombre)
        etDescripcion = findViewById(R.id.etDescripcion)
        etCategoria = findViewById(R.id.etCategoria)
        btnInsertar = findViewById(R.id.btnInsertar)
        btnEliminar = findViewById(R.id.btnEliminar)
        imImage = findViewById(R.id.ivInsertar)
        btnEliminar.visibility = View.GONE
    }

    fun initModificar(){
        btnInsertar = findViewById(R.id.btnInsertar)
        btnInsertar.setText("Modificar")
        uniqueID =  videojuego.id
        imagen = videojuego.imagen
        etNombre.setText(videojuego.nombre)
        etDescripcion.setText(videojuego.descripcion)
        etCategoria.setText(videojuego.categoria)
        btnEliminar = findViewById(R.id.btnEliminar)
        btnEliminar.visibility = View.VISIBLE
        loadImagen()


    }
    fun loadImagen() {

        val storageReference =
            FirebaseStorage.getInstance().reference.child("videojuegos/${videojuego.imagen}")

        storageReference.downloadUrl.addOnSuccessListener {
            Picasso.with(this).load(it).into(imImage);
        }.addOnFailureListener {
        }


    }
    fun seleccionarImagen(view: View) {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), PICK_IMAGE_REQUEST)

    }

    fun insertar(view: View) {

        if(etNombre.text.isEmpty() || etDescripcion.text.isEmpty() || etCategoria.text.isEmpty()){
            Toast.makeText(this, "Debes de rellenar todos los campos" , Toast.LENGTH_LONG).show()
        }else{


                if(withImage){
                    subirImagen()
                }else{

                    if(esModificar){
                        editarVideojuego()
                    }else{
                        guardarVideojuego()
                    }
                }




        }
    }

    private fun guardarVideojuego(){
        val v = Videojuego(uniqueID,etNombre.text.toString(),etDescripcion.text.toString(),this.imagen, etCategoria.text.toString())
        database.child(uniqueID).setValue(v)
        Toast.makeText(this, "Videojuego insertado" , Toast.LENGTH_LONG).show()

        etNombre.setText("")
        etCategoria.setText("")
        etDescripcion.setText("")

        uniqueID =  UUID.randomUUID().toString()
    }

    private fun editarVideojuego(){

        val v = Videojuego(uniqueID,etNombre.text.toString(),etDescripcion.text.toString(),this.imagen, etCategoria.text.toString())
        database.child(uniqueID).setValue(v).addOnSuccessListener {
            Toast.makeText(this, "Videojuego modificado" , Toast.LENGTH_LONG).show()

        }
            .addOnFailureListener {
                Toast.makeText(this, "Videojuego FAIL" , Toast.LENGTH_LONG).show()

            }




    }
    private fun subirImagen(){
        if(filePath != null){

            imagen= obtenerNombreImagen((filePath as  Uri))
            val ref = storageReference?.child("videojuegos/"+imagen)
            val uploadTask = ref?.putFile(filePath!!)

             uploadTask?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if(esModificar){
                        editarVideojuego()
                    }else{
                        guardarVideojuego()
                    }

                } else {
                    // Handle failures
                }
            }?.addOnFailureListener{

            }
        }else{
            Toast.makeText(this, "Carga una imagen", Toast.LENGTH_SHORT).show()
        }
    }

    fun obtenerNombreImagen(uri: Uri): String {
           var result: String = ""
          if (uri.getScheme().equals("content")) {
            var cursor: Cursor? = contentResolver.query(uri, null,null,null,null)
            try {
              if (cursor != null && cursor.moveToFirst()) {
                result = cursor?.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
              }
            } finally {
              cursor?.close()
            }
          }else{
              result = "default.png";
          }

           return result
     }

    fun eliminar(view: View) {

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Eliminar videojuego")
        builder.setMessage("Estás seguro de eliminar este videojuego?")
        //builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))

        builder.setPositiveButton(android.R.string.yes) { dialog, which ->
            database.child(videojuego.id).removeValue();
            Toast.makeText(applicationContext, "Videojuego Borrado",Toast.LENGTH_LONG).show()
            try {
                Thread.sleep(3000)
                finish()
            }catch (e: Exception){
                Log.i("ERROR","ERROR");
            }

        }

        builder.setNegativeButton(android.R.string.no) { dialog, which ->

        }

        builder.show()
    }
}
