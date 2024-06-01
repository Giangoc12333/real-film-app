package com.example.RealFilm.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.RealFilm.R;
import com.example.RealFilm.model.ApiResponse;
import com.example.RealFilm.model.Status;
import com.example.RealFilm.service.ApiService;
import com.example.RealFilm.service.UserService;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPassword1Activity extends AppCompatActivity {
    private EditText edittext_email, edittext_reset_password, edittext_reset_confirm_password;
    private TextInputLayout tiplayout_reset_confirm_password, tiplayout_email, tiplayout_reset_password;
    private Button btn_reset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password1);
        initUi();
        initListener();
    }

    private void initListener() {
        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i1 = new Intent(ResetPassword1Activity.this, LoginActivity.class);
                startActivity(i1);
            }
        });

        btnResetPassword();
    }

    private void initUi() {


        edittext_email = findViewById(R.id.edittext_email_password);
        edittext_reset_password = findViewById(R.id.edittext_reset_password);
        edittext_reset_confirm_password = findViewById(R.id.edittext_reset_confirm_password);

        tiplayout_email = findViewById(R.id.tiplayout_email_reset_password1);
        tiplayout_reset_password = findViewById(R.id.tiplayout_reset_password);
        tiplayout_reset_confirm_password = findViewById(R.id.tiplayout_reset_confirm_password);
        btn_reset = findViewById(R.id.btn_reset_password);
    }

    private void btnResetPassword() {
        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edittext_email.getText().toString().trim();
                String newPassword = edittext_reset_password.getText().toString().trim();
                String newPassword1 = edittext_reset_confirm_password.getText().toString().trim();

                if (email.isEmpty()) {
                    tiplayout_email.setError("Please enter your Email");
                } else {
                    tiplayout_email.setError(null);
                }
                if (newPassword.isEmpty()) {
                    tiplayout_reset_password.setError("Please enter a new password");
                } else {
                    tiplayout_reset_password.setError(null);
                }

                if (newPassword1.isEmpty() || !newPassword.equals(newPassword1)) {
                    tiplayout_reset_confirm_password.setError("Please enter and confirm the password correctly");
                } else {
                    tiplayout_reset_confirm_password.setError(null);
                }

                // Create an instance of the UserService interface
                UserService userService = ApiService.createService(UserService.class);

                // Make the API call to change the password
                Call<ApiResponse> resetPasswordCall = userService.resetPassword(email, newPassword);
                resetPasswordCall.enqueue(new Callback<ApiResponse>() {
                    @Override
                    public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                        if (response.isSuccessful()) {
                            ApiResponse apiResponse = response.body();
                            if (apiResponse != null && apiResponse.getStatus() == Status.SUCCESS) {
                                // Password changed successfully
                                Toast.makeText(ResetPassword1Activity.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(ResetPassword1Activity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                // Handle API call failure to change password
                                Toast.makeText(ResetPassword1Activity.this, "Failed to change password", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Handle API call failure
                            Toast.makeText(ResetPassword1Activity.this, "Failed to change password", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse> call, Throwable t) {
                        // Handle API call failure
                        Toast.makeText(ResetPassword1Activity.this, "Failed to change password: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}