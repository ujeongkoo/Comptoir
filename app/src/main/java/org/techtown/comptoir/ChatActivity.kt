package org.techtown.comptoir

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import org.techtown.comptoir.databinding.ActivityChatBinding

class ChatActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private lateinit var receivedId: String
    private lateinit var binding: ActivityChatBinding
    lateinit var mDbRef: DatabaseReference
    private lateinit var receiverRoom: String //받는 대화방
    private lateinit var senderRoom: String //보내는 대화방
    private lateinit var messageList: ArrayList<Message>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mDbRef = FirebaseDatabase.getInstance().reference

        messageList = ArrayList()
        val messageAdapter: MessageAdapter = MessageAdapter(this, messageList)

        binding.chatRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.chatRecyclerView.adapter = messageAdapter

        receivedId = intent.getStringExtra("userId").toString()
        val senderUid = auth.currentUser?.uid
        senderRoom = receivedId + senderUid
        receiverRoom = senderUid + receivedId


        binding.btnSend.setOnClickListener {
            val message = binding.chatEdit.text.toString()
            val messageObject = Message(message, senderUid)

            mDbRef.child("chats").child(senderRoom).child("messages").push()
                .setValue(messageObject).addOnSuccessListener {
                    mDbRef.child("chats").child(receiverRoom).child("messages").push()
                        .setValue(messageObject)
                }
            binding.chatEdit.setText("")
        }

        mDbRef.child("chats").child(senderRoom).child("messages")
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()

                    for (postSnapshot in snapshot.children) {
                        val message = postSnapshot.getValue(Message::class.java)
                        messageList.add(message!!)
                    }
                    messageAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("adapterError", "${error.message}")
                }
            })
    }
}