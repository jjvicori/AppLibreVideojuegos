package com.example.appkotlin

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Videojuego (
    val id: String="",
    val nombre: String="",
    val descripcion: String="",
    var imagen: String="",
    var categoria: String=""
): Parcelable