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
import com.s23010155.databinding.FragmentLogoutBinding;
public class LogoutFragment extends Fragment {
    private FragmentLogoutBinding binding;
    @Override public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle s) {
        binding = FragmentLogoutBinding.inflate(i, c, false);
        return binding.getRoot();
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.buttonConfirmLogout.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_logoutFragment_to_loginFragment));
    }
    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
} 