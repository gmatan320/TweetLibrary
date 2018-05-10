package com.example.matangenezia.tweeterclone.Activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.*
import android.widget.EditText
import android.widget.Switch
import com.example.matangenezia.tweeterclone.R
import com.example.mytweeterlibrary.User
import com.example.mytweeterlibrary.UserAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserInfo
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_users_list.*

class UsersListActivity : AppCompatActivity() {

    private val mDatabase = FirebaseDatabase.getInstance().reference
    private val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    private val nameList = ArrayList<String>()
    private val idList = ArrayList<String>()
    private val usersList = ArrayList<User>()
    private var adapter: UserAdapter? = null

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        val menuInflater = MenuInflater(this)
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        super.onOptionsItemSelected(item)
        when (item!!.itemId) {
            R.id.tweet -> this.tweet()

            R.id.feed -> this.showFeed()

            R.id.logout -> this.signOut()

            else -> {
                return false
            }
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users_list)
        this.adapter = UserAdapter(this, this.usersList)
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            for (profile: UserInfo in currentUser.providerData) {
                var name = profile.displayName
                if (name == null || name.isEmpty()) {
                    name = profile.email
                }
                title = name
            }
        }

        listView.adapter = this.adapter
        this.getAllUsers()
    }

    fun setIsChecked(view: View) {
        val switch = view as Switch
        val tag = switch.tag.toString().toInt()
        val user = this.usersList[tag]
        user.isFollow = switch.isChecked
    }

    private fun getAllUsers() {
        val usersRef = mDatabase.child("users")
        val eventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (ds in dataSnapshot.children) {

                    val name = ds.value.toString()
                    this@UsersListActivity.nameList.add(name);

                    val userId = ds.key.toString()
                    this@UsersListActivity.idList.add(userId)

                    val user = User(name, true)
                    this@UsersListActivity.usersList.add(user)
                    this@UsersListActivity.adapter!!.notifyDataSetChanged()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        }
        usersRef.addListenerForSingleValueEvent(eventListener)
    }

    private fun tweet() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.alert_view_edit_text, null)
        builder.setView(dialogView)
        builder.setTitle("Tweet")
        val name = currentUser!!.email!!.split("@").get(0)
        builder.setPositiveButton("Tweet", { dialog, id ->
            val tweetEditText = dialogView.findViewById<EditText>(R.id.tweetEditText)
            val tweetTxt = tweetEditText.text.toString()

            val usersRef = mDatabase.child("number_of_tweets")
            val eventListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (ds in dataSnapshot.children) {
                        if (ds.key.toString() == currentUser!!.uid) {
                            val numberOfTweets = ds.value.toString().toInt()
                            this@UsersListActivity.mDatabase.child("tweets").child("$name@$numberOfTweets").setValue(tweetTxt)
                            this@UsersListActivity.mDatabase.child("number_of_tweets").child(currentUser!!.uid).setValue(numberOfTweets + 1)
                            break
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            }
            usersRef.addListenerForSingleValueEvent(eventListener)
        })
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun showFeed() {

        val followersList = ArrayList<String>()
        for (user: User in this.usersList) {
            if (user.isFollow) {
                followersList.add(user.username)
            }
        }
        val intent = Intent(applicationContext, FeedActivity::class.java).apply {
            putExtra("userlist", followersList)
        }
        startActivity(intent)
    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        finish()
    }
}