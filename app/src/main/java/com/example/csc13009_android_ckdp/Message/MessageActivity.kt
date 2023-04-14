package com.example.csc13009_android_ckdp.Message

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.csc13009_android_ckdp.LoginActivity
import com.example.csc13009_android_ckdp.Message.NewMessageActivity.Companion.USER_KEY
import com.example.csc13009_android_ckdp.Models.Users
import com.example.csc13009_android_ckdp.Notification.NotificationService
import com.example.csc13009_android_ckdp.R
import com.example.csc13009_android_ckdp.databinding.ActivityChatBinding
import com.example.csc13009_android_ckdp.databinding.ActivityMessageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import de.hdodenhof.circleimageview.CircleImageView
import org.w3c.dom.Text

class MessageActivity : AppCompatActivity() {
    companion object {
        var currentUser: Users? = null
    }
    lateinit private var binding: ActivityMessageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.recyclerviewLatestMessages.adapter=adapter

        adapter.setOnItemClickListener { item, view ->

            val userItem = item as LatestMessageRow

            val intent= Intent(view.context,ChatActivity::class.java)
            intent.putExtra(USER_KEY, userItem.toUser)
            startActivity(intent)
        }

        listenForLatestMessages()


        fetchCurrentUser()

    }

    class LatestMessageRow(val chatMessage: ChatMessage,val toUser:Users?): Item<ViewHolder>() {
        override fun bind(viewHolder: ViewHolder, position: Int) {
            val lastestChat=viewHolder.itemView.findViewById<TextView>(R.id.message_textview_latest_message)
lastestChat.text = chatMessage.text

            val image=viewHolder.itemView.findViewById<CircleImageView>(R.id.imageview_latest_message)
val username=viewHolder.itemView.findViewById<TextView>(R.id.username_textview_latest_message)

            username.text=toUser?.name
            Picasso.get().load(toUser?.image).into(image)

            val button: Button =viewHolder.itemView.findViewById(R.id.remindToTakeMedicineBtn)
            button.setOnClickListener{
                val notificationService= NotificationService()
                var mUserId=FirebaseAuth.getInstance().uid
                if (mUserId != null) {
                    notificationService.notifyForThatPerson(toUser!!.userId,"clock",mUserId,System.currentTimeMillis().toString())
                }
            }
        }

        override fun getLayout(): Int {
            return R.layout.item_last_message
        }
    }
    val adapter=GroupAdapter<ViewHolder>()

    val latestMessagesMap = HashMap<String, ChatMessage>()

    private fun refreshRecyclerViewMessages() {
        adapter.clear()
        latestMessagesMap.values.forEach {task ->

            FirebaseDatabase.getInstance().getReference("/Users/${task.toId}").get().addOnSuccessListener {
                val toUser=it.getValue(Users::class.java)
                adapter.add(LatestMessageRow(task,toUser))
            }.addOnFailureListener{
                Log.e("firebase", "Error getting data", it)
            }
        }
    }

    private fun listenForLatestMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/Latest_Messages/$fromId")
        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java) ?: return
                latestMessagesMap[p0.key!!] = chatMessage
                refreshRecyclerViewMessages()
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java) ?: return
                latestMessagesMap[p0.key!!] = chatMessage
                refreshRecyclerViewMessages()
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }
            override fun onChildRemoved(p0: DataSnapshot) {

            }
            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/Users/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                currentUser = p0.getValue(Users::class.java)
                Log.d("LatestMessages", "Current user ${currentUser?.image}")
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun verifyUserIsLoggedIn(){
        val uid=FirebaseAuth.getInstance().uid
        if(uid==null){
            val intent= Intent(this,LoginActivity::class.java)
            intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
                when (item?.itemId) {
            R.id.menu_new_message -> {
                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }
        }

        return super.onOptionsItemSelected(item)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.message_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

}