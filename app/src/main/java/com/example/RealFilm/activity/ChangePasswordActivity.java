package com.example.RealFilm.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.RealFilm.R;
import com.example.RealFilm.model.ApiResponse;
import com.example.RealFilm.model.Status;
import com.example.RealFilm.model.User;
import com.example.RealFilm.service.ApiService;
import com.example.RealFilm.service.UserService;
import com.google.android.material.textfield.TextInputLayout;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText edittext_register_password1, edittext_register_password, edittext_register_confirm_password;
    private TextInputLayout tiplayout_register_confirm_password, tiplayout_register_password1, tiplayout_register_password;
    private Button btn_ChangePass;
    Integer userId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        initUi();

        initListener();
    }

    private void initListener() {
        btn_ChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i1 = new Intent(ChangePasswordActivity.this, LoginActivity.class);
                startActivity(i1);
            }
        });

        btnChangePassword();
    }

    private void initUi() {

        edittext_register_password1 = findViewById(R.id.edittext_register_password1234);
        edittext_register_password = findViewById(R.id.edittext_register_password12345);
        edittext_register_confirm_password = findViewById(R.id.edittext_register_confirm_password123456);
        tiplayout_register_confirm_password = findViewById(R.id.tiplayout_register_confirm_password);
        tiplayout_register_password1 = findViewById(R.id.tiplayout_register_password1);
        tiplayout_register_password = findViewById(R.id.tiplayout_register_password);
        btn_ChangePass = findViewById(R.id.btn_ChangePass);
    }

    private void btnChangePassword() {
        btn_ChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentPassword = edittext_register_password1.getText().toString().trim();
                String newPassword = edittext_register_password.getText().toString().trim();
                String newPassword1 = edittext_register_confirm_password.getText().toString().trim();

                if (currentPassword.isEmpty()) {
                    tiplayout_register_password1.setError("Please enter your current password");
                } else {
                    tiplayout_register_password1.setError(null);
                }
                if (newPassword.isEmpty()) {
                    tiplayout_register_password.setError("Please enter a new password");
                } else {
                    tiplayout_register_password.setError(null);
                }

                if (newPassword1.isEmpty() || !newPassword.equals(newPassword1)) {
                    tiplayout_register_confirm_password.setError("Please enter and confirm the password correctly");
                } else {
                    tiplayout_register_confirm_password.setError(null);
                }

                // Create an instance of the UserService interface
                UserService userService = ApiService.createService(UserService.class);

                // Make the API call to change the password
                Call<ApiResponse> changePasswordCall = userService.changePassword(currentPassword, newPassword);
                changePasswordCall.enqueue(new Callback<ApiResponse>() {
                    @Override
                    public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                        if (response.isSuccessful()) {
                            ApiResponse apiResponse = response.body();
                            if (apiResponse != null && apiResponse.getStatus() == Status.SUCCESS) {
                                // Password changed successfully
                                Toast.makeText(ChangePasswordActivity.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(ChangePasswordActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                // Handle API call failure to change password
                                Toast.makeText(ChangePasswordActivity.this, "Failed to change password", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Handle API call failure
                            Toast.makeText(ChangePasswordActivity.this, "Failed to change password", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse> call, Throwable t) {
                        // Handle API call failure
                        Toast.makeText(ChangePasswordActivity.this, "Failed to change password: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
