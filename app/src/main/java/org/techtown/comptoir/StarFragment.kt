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

class StarFragment : Fragment() {

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
        val view = inflater.inflate(R.layout.fragment_star, container, false)

        viewManager = FlexboxLayoutManager(requireContext())
        viewAdapter = ChatAdapter(requireContext())

        recyclerView = view.findViewById(R.id.recyclerview_chatboard)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        val user = auth.currentUser?.uid
        if (user != null) {
            val userId = user.toString()
            chatDBPost(userId)
        } else {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    private fun chatDBPost(userId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { documents ->
                val chatList = documents.get("chatId") as? ArrayList<String>
                val chatIds = mutableListOf<String>()
                if (chatList != null) {
                    for (chat in chatList) {
                        chatIds.add(chat)
                        (recyclerView.adapter as?  ChatAdapter)?.setData(chatIds)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("dbError", "${e.message}")
            }
    }

}