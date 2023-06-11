package com.example.objectdetection

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DAO {
    val fDatabase = FirebaseFirestore.getInstance()
    val userColl = fDatabase.collection("users")
    fun add(user: User?, context: Context) {
        if (user != null) {
            GlobalScope.launch {
                userColl.document(user.uid).set(user)
            }
        }
    }

    fun user(user: String): Task<DocumentSnapshot> {
        return userColl.document(user).get()
    }
}