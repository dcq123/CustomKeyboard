package cn.qing.soft.customkeyboard;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

import cn.qing.soft.keyboardlib.CustomKeyboardHelper;

public class MainActivity extends AppCompatActivity {

    CustomKeyboardHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        helper = new CustomKeyboardHelper(this, R.xml.keyboardnumber);

        EditText editText1 = (EditText) findViewById(R.id.editText1);
        EditText editText3 = (EditText) findViewById(R.id.editText3);

        // 固定数字键盘
        helper.registerEditText(editText1);
        // 随机数字键盘
        helper.registerEditText(editText3, true);

    }

    @Override
    public void onBackPressed() {
        if (helper.isCustomKeyboardVisible()) {
            helper.hideCustomKeyboard();
        } else {
            super.onBackPressed();
        }
    }
}
