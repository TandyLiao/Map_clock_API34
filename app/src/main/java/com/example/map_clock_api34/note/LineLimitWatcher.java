package com.example.map_clock_api34.note;

import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.widget.EditText;

public class LineLimitWatcher implements TextWatcher {
    private int maxLines; // 定義最大行數限制
    private EditText editText; // 要限制行數的 EditText
    private boolean isHandlingTextChange = false; // 用於防止遞迴無限迴圈

    public LineLimitWatcher(int maxLines, EditText editText) {
        this.maxLines = maxLines; // 設定行數限制
        this.editText = editText; // 設定目標 EditText
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // 文字改變之前執行的操作
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // 如果已經在處理文字變更，直接返回，避免無限遞迴
        if (isHandlingTextChange) {
            return; // 防止無限迴圈
        }

        isHandlingTextChange = true; // 標記正在處理文字變更

        // 檢查 EditText 的行數是否超過最大行數
        Layout layout = editText.getLayout();
        if (layout != null && layout.getLineCount() > maxLines) {
            // 如果超過最大行數，截取前 maxLines 行的文本
            int endOffset = Math.min(editText.getText().length(), layout.getLineEnd(maxLines - 1));
            editText.setText(editText.getText().subSequence(0, endOffset)); // 截取前面部分文字
            editText.setSelection(editText.getText().length()); // 光標移到文本末端
        }

        isHandlingTextChange = false; // 完成文字變更後重置標記
    }

    @Override
    public void afterTextChanged(Editable s) {
        // 文字改變後執行的操作
    }
}
