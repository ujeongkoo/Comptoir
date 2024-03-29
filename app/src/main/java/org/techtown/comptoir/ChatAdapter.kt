package org.techtown.comptoir

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChatAdapter(private val context: Context, private var chatId: List<String> = ArrayList()) :
        RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

        class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var chatimage: ImageView = itemView.findViewById(R.id.chat_basic_img)
            var chatName: TextView = itemView.findViewById(R.id.chat_person)
        }

    private var firebase: FirebaseFirestore = FirebaseFirestore.getInstance()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatAdapter.ChatViewHolder {
        val cartView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)

        return ChatViewHolder(cartView)
    }

    override fun onBindViewHolder(holder: ChatAdapter.ChatViewHolder, position: Int) {
        val currentUser = chatId[position]

        val chatRef = firebase.collection("users").document(currentUser)
        chatRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot != null) {
                    val imageUrl = documentSnapshot.getString("profileImageUrl")
                    if (imageUrl != null) {
                        Glide.with(holder.itemView)
                            .load(imageUrl)
                            .into(holder.chatimage)
                    } else {
                        holder.chatimage.setImageResource(R.drawable.icon_basic)
                    }
                    val name = documentSnapshot.getString("name")
                    holder.chatName.text = name?.toString() ?: "(이름없음)"
                }
            }

        holder.chatName.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("userId", currentUser)
            ContextCompat.startActivity(holder.chatName.context, intent, null)
        }

    }

    override fun getItemCount(): Int {
        return chatId.size
    }

    fun setData(newChatIds: List<String>) {
        chatId = newChatIds
        notifyDataSetChanged()
    }

}