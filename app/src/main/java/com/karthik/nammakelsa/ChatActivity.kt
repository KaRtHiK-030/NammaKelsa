package com.karthik.nammakelsa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.karthik.nammakelsa.ui.theme.NammaKelsaTheme

class ChatActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val receiverId   = intent.getStringExtra("receiverId") ?: ""
        val receiverName = intent.getStringExtra("receiverName") ?: ""

        setContent {
            NammaKelsaTheme {
                ChatScreen(receiverId = receiverId, receiverName = receiverName)
            }
        }
    }
}
