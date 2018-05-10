package com.example.matangenezia.tweeterclone.Activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import com.example.matangenezia.tweeterclone.R
import com.example.mytweeterlibrary.Feed
import com.example.mytweeterlibrary.FeedAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_feed.*

class FeedActivity : AppCompatActivity() {

    private val mDatabase = FirebaseDatabase.getInstance().reference
    private val feeds = ArrayList<Feed>()
    private var adapter: FeedAdapter? = null
    private var usersList: ArrayList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)
        this.adapter = FeedAdapter(this, this.feeds)

        this.usersList =  intent.getStringArrayListExtra("userlist")
        listView.adapter = this.adapter

        getTweetsFromUsers()
    }

    fun getTweetsFromUsers() {
        val usersdRef = mDatabase.child("tweets")
        val eventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (ds in dataSnapshot.children) {

                    val tweet = ds.value.toString()
                    val name = ds.key.toString().split("@")[0]
                    if (usersList!!.contains(name)) {
                        val feed = Feed(name, tweet)
                        this@FeedActivity.feeds.add(feed)
                        this@FeedActivity.adapter!!.notifyDataSetChanged()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        }
        usersdRef.addListenerForSingleValueEvent(eventListener)
    }
}