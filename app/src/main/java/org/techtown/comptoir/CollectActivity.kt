package org.techtown.comptoir

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CollectActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collect)

        val postId = intent.getStringExtra("postId")!!

        viewManager = FlexboxLayoutManager(this)
        viewAdapter = CollectAdapter(this, postId)

        recyclerView = findViewById(R.id.recyclerview_collectboard)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        val postRef = firestore.collection("posts").document(postId)
        postRef.get()
            .addOnSuccessListener { documentSnapshot ->
                val orderList = documentSnapshot.get("OrderList") as? List<String>
                if (orderList != null) {
                    (recyclerView.adapter as? CollectAdapter)?.setData(postId, orderList)
                }
            }
            .addOnFailureListener { e ->
                Log.e("postRefError", "${e.message}")
            }
    }
}