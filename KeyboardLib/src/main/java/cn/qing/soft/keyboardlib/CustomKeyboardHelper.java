
package cn.qing.soft.keyboardlib;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * 自定义键盘显示隐藏工具类
 */
public class CustomKeyboardHelper {

    private KeyboardView mKeyboardView;
    private Keyboard mKeyboard;
    private Activity mHostActivity;
    private View mKeyboardContainer;
    // 按键对应的xml布局文件
    private int mKeyXml;

    private OnKeyboardActionListener mOnKeyboardActionListener = new SimpleOnKeyboardActionListener() {

        private final static int CodeDelete = -5; // 删除按键
        private final static int CodeNone = -10; // 空按键

        @Override
        public void onKey(int primaryCode, int[] keyCodes) {
            View focusCurrent = mHostActivity.getWindow().getCurrentFocus();
            if (focusCurrent == null || !(focusCurrent instanceof EditText)) {
                return;
            }
            EditText edittext = (EditText) focusCurrent;
            Editable editable = edittext.getText();
            int start = edittext.getSelectionStart();
            if (primaryCode == CodeDelete) {
                if (editable != null && start > 0) editable.delete(start - 1, start);
            } else if (primaryCode == CodeNone) {
                // do nothing
            } else { // 插入字符到EditText
                editable.insert(start, Character.toString((char) primaryCode));
            }
        }
    };

    /**
     * 该构造方法使用lib中提供的默认键盘布局xml,如需自定义xml可使用下面的构造方法
     *
     * @param host
     */
    public CustomKeyboardHelper(Activity host) {
        this(host, R.xml.keyboardnumber);
    }

    public CustomKeyboardHelper(Activity host, int keyXml) {
        mHostActivity = host;
        this.mKeyXml = keyXml;
        initKeyboardLayout();
        mHostActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void initKeyboardLayout() {
        // 将自定义的键盘布局添加到content中,这样就不依赖具体的布局文件了
        ViewGroup mContentView = (ViewGroup) mHostActivity.getWindow().findViewById(android.R.id.content);
        mKeyboardContainer = LayoutInflater.from(mHostActivity).inflate(R.layout.keyboard_layout, null);
        mKeyboardView = (KeyboardView) mKeyboardContainer.findViewById(R.id.keyboardview);
        // 从xml中读取配置的key,构建keyboard,它就像KeyboardView的数据集一样
        mKeyboard = new Keyboard(mHostActivity, mKeyXml);
        mKeyboardView.setKeyboard(mKeyboard);
        mKeyboardView.setPreviewEnabled(false);
        // 设置动作监听,在监听方法中将键盘上点击的字符手动添加到EditText中
        mKeyboardView.setOnKeyboardActionListener(mOnKeyboardActionListener);
        mContentView.addView(mKeyboardContainer);
        // 初始化键盘布局的显示,默认是隐藏到布局的最底部,在显示时,以动画显示出来
        mKeyboardContainer.post(new Runnable() {
            @Override
            public void run() {
                mKeyboardContainer.setTranslationY(mKeyboardContainer.getHeight());
                mKeyboardContainer.setVisibility(View.GONE);
            }
        });
        // 点击图标,隐藏键盘
        mKeyboardContainer.findViewById(R.id.keyboardHideImage).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideCustomKeyboard();
            }
        });
    }

    /**
     * 判断当前键盘的显示状态
     */
    public boolean isCustomKeyboardVisible() {
        return mKeyboardContainer.getVisibility() == View.VISIBLE;
    }

    /**
     * 动画显示自定义键盘
     */
    public void showCustomKeyboard(View v) {

        mKeyboardContainer.animate().translationY(0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mKeyboardContainer.setVisibility(View.VISIBLE);
            }
        }).start();
        if (v != null)
            ((InputMethodManager) mHostActivity.getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    /**
     * 动画隐藏自定义键盘
     */
    public void hideCustomKeyboard() {
        mKeyboardContainer.animate().translationY(mKeyboardContainer.getHeight()).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mKeyboardContainer.setVisibility(View.GONE);
            }
        }).start();
    }

    /**
     * 对指定的EditText进行自定义键盘监听,只要调用该方法注册了键盘监听的EditText才能成功call起自定义键盘,否则显示的将会是系统键盘
     *
     * @param edittext
     */
    public void registerEditText(EditText edittext) {
        registerEditText(edittext, false);
    }

    /**
     * 定义是否使用随机数字键盘
     *
     * @param edittext
     * @param isRandom
     */
    public void registerEditText(EditText edittext, final boolean isRandom) {
        // 通过以下3个监听方法,确保自定义键盘显示
        edittext.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    resetKeyNumber(isRandom);
                    showCustomKeyboard(v);
                } else {
                    hideCustomKeyboard();
                }
            }
        });
        edittext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isCustomKeyboardVisible()) {
                    showCustomKeyboard(v);
                }
            }
        });
        edittext.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                EditText edittext = (EditText) v;
                edittext.onTouchEvent(event);
                ((InputMethodManager) mHostActivity.getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), 0);
                return true;
            }
        });
    }

    /**
     * 根据是否随机动态改变键盘显示
     *
     * @param isRandom
     */
    private void resetKeyNumber(boolean isRandom) {
        if (isRandom) {
            randomKeyboardNumber();
            mKeyboardView.requestLayout();
        } else {
            mKeyboard = new Keyboard(mHostActivity, mKeyXml);
            mKeyboardView.setKeyboard(mKeyboard);
            mKeyboardView.requestLayout();
        }
    }

    private boolean isNumber(String str) {
        return "0123456789".contains(str);
    }

    /**
     * 随机获取键盘的位置,重新布局键盘各个Key的显示
     */
    private void randomKeyboardNumber() {
        List<Keyboard.Key> keyList = mKeyboard.getKeys();
        // 查找出0-9的数字键
        List<Keyboard.Key> newkeyList = new ArrayList<Keyboard.Key>();
        for (int i = 0; i < keyList.size(); i++) {
            if (!TextUtils.isEmpty(keyList.get(i).label)
                    && isNumber(keyList.get(i).label.toString())) {
                newkeyList.add(keyList.get(i));
            }
        }
        int count = newkeyList.size();
        List<KeyModel> resultList = new ArrayList<KeyModel>();
        LinkedList<KeyModel> temp = new LinkedList<KeyModel>();
        for (int i = 0; i < count; i++) {
            temp.add(new KeyModel(48 + i, i + ""));
        }
        Random rand = new Random();
        for (int i = 0; i < count; i++) {
            int num = rand.nextInt(count - i);
            resultList.add(new KeyModel(temp.get(num).getCode(),
                    temp.get(num).getLabel()));
            temp.remove(num);
        }
        for (int i = 0; i < newkeyList.size(); i++) {
            newkeyList.get(i).label = resultList.get(i).getLabel();
            newkeyList.get(i).codes[0] = resultList.get(i)
                    .getCode();
        }
        mKeyboardView.setKeyboard(mKeyboard);
    }

    class KeyModel {

        private Integer code;
        private String label;

        public KeyModel(Integer code, String label) {
            this.code = code;
            this.label = label;
        }

        public Integer getCode() {
            return code;
        }

        public void setCode(Integer code) {
            this.code = code;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }


    }

}
