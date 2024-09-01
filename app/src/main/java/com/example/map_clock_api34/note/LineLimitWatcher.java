package com.example.map_clock_api34.note;

import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.widget.EditText;

public class LineLimitWatcher implements TextWatcher {
    private int maxLines;
    private EditText editText;
    private boolean isHandlingTextChange = false;

    public LineLimitWatcher(int maxLines, EditText editText) {
        this.maxLines = maxLines;
        this.editText = editText;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (isHandlingTextChange) {
            return; // 防止無限迴圈
        }

        isHandlingTextChange = true;

        // 检查行數不超過20行
        Layout layout = editText.getLayout();
        if (layout != null && layout.getLineCount() > maxLines) {
            int endOffset = Math.min(editText.getText().length(), layout.getLineEnd(maxLines - 1));
            editText.setText(editText.getText().subSequence(0, endOffset));
            editText.setSelection(editText.getText().length()); // 将輸入圖標移到最末端
        }

        isHandlingTextChange = false;
    }

    @Override
    public void afterTextChanged(Editable s) {
        // 文本变化后的操作
    }
}
