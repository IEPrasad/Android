package com.s23010155.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.s23010155.R;
import com.s23010155.databinding.FragmentWelcomeBinding;

public class WelcomeFragment extends Fragment {

    private FragmentWelcomeBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWelcomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonGoToLogin.setOnClickListener(v ->
                NavHostFragment.findNavController(WelcomeFragment.this)
                        .navigate(R.id.action_welcomeFragment_to_loginFragment));

        binding.buttonGoToRegister.setOnClickListener(v ->
                NavHostFragment.findNavController(WelcomeFragment.this)
                        .navigate(R.id.action_welcomeFragment_to_registerFragment));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 