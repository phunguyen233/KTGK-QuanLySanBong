package com.example.ktgk13_11_26;

import android.view.View;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

/** Activity cha: gắn Toolbar + nút quay lại cho mọi màn hình. */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    public void setContentView(int layoutResID) {
        EdgeToEdge.enable(this);
        super.setContentView(layoutResID);
    }

    protected void setupToolbar(String title, boolean coNutQuayLai) {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(title);
            ab.setDisplayHomeAsUpEnabled(coNutQuayLai);
        }

        // Ứng dụng vẽ tràn viền (edge-to-edge): đẩy Toolbar xuống dưới thanh trạng thái,
        // và chừa khoảng trống phía dưới cho thanh điều hướng / bàn phím.
        View root = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, 0, bars.right, bars.bottom);
            toolbar.setPadding(0, bars.top, 0, 0);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();   // bấm mũi tên trên Toolbar -> đóng màn hình hiện tại
        return true;
    }
}
