package org.techtown.comptoir

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.flexbox.FlexboxLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class ContentActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var real_title: TextView
    private lateinit var real_content: TextView
    private lateinit var real_target: TextView
    private lateinit var real_now: TextView
    private lateinit var real_place: TextView
    private lateinit var spinnerItem: Spinner
    private lateinit var btnSelect: Button
    private lateinit var btnChat: Button
    private lateinit var btnFinish: Button
    private lateinit var btnRepost: Button
    private lateinit var btnDelete: Button
    private lateinit var numberPicker: NumberPicker
    private lateinit var layoutTags: FlexboxLayout
    private lateinit var itemAdapter: ArrayAdapter<String>
    private lateinit var auth: FirebaseAuth
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: ContentImageAdapter

    private val tagList = mutableListOf<String>()
    private val imageList = listOf(
        R.drawable.icon_basic
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content)

        real_title = findViewById(R.id.content_title)
        real_content = findViewById(R.id.content_content)
        real_target = findViewById(R.id.content_goal)
        real_now = findViewById(R.id.content_now)
        real_place = findViewById(R.id.content_place)
        spinnerItem = findViewById(R.id.content_spinner)
        btnSelect = findViewById(R.id.btn_select)
        btnChat = findViewById(R.id.btn_part)
        btnFinish = findViewById(R.id.btn_finish)
        btnRepost = findViewById(R.id.btn_repost)
        btnDelete = findViewById(R.id.btn_delete)
        numberPicker = findViewById(R.id.content_numberpicker)
        layoutTags = findViewById(R.id.content_tags)


        val postId = intent.getStringExtra("postId")

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser?.uid.toString()
        if (postId != null) {
            if (user != null) {
                val writerRef = firestore.collection("posts").document(postId)
                writerRef.get()
                    .addOnSuccessListener { documentSnapShot ->
                        if (documentSnapShot.exists()) {
                            val writer = documentSnapShot.getString("user")
                            val now_num = documentSnapShot.getLong("Now number of people")
                            val target_num = documentSnapShot.getLong("Target number of people")
                            val ing = documentSnapShot.getString("ing")
                            if (user == writer) {
                                btnFinish.visibility = View.VISIBLE
                                btnRepost.visibility = View.VISIBLE
                                btnDelete.visibility = View.VISIBLE
                            }
                            else if (now_num == target_num) {
                                btnChat.visibility = View.GONE
                            }
                            else {
                                btnFinish.visibility = View.GONE
                                btnRepost.visibility = View.GONE
                                btnDelete.visibility = View.GONE
                            }
                            if (ing == "false") {
                                btnChat.visibility = View.GONE
                                btnFinish.text = "다시 모집"
                            } else {
                                btnChat.visibility = View.VISIBLE
                                btnFinish.text = "마감 하기"
                            }
                        }
                    }
            }
        }

        fetchPostDetails(postId)

        if (postId != null) {
            val postRef = firestore.collection("posts").document(postId)
            postRef.get()
                .addOnSuccessListener { documentSnapShot ->
                    val max_num = documentSnapShot.getLong("max number")?.toInt()

                    numberPicker.minValue = 1
                    numberPicker.maxValue = max_num!!
                }
                .addOnFailureListener { e ->
                    Log.e("postRefError", "${e.message}")
                }
        }

        btnSelect.setOnClickListener {
            val kind_select = spinnerItem.selectedItem.toString()
            val num_select = numberPicker.value

            val tagText = "${kind_select} ${num_select}개"
            if(tagText.isNotEmpty()) {
                addTag(tagText)
            }
        }

        btnChat.setOnClickListener {
            val inflater = layoutInflater
            val dialogView = inflater.inflate(R.layout.popup_check, null)

            val placeText: TextView = dialogView.findViewById(R.id.popup_place_check)
            val kindText: TextView = dialogView.findViewById(R.id.popup_kind_check)

            if (postId != null) {
                val postRef = firestore.collection("posts").document(postId)
                postRef.get()
                    .addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot != null) {
                            val place = documentSnapshot.getString("Place of receipt")
                            val writer = documentSnapshot.getString("user")
                            val kind = tagList

                            placeText.text = "수령 장소: ${place}"
                            kindText.text = "상품 종류: ${kind}"

                            val builder = AlertDialog.Builder(this)
                            builder.setView(dialogView)
                                .setPositiveButton("확인하였습니다.") { dialog, which ->
                                    if (writer != user) {
                                        participate(user, postId, kind)
                                        val userId = documentSnapshot.getString("user")!!
                                        val intent = Intent(this, ChatActivity::class.java)
                                        intent.putExtra("userId", userId)
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        my_participate(user, postId, kind)
                                    }
                                }
                                .setNegativeButton("취소") { dialog, which ->
                                    dialog.cancel()
                                }

                            builder.create().show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("popupError", "${e.message}")
                    }
            }
        }

        btnFinish.setOnClickListener {
            val inflater = layoutInflater
            val dialogView = inflater.inflate(R.layout.popup_finish, null)
            val dialogView2 = inflater.inflate(R.layout.popup_resell, null)
            val builder = AlertDialog.Builder(this)
            
            if (postId != null) {
                val postRef = firestore.collection("posts").document(postId)
                postRef.get()
                    .addOnSuccessListener { documentSnapShot ->
                        if (documentSnapShot != null) {
                            val state = documentSnapShot.getString("ing")
                            if (state == "true") {
                                builder.setView(dialogView)
                                    .setPositiveButton("마감") { dialog, which ->
                                        val ch_state = "false"
                                        postRef.update("ing", ch_state)

                                        btnChat.visibility = View.GONE
                                        btnFinish.text = "다시 모집"
                                    }
                                    .setNegativeButton("취소") { dialog, which ->
                                        dialog.cancel()
                                    }
                                builder.create().show()
                            } else {
                                builder.setView(dialogView2)
                                    .setPositiveButton("다시 시작합니다.") { dialog, which ->
                                        val ch2_state = "true"
                                        postRef.update("ing", ch2_state)
                                        
                                        btnChat.visibility = View.VISIBLE
                                        btnFinish.text= "마감 하기"
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

        btnRepost.setOnClickListener {
            val intent = Intent(this, RewriteActivity::class.java)
            intent.putExtra("postId", postId)
            startActivity(intent)
            finish()
        }

        btnDelete.setOnClickListener {
            if (postId != null) {
                val deleteRef = FirebaseFirestore.getInstance().collection("posts").document(postId)
                deleteRef.delete()
                val userRef = FirebaseFirestore.getInstance().collection("users").document(user)
                userRef.update("WriterPost", FieldValue.arrayRemove(postId))
                userRef.update("cartList", FieldValue.arrayRemove(postId))
                Toast.makeText(this@ContentActivity, "게시물이 삭제되었습니다.", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, HomeFragment::class.java)
                startActivity(intent)
                finish()
            }
        }

    }

    private fun fetchPostDetails(postId: String?) {

        tagList.clear()

        if (postId != null) {
            val postRef = firestore.collection("posts").document(postId)
            postRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val title = documentSnapshot.getString("title")
                        val content = documentSnapshot.getString("content")
                        val target = documentSnapshot.getLong("Target number of people")
                        val now = documentSnapshot.getLong("Now number of people")
                        val place = documentSnapshot.getString("Place of receipt")
                        val imageLoad = documentSnapshot.get("imageUrls") as? ArrayList<String>
                        val item = documentSnapshot.get("tagList") as? ArrayList<String>

                        update(title, content, target, now, place)
                        updateImg(imageLoad)
                        initSpinner(item)
                    } else {
                        Toast.makeText(this@ContentActivity, "해당 포스트가 존재하지 않습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("postFail", "${e.message}")
                }
        } else {
            Toast.makeText(this@ContentActivity, "해당 포스트 정보가 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addTag(tagText: String) {
        val tagLayout = layoutInflater.inflate(R.layout.tag_layout, null) as LinearLayout
        val tagTextView: TextView = tagLayout.findViewById(R.id.tag_text)
        val deleteBtn: ImageView = tagLayout.findViewById(R.id.btn_delete)

        tagTextView.text = tagText

        deleteBtn.setOnClickListener {
            layoutTags.removeView(tagLayout)
            tagList.remove(tagText)
        }

        layoutTags.addView(tagLayout)
        tagList.add(tagText)
    }

    private fun participate(userId: String?, postId: String?, kind: List<String>) {
        if (postId != null) {
            val postRef = firestore.collection("posts").document(postId)
            postRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot != null) {
                        val participation = documentSnapshot.get("Participation") as? ArrayList<String>
                        if (participation != null) {
                            for (parti in participation) {
                                if (parti != userId) {
                                    var part = documentSnapshot.getLong("Now number of people")?.toInt()
                                    if (part != null) {
                                        part += 1
                                        postRef.update("Now number of people", part)
                                        if (userId != null) {
                                            postRef.update("Participation", FieldValue.arrayUnion(userId))
                                            val orderList = hashMapOf(
                                                "kind" to kind,
                                                "userId" to userId
                                            )
                                            postRef.update("OrderList", FieldValue.arrayUnion(orderList))

                                            val userRef = firestore.collection("users").document(userId)
                                            userRef.update("Participation", FieldValue.arrayUnion(postId))
                                            userRef.update("chatId", FieldValue.arrayUnion())
                                        }
                                    }
                                    val writer = documentSnapshot.getString("user")!!
                                    val writerRef = firestore.collection("users").document(writer)
                                    writerRef.get()
                                        .addOnSuccessListener {
                                            writerRef.update("chatId", FieldValue.arrayUnion(userId))
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("writerError", "${e.message}")
                                        }
                                    if (userId != null) {
                                        val userRef = firestore.collection("users").document(userId)
                                        userRef.update("chatId", FieldValue.arrayUnion(writer))
                                    }
                                } else {
                                    Toast.makeText(this@ContentActivity, "이미 참여하셨습니다!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            var part = documentSnapshot.getLong("Now number of people")?.toInt()
                            if (part != null) {
                               part += 1
                                postRef.update("Now number of people", part)
                                if (userId != null) {
                                    postRef.update("Participation", FieldValue.arrayUnion(userId))
                                    val orderList = hashMapOf(
                                        "kind" to kind,
                                        "userId" to userId
                                    )
                                    postRef.update("OrderList", FieldValue.arrayUnion(orderList))

                                    val userRef = firestore.collection("users").document(userId)
                                    userRef.update("Participation", FieldValue.arrayUnion(postId))
                                    val writer = documentSnapshot.getString("user")!!
                                    val writerRef = firestore.collection("users").document(writer)
                                    writerRef.get()
                                        .addOnSuccessListener {
                                            writerRef.update("chatId", FieldValue.arrayUnion(userId))
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("writerError", "${e.message}")
                                        }
                                    userRef.update("chatId", FieldValue.arrayUnion(writer))

                                }
                            }
                        }

                    }
                }
                .addOnFailureListener { e ->
                    Log.e("postRefError", "${e.message}")
                }
        } else {
            Log.d("yourError", "정보가 존재하지 않습니다.")
        }
    }

    private fun my_participate(userId: String?, postId: String?, kind: List<String>) {
        if (postId != null) {
            val postRef = firestore.collection("posts").document(postId)
            postRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot != null) {
                        val participation = documentSnapshot.get("Participation") as? ArrayList<String>
                        if (participation != null) {
                            for (parti in participation) {
                                if (parti != userId) {
                                    var part = documentSnapshot.getLong("Now number of people")?.toInt()
                                    if (part != null) {
                                        part += 1
                                        postRef.update("Now number of people", part)
                                        if (userId != null) {
                                            postRef.update("Participation", FieldValue.arrayUnion(userId))
                                            val orderList = hashMapOf(
                                                "kind" to kind,
                                                "userId" to userId
                                            )
                                            postRef.update("OrderList", FieldValue.arrayUnion(orderList))
                                            val userRef = firestore.collection("users").document(userId)
                                            userRef.update("Participation", FieldValue.arrayUnion(postId))
                                        }
                                    }
                                }
                            }
                        } else {
                            var part = documentSnapshot.getLong("Now number of people")?.toInt()
                            if (part != null) {
                                part += 1
                                postRef.update("Now number of people", part)
                                if (userId != null) {
                                    postRef.update("Participation", FieldValue.arrayUnion(userId))
                                    postRef.update("OrderList", FieldValue.arrayUnion(userId, arrayListOf(kind)))
                                    val userRef = firestore.collection("users").document(userId)
                                    userRef.update("Participation", FieldValue.arrayUnion(postId))
                                }
                            }
                        }
                    }
                }
        } else {
            Log.e("myError", "정보가 존재하지 않습니다.")
        }
    }

    private fun update(title: String?, content: String?, target: Long?, now: Long?, place: String?) {
        real_title.text = "${title}"
        real_content.text = "${content}"
        real_now.text = "현재 참여 인원: ${now}명"
        real_target.text = "목표 참여 인원: ${target}명"
        real_place.text = "수령 장소: ${place}"
    }

    private fun updateImg(imageLoad: ArrayList<String>?) {
        viewPager = findViewById(R.id.content_viewPager)
        if (imageLoad != null && imageLoad.isNotEmpty()) {
            adapter = ContentImageAdapter(imageLoad ?: ArrayList())
            Log.d("findError", "${imageLoad}")
        } else {
            adapter = ContentImageAdapter(imageList)
        }
        viewPager.adapter = adapter
    }

    private fun initSpinner(item: ArrayList<String>?) {
        if(item != null) {
            itemAdapter =
                ArrayAdapter(this@ContentActivity, android.R.layout.simple_spinner_item, item)
            itemAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerItem.adapter = itemAdapter
        }
    }
}

class ContentImageAdapter(private val images: List<Any>) :
    RecyclerView.Adapter<ContentImageAdapter.ImageViewHolder>() {
    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_slider, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val image = images[position]
        if (image is String) {
            Glide.with(holder.itemView.context)
                .load(image)
                .into(holder.imageView)
        } else if (image is Int) {
            holder.imageView.setImageResource(image)
        }
    }

    override fun getItemCount(): Int {
        return images.size
    }
}