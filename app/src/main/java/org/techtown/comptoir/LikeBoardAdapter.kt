package org.techtown.comptoir

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class LikeBoardAdapter(private val context: Context, private var postIds: List<String> = ArrayList(), private var postList: List<HashMap<String, Any>> = ArrayList()) :
        RecyclerView.Adapter<LikeBoardAdapter.LikeViewHolder>() {

            class LikeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
                var itemimage: ImageView = itemView.findViewById(R.id.like_basic_img)
                var itemtitle: TextView = itemView.findViewById(R.id.like_title)
                var itemcontent: TextView = itemView.findViewById(R.id.like_content)
                var itemcount: TextView = itemView.findViewById(R.id.like_person_num)
                var itemlike: ImageView = itemView.findViewById(R.id.like_like)
            }

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikeViewHolder {
        val cardView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_likeboard, parent, false)

        return LikeViewHolder(cardView)
    }

    override fun onBindViewHolder(holder: LikeViewHolder, position: Int) {
        val currentPost = postList[position]
        val postId = postIds[position]

        val imageUrls = currentPost["imageUrls"] as? List<String>
        if (imageUrls != null && imageUrls.isNotEmpty()) {
            val imageUrl = imageUrls[0]

            Glide.with(holder.itemView)
                .load(imageUrl)
                .into(holder.itemimage)
        } else {
            holder.itemimage.setImageResource(R.drawable.icon_basic)
        }

        holder.itemimage.setOnClickListener {
            val intent = Intent(holder.itemimage?.context, ContentActivity::class.java)
            intent.putExtra("postId", postId)
            ContextCompat.startActivity(holder.itemimage.context, intent, null)
        }

        val title = currentPost["title"]
        holder.itemtitle.text = title?.toString() ?: "제목이 없습니다."

        holder.itemtitle.setOnClickListener {
            val intent = Intent(holder.itemtitle?.context, ContentActivity::class.java)
            intent.putExtra("postId", postId)
            ContextCompat.startActivity(holder.itemtitle.context, intent, null)
        }

        val content = currentPost["content"]
        holder.itemcontent.text = content?.toString() ?: "내용이 없습니다."

        holder.itemcontent.setOnClickListener {
            val intent = Intent(holder.itemcontent?.context, ContentActivity::class.java)
            intent.putExtra("postId", postId)
            ContextCompat.startActivity(holder.itemcontent.context, intent, null)
        }

        val count = currentPost["Now number of people"]
        holder.itemcount.text = count?.toString() ?: "0"

        val user = auth.currentUser?.uid
        val likeList = currentPost["likeList"] as? MutableList<String> ?: mutableListOf()
        val likeimg = if (likeList.contains(user)) R.drawable.icon_likefull else R.drawable.icon_like
        holder.itemlike.setImageResource(likeimg)
        holder.itemlike.setOnClickListener {
            likeBtn(holder, position, postId)
        }
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    fun setData(newPostIds: List<String>, newpostList: List<HashMap<String, Any>>) {
        postList = newpostList
        postIds = newPostIds
        notifyDataSetChanged()
    }

    private fun likeBtn(holder: LikeViewHolder, position: Int, postId: String) {
        val currentPost = postList[position].toMutableMap()
        val user = auth.currentUser?.uid
        val firestore = FirebaseFirestore.getInstance()

        if (user == null) {
            return
        }

        val likeList = currentPost["likeList"] as? MutableList<String> ?: mutableListOf()
        val isLiked = likeList.contains(user)
        val likeListRef = firestore.collection("users").document(user)

        if (isLiked) {
            likeList.remove(user)
            likeListRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    val pIdLikeList = documentSnapshot.get("likeList") as? List<String>
                    if (pIdLikeList != null && pIdLikeList.contains(postId)) {
                        likeListRef.update("likeList", FieldValue.arrayRemove(postId))
                            .addOnSuccessListener {
                                Toast.makeText(context, "찜한 목록에서 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Log.e("likeList", "${e.message}")
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("likeList", "${e.message}")
                }
        } else {
            likeList.add(user)
            likeListRef.update("likeList", FieldValue.arrayUnion(postId))
                .addOnSuccessListener {
                    Toast.makeText(context, "찜한 목록에 추가되었습니다.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e("likeList", "${e.message}")
                }
        }

        val likeimg = if (likeList.contains(user)) R.drawable.icon_likefull else R.drawable.icon_like
        holder.itemlike.setImageResource(likeimg)

        currentPost["likeList"] = likeList

        val postRef = firestore.collection("posts").document(postId)

        postRef.update("likeList", likeList)
            .addOnSuccessListener {
                Toast.makeText(context, "찜한 목록이 수정되었습니다.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("postUpdate", "${e.message}")
            }
    }
}