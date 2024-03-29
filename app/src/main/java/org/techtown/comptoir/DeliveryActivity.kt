package org.techtown.comptoir

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.firebase.firestore.FirebaseFirestore

class DeliveryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivery)

        viewManager = FlexboxLayoutManager(this)
        viewAdapter = BoardAdapter(this)

        recyclerView = findViewById(R.id.recyclerview_board)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
        dbPost()
    }

    private fun dbPost() {
        val db = FirebaseFirestore.getInstance()
        db.collection("posts")
            .whereEqualTo("selected Board", "배달음식")
            .get()
            .addOnSuccessListener { documents ->
                val postIds = mutableListOf<String>()
                val postList = mutableListOf<HashMap<String, Any>>()

                for (document in documents) {
                    val postId = document.id
                    val postData = document.data
                    postIds.add(postId)
                    postList.add(postData as HashMap<String, Any>)
                }
                (recyclerView.adapter as? BoardAdapter)?.setData(postIds, postList)
            }
            .addOnFailureListener { exception ->
                Log.e("dbPost", "에러 발생: ", exception)
            }
    }
}