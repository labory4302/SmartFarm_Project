package com.example.smart_farm;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

public class ControlActivity extends Fragment {
    LinearLayout linearLayout;
    TextView show_temp_change, show_humidity_change, show_soil_change;
    Switch changeAuto;
    Button temp_up, temp_down, humidity_up, humidity_down, soil_up, soil_down, auto_change_apply;
    private View view;

    int temp = 20;
    int humidity = 30;
    int soil = 20;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.control_page,container,false);

        changeAuto = view.findViewById(R.id.change_auto);
        linearLayout = view.findViewById(R.id.auto_layout);
        temp_up = view.findViewById(R.id.temp_up);
        temp_down = view.findViewById(R.id.temp_down);
        humidity_up = view.findViewById(R.id.humidity_up);
        humidity_down = view.findViewById(R.id.humidity_down);
        soil_up = view.findViewById(R.id.soil_up);
        soil_down = view.findViewById(R.id.soil_down);
        auto_change_apply = view.findViewById(R.id.auto_change_apply);
        show_temp_change = view.findViewById(R.id.show_temp_change);
        show_humidity_change = view.findViewById(R.id.show_humidity_change);
        show_soil_change = view.findViewById(R.id.show_soil_change);


        changeAuto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    linearLayout.setVisibility(View.VISIBLE);
                }else{
                    linearLayout.setVisibility(View.GONE);
                }
            }
        });

        temp_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                temp++;
                show_temp_change.setText(String.valueOf(temp));
            }
        });
        temp_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(temp <=0) {
                } else{
                    temp--;
                    show_temp_change.setText(String.valueOf(temp));
                }
            }
        });
        humidity_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                humidity++;
                show_humidity_change.setText(String.valueOf(humidity));
            }
        });
        humidity_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(humidity <= 0){
                } else{
                    humidity--;
                    show_humidity_change.setText(String.valueOf(humidity));
                }
            }
        });
        soil_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                soil++;
                show_soil_change.setText(String.valueOf(soil));
            }
        });
        soil_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(soil <= 0){
                }else{
                    soil--;
                    show_soil_change.setText(String.valueOf(soil));
                }
            }
        });
        //쏘는버튼
        auto_change_apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String temp_str = show_temp_change.getText().toString();
            }
        });



        return view;
    }

}