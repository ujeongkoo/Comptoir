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

class CollectAdapter(private val context: Context, private var postId: String, private var orderList: List<String> = ArrayList()) :
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
        val postId = postId

        val postRef = firebase.collection("posts").document(postId)

        postRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot != null) {

                    val orderList = documentSnapshot.get("OrderList") as? List<HashMap<String, String>>
                    try {
                        if (orderList != null) {
                            for (order in orderList) {
                                val kind = order["kind"] as ArrayList<String>
                                val userId = order["userId"] as String

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
                                        holder.itemname.text = "${name}"
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("userRefError", "${e.message}")
                                    }
                                holder.itemlist.text = "${kind}"
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("orderListError", "${e.message}")
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("postRefError", "${e.message}")
            }
    }

    override fun getItemCount(): Int {
        return orderList.size
    }

    fun setData(newPostId: String, newOrderList: List<String>) {
        postId = newPostId
        orderList = newOrderList
        notifyDataSetChanged()
    }

}