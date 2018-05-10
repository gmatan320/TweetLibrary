package com.example.matangenezia.tweeterclone.Activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.Toast
import android.view.View
import com.example.matangenezia.tweeterclone.R
import com.google.firebase.database.FirebaseDatabase


class MainActivity : AppCompatActivity() {

    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val mDatabase = FirebaseDatabase.getInstance().reference

    override fun onStart() {
        super.onStart()
        val currentUser: FirebaseUser? = mAuth.currentUser
        if (currentUser != null) {
            this.updateUI(currentUser)
        }
        title = "Login to tweeter"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun updateUI(firebaseUser: FirebaseUser) {
        val intent = Intent(applicationContext, UsersListActivity::class.java)
        startActivity(intent)
    }

    fun signInOrSignUp(view: View) {
        val username = userNameEditText.text.toString()
        val password = passwordEditText.text.toString()
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.require_fields, Toast.LENGTH_SHORT).show()
        }
        else {
            this.signIn(username, password)
        }
    }

    fun signIn(email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // sign in user
                        val currentUser = mAuth.currentUser
                        if (currentUser != null) {
                            this.updateUI(currentUser!!)
                        }
                    }
                    else {
                        this.signUp(email, password)
                    }
                }
    }

    fun signUp(email: String, password: String) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val currentUser = mAuth.currentUser
                        if (currentUser != null) {
                            val name = email.split("@").get(0)
                            mDatabase.child("users").child(currentUser.uid).setValue(name)
                            mDatabase.child("number_of_tweets").child(currentUser.uid).setValue(0)
                            this.updateUI(currentUser!!)
                        }
                    }

                    else {
                        Toast.makeText(this, R.string.sign_up_fail, Toast.LENGTH_SHORT).show()
                    }
                }
    }
}
