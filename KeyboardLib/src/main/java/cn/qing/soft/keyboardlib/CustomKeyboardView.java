package cn.qing.soft.keyboardlib;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;

import java.util.List;

/**
 * 继承系统的KeyboardView,主要是在onDraw中修改个别Key的显示样式
 */
public class CustomKeyboardView extends KeyboardView {

    private Context mContext;

    public CustomKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public CustomKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
    }

    @TargetApi(21)
    public CustomKeyboardView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mContext = context;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Keyboard mKeyboard = getKeyboard();

        List<Keyboard.Key> keys = null;
        if (mKeyboard != null) {
            keys = mKeyboard.getKeys();
        }

        if (keys != null) {
            for (Keyboard.Key key : keys) {
                // 不可用的key,做暂位使用,将其背景绘制成灰色
                if (key.codes[0] == -10) {
                    drawUnavailable(canvas, key);
                }
                // 绘制删除键
                else if (key.codes[0] == -5) {
                    drawIconKey(canvas, key);
                }
            }
        }
    }

    /**
     * 绘制不可点击的Key的样式,它是一个灰色背景的没有状态的drawable,点击后无任何效果,所以只作为暂位使用
     *
     * @param canvas
     * @param key
     */
    private void drawUnavailable(Canvas canvas, Keyboard.Key key) {
        Drawable drawable = mContext.getResources().getDrawable(R.drawable.bg_keyboard_unenable);
        drawable.setBounds(key.x, key.y, key.x + key.width, key.y
                + key.height);
        drawable.draw(canvas);
    }

    /**
     * 绘制暂位背景相同背景的带图标的Key
     *
     * @param canvas
     * @param key
     */
    private void drawIconKey(Canvas canvas, Keyboard.Key key) {
        drawUnavailable(canvas, key);
        key.icon.setBounds(key.x + (key.width - key.icon.getIntrinsicWidth()) / 2, key.y + (key.height - key.icon.getIntrinsicHeight()) / 2,
                key.x + (key.width - key.icon.getIntrinsicWidth()) / 2 + key.icon.getIntrinsicWidth(), key.y + (key.height - key.icon.getIntrinsicHeight()) / 2 + key.icon.getIntrinsicHeight());
        key.icon.draw(canvas);
    }


}
