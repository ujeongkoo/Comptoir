package org.techtown.comptoir

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.google.android.flexbox.FlexboxLayout
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class OrderActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)

        viewManager = FlexboxLayoutManager(this)
        viewAdapter = LikeBoardAdapter(this)

        recyclerView = findViewById(R.id.recyclerview_orderboard)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        val user = auth.currentUser?.uid
        if(user != null) {
            val userId = user.toString()
            orderDBPost(userId)
        } else {
            val intent = Intent(this@OrderActivity, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun orderDBPost(userId: String) {
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { documents->
                val orderList = documents.get("Participation") as? ArrayList<String>
                val postIds = mutableListOf<String>()
                val postList = mutableListOf<HashMap<String, Any>>()
                if (orderList != null) {
                    for (order in orderList) {
                        val postRef = FirebaseFirestore.getInstance().collection("posts").document(order)
                        postRef.get()
                            .addOnSuccessListener {
                                val postId = order
                                val postData = it.data

                                postIds.add(postId)
                                postList.add(postData as HashMap<String, Any>)
                                (recyclerView.adapter as? LikeBoardAdapter)?.setData(postIds, postList)
                            }
                            .addOnFailureListener { e ->
                                Log.e("postRefFail", "${e.message}")
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("ErrorFind", "${e.message}")
            }
    }
}