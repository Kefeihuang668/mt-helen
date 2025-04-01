package com.fedunimap.wayfinding

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class ManualNavigationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 这里可以根据需要添加点对点导航界面的UI
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text("Enter Start and End Locations", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                // 添加起点、终点输入框，导航按钮等
            }
        }
    }
}
