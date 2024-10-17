package com.example.personalizedskincareproductsrecommendation;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {
    private ImageView back;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        back = view.findViewById(R.id.back);
        viewPager = view.findViewById(R.id.viewpager);
        tabLayout = view.findViewById(R.id.tab_layout);

        // Retrieve userId from arguments
        if (getArguments() != null) {
            userId = getArguments().getString("ARG_USER_ID");
            Log.d("HistoryFragment", "User ID: " + userId); // Log userId
        } else {
            Log.e("HistoryFragment", "Arguments are null!"); // Log if arguments are null
        }

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backHomepage();
            }
        });

        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);

        return view;
    }

    private void setupViewPager(ViewPager viewPager) {
        HistoryViewPagerAdapter adapter = new HistoryViewPagerAdapter(getChildFragmentManager());

        // Create instances of the fragments with userId passed as an argument
        SkinLogHistory skinLogHistoryFragment = new SkinLogHistory();
        SkinAnalysisHistory skinAnalysisHistoryFragment = new SkinAnalysisHistory();

        // Create a bundle to pass userId
        Bundle args = new Bundle();
        args.putString("ARG_USER_ID", userId);

        // Set the arguments for each fragment
        skinLogHistoryFragment.setArguments(args);
        skinAnalysisHistoryFragment.setArguments(args);

        // Add Fragments for Active and Deactivated Users
        adapter.addFragment(skinLogHistoryFragment, "Skin Log");
        adapter.addFragment(skinAnalysisHistoryFragment, "Skin Analysis");

        viewPager.setAdapter(adapter);
    }


    public void backHomepage() {
        HomeFragment homeFragment = HomeFragment.newInstance(userId);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, homeFragment)
                .commit();
    }


}
