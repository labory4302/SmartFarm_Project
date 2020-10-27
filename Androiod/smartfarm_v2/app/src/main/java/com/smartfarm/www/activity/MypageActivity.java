package com.smartfarm.www.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.smartfarm.www.R;

public class MypageActivity extends Fragment {
    private View view;

    TextView mypage_nickname, mypage_email, mypage_location;
    Button changemyinformation_button, notification_button, event_button, inquiry_button, version_button;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.mypage_page,container,false);
        mypage_nickname = view.findViewById(R.id.mypage_nickname);
        mypage_email = view.findViewById(R.id.mypage_email);
        mypage_location = view.findViewById(R.id.mypage_location);
        changemyinformation_button = view.findViewById(R.id.changemyinformation_button);
        notification_button = view.findViewById(R.id.notification_button);
        event_button = view.findViewById(R.id.event_button);
        inquiry_button = view.findViewById(R.id.inquiry_button);
        version_button = view.findViewById(R.id.version_button);


        changemyinformation_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ChangeMyInformationActivity.class);
                startActivity(intent);
            }
        });

        notification_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        event_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        inquiry_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        version_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), VersionActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

}