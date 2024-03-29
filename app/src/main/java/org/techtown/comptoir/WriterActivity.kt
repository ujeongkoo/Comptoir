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
import java.util.HashMap

class WriterActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_writer)

        viewManager = FlexboxLayoutManager(this)
        viewAdapter = LikeBoardAdapter(this)

        recyclerView =findViewById(R.id.recyclerview_writerboard)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        val user = auth.currentUser?.uid
        if (user != null) {
            val userId = user.toString()
            writerDBPost(userId)
        } else {
            val intent = Intent(this@WriterActivity, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun writerDBPost(userId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { documents ->
                val writerList = documents.get("WriterPost") as? ArrayList<String>
                val postIds = mutableListOf<String>()
                val postList = mutableListOf<HashMap<String, Any>>()
                if (writerList != null) {
                    for (list in writerList) {
                        val postRef = FirebaseFirestore.getInstance().collection("posts").document(list)
                        postRef.get()
                            .addOnSuccessListener {
                                val postId = list
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