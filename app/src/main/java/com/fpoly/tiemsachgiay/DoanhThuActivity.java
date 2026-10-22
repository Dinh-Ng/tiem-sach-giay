package com.fpoly.tiemsachgiay;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fpoly.tiemsachgiay.adapter.HoaDonAdapter;
import com.fpoly.tiemsachgiay.dao.HoaDonDAO;
import com.fpoly.tiemsachgiay.model.HoaDon;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DoanhThuActivity extends AppCompatActivity {

    private static final int MODE_DAY = 0;
    private static final int MODE_WEEK = 1;
    private static final int MODE_MONTH = 2;

    private int currentMode = MODE_DAY;
    private final Calendar selectedCal = Calendar.getInstance();

    private MaterialButtonToggleGroup toggleGroup;
    private MaterialButton btnSelectDate;
    private TextView tvTotalRevenue, tvInvoiceCount, tvAvgRevenue, tvTimePeriodLabel, tvEmptyMessage;
    private ProgressBar pbLoading;
    private LinearLayout layoutEmpty;
    private RecyclerView rvHoaDon;

    private HoaDonDAO hoaDonDAO;
    private HoaDonAdapter adapter;

    private final List<HoaDon> allInvoices = new ArrayList<>();
    private final List<HoaDon> matchingInvoices = new ArrayList<>();
    private final DecimalFormat formatter = new DecimalFormat("#,### VNĐ");
    private final SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat sdfDisplayDay = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat sdfDisplayMonth = new SimpleDateFormat("MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_doanh_thu);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        hoaDonDAO = new HoaDonDAO(this);

        initViews();
        setupListeners();
        setupRecyclerView();

        loadInvoicesFromFirestore();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btn_back_doanh_thu);
        toggleGroup = findViewById(R.id.toggle_group_time_type);
        btnSelectDate = findViewById(R.id.btn_select_date);
        tvTotalRevenue = findViewById(R.id.tv_total_revenue);
        tvInvoiceCount = findViewById(R.id.tv_invoice_count);
        tvAvgRevenue = findViewById(R.id.tv_avg_revenue);
        tvTimePeriodLabel = findViewById(R.id.tv_time_period_label);
        tvEmptyMessage = findViewById(R.id.tv_empty_doanh_thu_message);
        pbLoading = findViewById(R.id.pb_loading_doanh_thu);
        layoutEmpty = findViewById(R.id.layout_empty_doanh_thu);
        rvHoaDon = findViewById(R.id.rv_doanh_thu_hoa_don);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupListeners() {
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btn_tab_ngay) {
                    currentMode = MODE_DAY;
                } else if (checkedId == R.id.btn_tab_tuan) {
                    currentMode = MODE_WEEK;
                } else if (checkedId == R.id.btn_tab_thang) {
                    currentMode = MODE_MONTH;
                }
                calculateRevenue();
            }
        });

        btnSelectDate.setOnClickListener(v -> showDatePickerDialog());
    }

    private void setupRecyclerView() {
        rvHoaDon.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HoaDonAdapter(this, matchingInvoices);
        rvHoaDon.setAdapter(adapter);
    }

    private void showDatePickerDialog() {
        int year = selectedCal.get(Calendar.YEAR);
        int month = selectedCal.get(Calendar.MONTH);
        int day = selectedCal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, (view, yearSelected, monthSelected, daySelected) -> {
            selectedCal.set(Calendar.YEAR, yearSelected);
            selectedCal.set(Calendar.MONTH, monthSelected);
            selectedCal.set(Calendar.DAY_OF_MONTH, daySelected);
            calculateRevenue();
        }, year, month, day);

        dialog.show();
    }

    private void loadInvoicesFromFirestore() {
        pbLoading.setVisibility(View.VISIBLE);
        rvHoaDon.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);

        hoaDonDAO.getAll(new HoaDonDAO.OnDataListLoadedListener<HoaDon>() {
            @Override
            public void onSuccess(List<HoaDon> data) {
                pbLoading.setVisibility(View.GONE);
                allInvoices.clear();
                if (data != null) {
                    allInvoices.addAll(data);
                }
                calculateRevenue();
            }

            @Override
            public void onFailure(String message) {
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(DoanhThuActivity.this, message, Toast.LENGTH_SHORT).show();
                allInvoices.clear();
                calculateRevenue();
            }
        });
    }

    private void calculateRevenue() {
        matchingInvoices.clear();
        double totalRevenue = 0;

        if (currentMode == MODE_DAY) {
            String targetDayStr = sdfDate.format(selectedCal.getTime());
            String displayDayStr = sdfDisplayDay.format(selectedCal.getTime());
            btnSelectDate.setText("Ngày chọn: " + displayDayStr);
            tvTimePeriodLabel.setText("Doanh thu ngày " + displayDayStr);

            for (HoaDon hd : allInvoices) {
                // Chỉ tính hóa đơn Đã thanh toán và đúng ngày chọn
                if ("Đã thanh toán".equalsIgnoreCase(hd.getTrangThai())) {
                    if (targetDayStr.equalsIgnoreCase(hd.getNgayLap())) {
                        matchingInvoices.add(hd);
                        totalRevenue += hd.getTongTien();
                    }
                }
            }

        } else if (currentMode == MODE_WEEK) {
            Calendar calStart = (Calendar) selectedCal.clone();
            calStart.setFirstDayOfWeek(Calendar.MONDAY);
            calStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            calStart.set(Calendar.HOUR_OF_DAY, 0);
            calStart.set(Calendar.MINUTE, 0);
            calStart.set(Calendar.SECOND, 0);
            calStart.set(Calendar.MILLISECOND, 0);

            Calendar calEnd = (Calendar) calStart.clone();
            calEnd.add(Calendar.DAY_OF_YEAR, 6);
            calEnd.set(Calendar.HOUR_OF_DAY, 23);
            calEnd.set(Calendar.MINUTE, 59);
            calEnd.set(Calendar.SECOND, 59);

            String weekLabel = sdfDisplayDay.format(calStart.getTime()) + " - " + sdfDisplayDay.format(calEnd.getTime());
            btnSelectDate.setText("Tuần chọn: " + weekLabel);
            tvTimePeriodLabel.setText("Doanh thu tuần (" + weekLabel + ")");

            for (HoaDon hd : allInvoices) {
                if ("Đã thanh toán".equalsIgnoreCase(hd.getTrangThai()) && hd.getNgayLap() != null) {
                    try {
                        Date hdDate = sdfDate.parse(hd.getNgayLap());
                        if (hdDate != null && !hdDate.before(calStart.getTime()) && !hdDate.after(calEnd.getTime())) {
                            matchingInvoices.add(hd);
                            totalRevenue += hd.getTongTien();
                        }
                    } catch (Exception ignored) {
                    }
                }
            }

        } else if (currentMode == MODE_MONTH) {
            int targetYear = selectedCal.get(Calendar.YEAR);
            int targetMonth = selectedCal.get(Calendar.MONTH); // 0-based
            String monthLabel = sdfDisplayMonth.format(selectedCal.getTime());

            btnSelectDate.setText("Tháng chọn: Tháng " + monthLabel);
            tvTimePeriodLabel.setText("Doanh thu Tháng " + monthLabel);

            for (HoaDon hd : allInvoices) {
                if ("Đã thanh toán".equalsIgnoreCase(hd.getTrangThai()) && hd.getNgayLap() != null) {
                    try {
                        Date hdDate = sdfDate.parse(hd.getNgayLap());
                        if (hdDate != null) {
                            Calendar tempCal = Calendar.getInstance();
                            tempCal.setTime(hdDate);
                            if (tempCal.get(Calendar.YEAR) == targetYear && tempCal.get(Calendar.MONTH) == targetMonth) {
                                matchingInvoices.add(hd);
                                totalRevenue += hd.getTongTien();
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        int count = matchingInvoices.size();
        double avgRevenue = count > 0 ? (totalRevenue / count) : 0;

        tvTotalRevenue.setText(formatter.format(totalRevenue));
        tvInvoiceCount.setText(count + " hóa đơn");
        tvAvgRevenue.setText(formatter.format(avgRevenue));

        adapter.setListHoaDon(matchingInvoices);

        if (matchingInvoices.isEmpty()) {
            rvHoaDon.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            rvHoaDon.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }
}