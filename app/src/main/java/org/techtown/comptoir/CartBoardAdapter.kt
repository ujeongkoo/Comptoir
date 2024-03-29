package org.techtown.comptoir

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class CartBoardAdapter(private val context: Context, private var postIds: List<String> = ArrayList(), private var postList: List<HashMap<String, Any>> = ArrayList()) :
        RecyclerView.Adapter<CartBoardAdapter.CartViewHolder>() {

            class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
                var itemimage: ImageView = itemView.findViewById(R.id.cart_basic_img)
                var itemtitle: TextView = itemView.findViewById(R.id.cart_title)
                var itemcontent: TextView = itemView.findViewById(R.id.cart_content)
                var itemcount: TextView = itemView.findViewById(R.id.cart_person_num)
                var itemleft: TextView = itemView.findViewById(R.id.cart_left_num)
                var itemlike: ImageView = itemView.findViewById(R.id.cart_like)
                var itemdelete: Button = itemView.findViewById(R.id.cart_delete)
                var itemfinish: Button = itemView.findViewById(R.id.cart_finish)
                var itemrewrite: Button = itemView.findViewById(R.id.cart_rewrite)
            }

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val cardView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cartboard, parent, false)

        return CartViewHolder(cardView)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val currentPost = postList[position]
        val postId = postIds[position]

        val imageUrls = currentPost["imageUrls"] as? List<String>
        if(imageUrls != null && imageUrls.isNotEmpty()) {
            val imageUrl = imageUrls[0]

            Glide.with(holder.itemView)
                .load(imageUrl)
                .into(holder.itemimage)
        } else {
            holder.itemimage.setImageResource(R.drawable.icon_basic)
        }

        holder.itemimage.setOnClickListener {
            val intent = Intent(holder.itemimage?.context, CollectActivity::class.java)
            intent.putExtra("postId", postId)
            ContextCompat.startActivity(holder.itemimage.context, intent, null)
        }

        val title = currentPost["title"]
        holder.itemtitle.text = title?.toString() ?: "제목이 없습니다."

        holder.itemtitle.setOnClickListener {
            val intent = Intent(holder.itemtitle?.context, CollectActivity::class.java)
            intent.putExtra("postId", postId)
            ContextCompat.startActivity(holder.itemtitle.context, intent, null)
        }

        val content = currentPost["content"]
        holder.itemcontent.text = content?.toString() ?: "내용이 없습니다."

        holder.itemcontent.setOnClickListener {
            val intent = Intent(holder.itemcontent?.context, CollectActivity::class.java)
            intent.putExtra("postId", postId)
            ContextCompat.startActivity(holder.itemcontent.context, intent, null)
        }


        val count = currentPost["Now number of people"]
        holder.itemcount.text = count?.toString() ?: "0"

        val target: Int = (currentPost["Target number of people"] as Long).toInt()
        val now: Int = (currentPost["Now number of people"] as Long).toInt()
        val left = target - now
        holder.itemleft.text = "${left}명 남았습니다."

        val user = auth.currentUser?.uid
        val likeList = currentPost["likeList"] as? MutableList<String> ?: mutableListOf()
        val likeimg = if (likeList.contains(user)) R.drawable.icon_likefull else R.drawable.icon_like
        holder.itemlike.setImageResource(likeimg)
        holder.itemlike.setOnClickListener {
            likeBtn(holder, position, postId)
        }

        holder.itemrewrite.setOnClickListener {
            val intent = Intent(holder.itemrewrite?.context, RewriteActivity::class.java)
            intent.putExtra("postId", postId)
            ContextCompat.startActivity(holder.itemrewrite.context, intent, null)
        }

        holder.itemdelete.setOnClickListener {
            if (user != null) {
                val userRef = FirebaseFirestore.getInstance().collection("users").document(user)
                userRef.update("cartList", FieldValue.arrayRemove(postId))
                Toast.makeText(context, "장바구니에서 삭제되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        holder.itemfinish.setOnClickListener {
            val inflater = LayoutInflater.from(context)
            val dialogView = inflater.inflate(R.layout.popup_finish, null)
            val builder = AlertDialog.Builder(context)

            val postRef = FirebaseFirestore.getInstance().collection("posts").document(postId)
            postRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot != null) {
                        val state = documentSnapshot.getString("ing")
                        if (state == "true") {
                            builder.setView(dialogView)
                                .setPositiveButton("마감") { dialog, which ->
                                    val ch_state = "false"
                                    postRef.update("ing", ch_state)

                                    holder.itemfinish.visibility = View.GONE
                                }
                                .setNegativeButton("취소") { dialog, which ->
                                    dialog.cancel()
                                }
                            builder.create().show()
                        }
                    }
                }
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

    private fun likeBtn(holder: CartBoardAdapter.CartViewHolder, position: Int, postId: String) {
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