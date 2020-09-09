package com.example.diploma;

import android.app.ActivityManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.content.Context.ACTIVITY_SERVICE;
import static com.example.diploma.FSActions.getExternalStoragePath;

public class MemoryFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    Unbinder unbinder;

    @BindView(R.id.memory_chart)
    PieChart memoryChart;

    @BindView(R.id.memory_chart2)
    PieChart memoryChart2;

    @BindView(R.id.ram_chart)
    PieChart ramChart;

    @BindView(R.id.external2_memory)
    ConstraintLayout external2Memory;
    /*@BindView(R.id.textView4)
    TextView tv4;

    @BindView(R.id.textView5)
    TextView tv5;

    @BindView(R.id.textView6)
    TextView tv6;

    @BindView(R.id.textView7)
    TextView tv7;*/


    public MemoryFragment() {
        // Required empty public constructor
    }

    public static MemoryFragment newInstance(String param1, String param2) {
        MemoryFragment fragment = new MemoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_memory, container, false);
        unbinder = ButterKnife.bind(this, v);
        //EventBus.getDefault().register(this);
        return v;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Float totalSize = getTotalExternalMemorySize();
        Float availableSize = getAvailableExternalMemorySize();
        Float total2Size = getTotalExternal2MemorySize();
        Float available2Size = getAvailableExternal2MemorySize();
        Float totalRAMSize = getTotalRam();
        Float availableRAMSize = getAvailableRam();
        if(total2Size!=-1){
            makeDiagram(memoryChart2,total2Size,available2Size);
        }else external2Memory.setVisibility(View.GONE);
        if(totalSize!=-1) {
            makeDiagram(memoryChart, totalSize, availableSize);
        }
        makeDiagram(ramChart,totalRAMSize,availableRAMSize);
    }

    @Override
    public void onDestroyView() {
        //EventBus.getDefault().unregister(this);
        unbinder.unbind();
        unbinder = null;
        super.onDestroyView();
    }

    public void makeDiagram(PieChart memoryChart, float totalSize, float availableSize){
        Float percent = ((totalSize-availableSize)*100)/totalSize;
        BigDecimal bd = new BigDecimal(Double.toString(percent));
        percent = bd.setScale(2, RoundingMode.HALF_UP).floatValue();
        ArrayList<PieEntry> tmp=new ArrayList<>();
        tmp.add(new PieEntry(totalSize-availableSize, "Занято"));
        tmp.add(new PieEntry(availableSize, "Свободно"));
        int[] colorArray=new int[]{Color.rgb(153,204,96), Color.rgb(225, 250, 192)};//196, 245, 140  107, 72, 18  153,204,96
        PieDataSet pieDataSet=new PieDataSet(tmp, "");
        pieDataSet.setColors(colorArray);
        PieData pieData = new PieData(pieDataSet);
        memoryChart.setData(pieData);
        memoryChart.setDrawEntryLabels(false);
        memoryChart.setCenterText(percent.toString()+"%");
        memoryChart.setCenterTextSize(15);
        memoryChart.setHoleRadius(60);
        memoryChart.setTransparentCircleRadius(60);
        memoryChart.setTransparentCircleColor(Color.rgb(153,204,96));
        memoryChart.invalidate();
    }

    public static boolean externalMemoryAvailable() {
        String f=android.os.Environment.getExternalStorageState();
        return android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
    }

    /*public static String getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        return formatSize(availableBlocks * blockSize);
    }

    public static String getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        return formatSize(totalBlocks * blockSize);
    }*/

    public static Float getAvailableExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSizeLong();
            long availableBlocks = stat.getAvailableBlocksLong();
            return formatSizeToGB(availableBlocks * blockSize);
        } else {
            return (float)-1;
        }
    }

    public static Float getTotalExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSizeLong();
            long totalBlocks = stat.getBlockCountLong();
            return formatSizeToGB(totalBlocks * blockSize);
        } else {
            return (float)-1;
        }
    }

    public float getAvailableExternal2MemorySize() {
        File f=getExternalStoragePath(this.getActivity().getBaseContext(), true);
        if (  f!=null) {
            File path = f;
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSizeLong();
            long availableBlocks = stat.getAvailableBlocksLong();
            return formatSizeToGB(availableBlocks * blockSize);
        } else {
            return (float)-1;
        }
    }

    public float getTotalExternal2MemorySize() {
        File f=getExternalStoragePath(this.getActivity().getBaseContext(), true);
        if (  f!=null) {
            File path = f;
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSizeLong();
            long totalBlocks = stat.getBlockCountLong();
            return formatSizeToGB(totalBlocks * blockSize);
        } else {
            return (float)-1;
        }
    }

    public float getTotalRam(){
        ActivityManager actManager = (ActivityManager) getActivity().getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        actManager.getMemoryInfo(memInfo);
        long totalMemory = memInfo.totalMem;
        return formatSizeToGB(totalMemory);
    }

    public float getAvailableRam(){
        ActivityManager actManager = (ActivityManager) getActivity().getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        actManager.getMemoryInfo(memInfo);
        long totalMemory = memInfo.availMem;
        return formatSizeToGB(totalMemory);
    }

    public static float formatSizeToGB(long size){
        double res=(double)( size);
        /*if (res >= 1024) {
            res /= 1024.0;
            if (res >= 1024) {
                res /= 1024.0;
                if (res >= 1024) {
                    res /= 1024.0;
                }
            }
        }*/
        res/= Math.pow(1024,3);
        BigDecimal bd = new BigDecimal(Double.toString(res));
        res = bd.setScale(2, RoundingMode.HALF_UP).doubleValue();
        return (float)res;
    }

    public static String formatSize(long size) {
        String suffix = "B";
        double res=(double) size;
        if (res >= 1024) {
            suffix = "kB";
            res /= 1024.0;
            if (res >= 1024) {
                suffix = "MB";
                res /= 1024.0;
                if (res >= 1024) {
                    suffix = "GB";
                    res /= 1024.0;
                }
            }
        }
        BigDecimal bd = new BigDecimal(Double.toString(res));
        res = bd.setScale(2, RoundingMode.HALF_UP).doubleValue();
        StringBuilder resultBuffer = new StringBuilder(Double.toString(res));
        resultBuffer.append(suffix);
        return resultBuffer.toString();
    }
}
