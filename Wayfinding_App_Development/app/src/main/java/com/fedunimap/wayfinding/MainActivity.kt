package com.fedunimap.wayfinding

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainUIScreen()
        }
    }
}

@Composable
fun MainUIScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF1F4FB) // 浅蓝背景（和 login 页面一致）
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 标题部分
            Text(
                text = "Smart Navigation",
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFF002B5B) // 深蓝色标题
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 搜索栏
            TextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Search locations", color = Color.Gray) },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFE9ECF5),
                    focusedContainerColor = Color(0xFFE9ECF5),
                    focusedIndicatorColor = Color(0xFF002B5B),
                    unfocusedIndicatorColor = Color(0xFFCCCCCC),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 地图占位符
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Map will be shown here",
                    color = Color.Gray
                )
            }
        }

        // 右下角的浮动按钮
        FloatingButtons()
    }
}

@Composable
fun FloatingButtons() {
    val context = LocalContext.current

    // 这里的箭头按钮跳转到 ManualNavigationActivity 页面
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End
    ) {
        // 当前位置按钮
        FloatingActionButton(
            onClick = {
                // 实现显示当前位置的逻辑
            },
            containerColor = Color(0xFF002B5B),
            contentColor = Color.White
        ) {
            // 当前无图标，保留按钮样式
        }
        Spacer(modifier = Modifier.height(12.dp))

        // 标记位置按钮
        FloatingActionButton(
            onClick = {
                // 实现标记位置的逻辑
            },
            containerColor = Color(0xFF002B5B),
            contentColor = Color.White
        ) {
            // 当前无图标，保留按钮样式
        }
        Spacer(modifier = Modifier.height(12.dp))

        // 跳转到点对点导航的页面
        FloatingActionButton(
            onClick = {
                val intent = Intent(context, ManualNavigationActivity::class.java)
                context.startActivity(intent)
            },
            containerColor = Color(0xFF002B5B),
            contentColor = Color.White
        ) {
            // 当前无图标，保留按钮样式
        }
    }
}
