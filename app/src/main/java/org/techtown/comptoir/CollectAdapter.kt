package org.techtown.comptoir

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class CollectAdapter(private val context: Context, private var postId: String, private var orderList: List<HashMap<String, String>> = ArrayList()) :
        RecyclerView.Adapter<CollectAdapter.CollectViewHolder>() {

            class CollectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
                var itemimage: ImageView = itemView.findViewById(R.id.collect_basic_img)
                var itemname: TextView = itemView.findViewById(R.id.collect_name)
                var itemlist: TextView = itemView.findViewById(R.id.collect_list)
            }

    private var firebase: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectAdapter.CollectViewHolder {
        val cardView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_collect, parent, false)

        return CollectViewHolder(cardView)
    }

    override fun onBindViewHolder(holder: CollectAdapter.CollectViewHolder, position: Int) {
        try {
            val postRef = firebase.collection("posts").document(postId)

            postRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot != null) {
                        val orderList =
                            documentSnapshot.get("OrderList") as? List<HashMap<String, Any>>
                        if (orderList != null && position < orderList.size) {
                            val order = orderList[position]
                            val kindList = order["kind"] as? ArrayList<String> ?: arrayListOf()
                            val userId = order["userId"] as? String ?: ""

                            val userRef = firebase.collection("users").document(userId)
                            userRef.get()
                                .addOnSuccessListener { documentSnapshot ->
                                    val imageUrl = documentSnapshot.getString("profileImageUrl")
                                    if (imageUrl != null) {
                                        Glide.with(holder.itemView)
                                            .load(imageUrl)
                                            .into(holder.itemimage)
                                    } else {
                                        holder.itemimage.setImageResource(R.drawable.icon_basic)
                                    }
                                    val name = documentSnapshot.getString("name")
                                    holder.itemname.text = name

                                    holder.itemlist.text = kindList.joinToString(", ")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("userRefError", "${e.message}")
                                }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("postRefError", "${e.message}")
                }
        } catch (e: Exception) {
            Log.e("postError", "${e.message}")
        }
    }

    override fun getItemCount(): Int {
        return orderList.size
    }

    fun setData(newPostId: String, newOrderList: List<HashMap<String, String>>) {
        postId = newPostId
        orderList = newOrderList
        notifyDataSetChanged()
    }

}