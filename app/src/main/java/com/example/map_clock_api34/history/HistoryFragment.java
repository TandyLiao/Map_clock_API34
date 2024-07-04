package com.example.map_clock_api34.history;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.map_clock_api34.Database.AppDatabaseHelper;
import com.example.map_clock_api34.R;
import com.example.map_clock_api34.history.ListAdapter.ListAdapterHistory;
import com.example.map_clock_api34.Database.AppDatabaseHelper.LocationTable;
import com.example.map_clock_api34.Database.AppDatabaseHelper.HistoryTable;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class HistoryFragment extends Fragment {

    boolean isEdit, isDelete;

    View rootView;
    View overlayView;
    Button btnEdit, btnSelect, btnClearAll;

    RecyclerView recyclerViewHistory;
    ListAdapterHistory listAdapterHistory;
    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
    ArrayList<HashMap<String, String>> toRemove = new ArrayList<>();

    private AppDatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.history_fragment_history, container, false);
        dbHelper= new AppDatabaseHelper(requireContext());

        setupActionBar();
        setupButtons();
        setupRecyclerViews();
        return rootView;
    }

    private void setupButtons() {
        // 編輯以及確認按鈕
        btnEdit = rootView.findViewById(R.id.EditButton);
        btnEdit.setOnClickListener(v -> {
            isEdit = !isEdit;
            isDelete = !isDelete;

            updateButtonState();
            listAdapterHistory.setEditMode(isEdit, isEdit);
            if (!isEdit) {
                clearSelections();
            }
        });

        // 清除資料庫按鈕
        btnClearAll = rootView.findViewById(R.id.ClearAllButton);
        btnClearAll.setOnClickListener(v -> {

            dbHelper.clearAllTables();
            Toast.makeText(getActivity(), "已清除所有紀錄", Toast.LENGTH_SHORT).show();

            arrayList.clear();
            listAdapterHistory.notifyDataSetChanged();

            isEdit = false;    // 回到初始状态
            isDelete = false;  // 回到初始状态
            updateButtonState(); // 更新按鈕狀態
        });

        // 刪除以及套用按鈕
        btnSelect = rootView.findViewById(R.id.SelectButton);
        btnSelect.setOnClickListener(v -> {
            if (isDelete) {
                ShowPopupWindow();
            } else {
                // 套用按鈕在這實現功能
            }
        });

        updateButtonState(); // 初始化按鈕狀態
    }

    private void setupRecyclerViews() {
        recyclerViewHistory = rootView.findViewById(R.id.recycleViewHistory);
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewHistory.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        listAdapterHistory = new ListAdapterHistory(arrayList);
        //檢測是否有選擇RecycleView的監聽器
        listAdapterHistory.setOnItemSelectedListener(this::updateButtonState);
        recyclerViewHistory.setAdapter(listAdapterHistory);
    }

    private void RecycleViewReset() {
        arrayList.clear();
        addFromDB();
        listAdapterHistory.notifyDataSetChanged();
        updateButtonState(); // 更新按鈕狀態
    }

    private void addFromDB() {
        String placeName;
        String lan;
        String lon;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM history WHERE arranged_id=0", null);

        if (cursor != null) {

            while (cursor.moveToNext()) {
                placeName = cursor.getString(2);
                lan = cursor.getString(3);
                lon=cursor.getString(4);

                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("placeName", placeName);
                hashMap.put("latitude", lan);
                hashMap.put("longitude", lon);
                arrayList.add(hashMap);
            }
            cursor.close();
        }
        db.close();
    }

    @Override
    public void onResume() {
        super.onResume();

        //不在編輯模式下按鈕布林值
        isEdit = false;
        //刪除按鈕有無出現的布林值
        isDelete = false;
        Log.d("HistoryFragment", "onResume called");
        updateButtonState();
        RecycleViewReset();
    }
    private void setupActionBar() {
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
            actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getActivity(), R.color.lightgreen)));
        }

        CardView cardViewTitle = new CardView(requireContext());
        cardViewTitle.setLayoutParams(new CardView.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT));
        Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.cardviewtitle_shape);
        cardViewTitle.setBackground(drawable);

        LinearLayout linearLayout = new LinearLayout(requireContext());
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        ImageView mark = new ImageView(requireContext());
        mark.setImageResource(R.drawable.history_record1);
        mark.setPadding(10, 10, 5, 10);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                100, 100
        );
        params.setMarginStart(10);
        mark.setLayoutParams(params);

        TextView bookTitle = new TextView(requireContext());
        bookTitle.setText("歷史紀錄");
        bookTitle.setTextSize(15);
        bookTitle.setTextColor(getResources().getColor(R.color.green));
        bookTitle.setPadding(10, 10, 10, 10);

        linearLayout.addView(mark);
        linearLayout.addView(bookTitle);
        cardViewTitle.addView(linearLayout);

        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(cardViewTitle, new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    Gravity.END));
            actionBar.show();
        }
    }

    private void updateButtonState() {

        boolean hasItems = arrayList.isEmpty();
        boolean hasSelectedItems = false;
        for (HashMap<String, String> item : arrayList) {
            if ("true".equals(item.get("isSelected"))) {
                hasSelectedItems = true;
                break;
            }
        }

        //RecycleView沒有東西時的按鈕狀態
        if (hasItems) {

            btnEdit.setEnabled(false);
            btnSelect.setEnabled(false);
            btnClearAll.setEnabled(false);
            btnClearAll.setVisibility(View.INVISIBLE);

            //兩個按鈕顏色的改變
            btnEdit.setTextColor(ContextCompat.getColor(requireContext(), R.color.lightgreen));
            btnSelect.setTextColor(ContextCompat.getColor(requireContext(), R.color.lightgreen));

            //三個按鈕不能被點擊
            btnEdit.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.btn_unclickable));
            btnSelect.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.btn_unclickable));
            btnClearAll.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.btn_unclickable));
        } else {
            //RecycleView有東西時的按鈕狀態
            btnEdit.setEnabled(true);
            btnEdit.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkgreen));
            btnEdit.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.btn_additem));
        }

        //如果不在編輯模式下的按鈕狀態
        if (!isEdit) {
            btnEdit.setText("編輯");
            btnSelect.setText("套用");
            btnClearAll.setVisibility(View.INVISIBLE);
            btnClearAll.setEnabled(false);

            //不是編輯模式下的RecycleView選擇
            if (hasSelectedItems) {
                btnSelect.setEnabled(true);
                btnSelect.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkgreen));
                btnSelect.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.btn_additem));
            } else {
                btnSelect.setEnabled(false);
                btnSelect.setTextColor(ContextCompat.getColor(requireContext(), R.color.lightgreen));
                btnSelect.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.btn_unclickable));
            }
        } else {
            //在編輯模式下的按鈕狀態
            btnEdit.setText("確認");
            btnSelect.setText("刪除");
            btnClearAll.setVisibility(View.VISIBLE);
            btnClearAll.setEnabled(true);

            btnClearAll.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkgreen));
            btnClearAll.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.btn_additem));
            btnSelect.setEnabled(hasSelectedItems);
            btnSelect.setTextColor(hasSelectedItems ? ContextCompat.getColor(requireContext(), R.color.darkgreen) : ContextCompat.getColor(requireContext(), R.color.lightgreen));
            btnSelect.setBackground(ContextCompat.getDrawable(requireContext(), hasSelectedItems ? R.drawable.btn_additem : R.drawable.btn_unclickable));
        }
    }

    private void clearSelections() {
        for (HashMap<String, String> item : arrayList) {
            item.put("isSelected", "false");
        }
        listAdapterHistory.notifyDataSetChanged();
        updateButtonState(); // 更新按鈕狀態
    }

    private void ShowPopupWindow() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.popupwindow_reset_button, null, false);
        PopupWindow popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setWidth(700);
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(false);

        // 讓PopupWindow顯示出來的關鍵句
        popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0);
        popupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        // 疊加View在底下，讓她不會按到底層就跳掉
        overlayView = new View(getContext());
        overlayView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        overlayView.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.transparent));
        overlayView.setClickable(true);
        ((ViewGroup) rootView).addView(overlayView);

        // PopupWindow的文字顯示
        TextView warning = view.findViewById(R.id.txtNote);
        warning.setText("功能還沒寫好喔，請參考全部刪除!");

        // PopUpWindow的取消按鈕
        Button BTNPopup = (Button) view.findViewById(R.id.PopupCancel);
        BTNPopup.setOnClickListener(v -> {
            popupWindow.dismiss();
            // 移除疊加在底下防止點擊其他區域的View
            removeOverlayView();
        });

        // PopupWindow的確認按鈕
        /*Button btnsure = (Button) view.findViewById(R.id.Popupsure);
        btnsure.setOnClickListener(v -> {
            AppDatabaseHelper dbHelper = new AppDatabaseHelper(getActivity());
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            for (HashMap<String, String> item : arrayList) {
                if ("true".equals(item.get("isSelected"))) {
                    toRemove.add(item);
                    // 使用 SQL DELETE 語句刪除資料庫中的相應項目
                    String alarm_name = item.get("alarm_name");
                    db.execSQL("DELETE FROM history WHERE place_name = ?", new String[]{alarm_name});
                }
            }
            db.close();
            arrayList.removeAll(toRemove);
            listAdapterHistory.notifyDataSetChanged();
            removeOverlayView();
            popupWindow.dismiss();
            updateButtonState(); // 更新按鈕狀態
            if (arrayList.isEmpty()) {
                isEdit = false;
                isDelete = false;
                updateButtonState();
            }
        });*/
    }

    // 把疊加在底層的View刪掉
    private void removeOverlayView() {
        if (overlayView != null && overlayView.getParent() != null) {
            ((ViewGroup) overlayView.getParent()).removeView(overlayView);
            overlayView = null;
        }
    }
}
