package org.techtown.comptoir

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CartFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_cart, container, false)

        viewManager = FlexboxLayoutManager(requireContext())
        viewAdapter = CartBoardAdapter(requireContext())

        recyclerView = view.findViewById(R.id.recyclerview_listboard)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = auth.currentUser?.uid
        if (user != null) {
            val userId = user.toString()
            cartDBPost(userId)
        } else {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun cartDBPost(userId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { documents ->
                val cartList = documents.get("WriterPost") as? ArrayList<String>
                val postIds = mutableListOf<String>()
                val postList = mutableListOf<HashMap<String, Any>>()
                if (cartList != null) {
                    for (cart in cartList) {
                        val postRef = FirebaseFirestore.getInstance().collection("posts").document(cart)
                        postRef.get()
                            .addOnSuccessListener {
                                val postId = cart
                                val postData = it.data

                                postIds.add(postId)
                                postList.add(postData as HashMap<String, Any>)
                                (recyclerView.adapter as? CartBoardAdapter)?.setData(postIds, postList)
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