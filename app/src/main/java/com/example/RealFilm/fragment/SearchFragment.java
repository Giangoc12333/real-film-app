package com.example.RealFilm.fragment;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.RealFilm.activity.MoviesInformationActivity;
import com.example.RealFilm.R;
import com.example.RealFilm.model.ApiResponse;
import com.example.RealFilm.model.Movie;
import com.example.RealFilm.service.ApiService;
import com.example.RealFilm.service.MovieService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class SearchFragment extends Fragment {

    private SearchView text_search;
    private GridLayout show_layout;
    private ImageView micButton;

    private SpeechRecognizer speechRecognizer;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    protected static final int RESULT_SPECH = 1;
    protected static final int RESULT_OK = -1;
    private ProgressBar progressBar;

    public SearchFragment() {
        // Required empty public constructor
    }


    public static SearchFragment newInstance(String param1, String param2) {
        SearchFragment fragment = new SearchFragment();
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
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getActivity());
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                // Xử lý sự kiện khi sẵn sàng để nhận dạng giọng nói
            }

            @Override
            public void onBeginningOfSpeech() {
                // Xử lý sự kiện khi bắt đầu nhận dạng giọng nói
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                // Xử lý sự kiện khi cường độ âm thanh thay đổi
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                // Xử lý sự kiện khi nhận được dữ liệu âm thanh
            }

            @Override
            public void onEndOfSpeech() {
                // Xử lý sự kiện khi kết thúc nhận dạng giọng nói
            }

            @Override
            public void onError(int error) {
                // Xử lý sự kiện khi xảy ra lỗi trong quá trình nhận dạng giọng nói
            }

            @Override
            public void onResults(Bundle results) {
                // Được gọi khi nhận dạng giọng nói thành công và trả về kết quả.
                ArrayList<String> voiceResults = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (voiceResults != null && !voiceResults.isEmpty()) {
                    String spokenText = voiceResults.get(0);
                    text_search.setQuery(spokenText, false); // Gán giá trị nói vào ô tìm kiếm
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                // Được gọi khi có kết quả tạm thời từ quá trình nhận dạng giọng nói.
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                // Được gọi khi có sự kiện không thuộc các phương thức trên.
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        text_search = view.findViewById(R.id.text_search);
        show_layout = view.findViewById(R.id.show_layout);
        progressBar = view.findViewById(R.id.progressBar);
        micButton = view.findViewById(R.id.mic_button);
        progressBar.setVisibility(View.INVISIBLE);
        micButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN");
                try {
                    startActivityForResult(intent, RESULT_SPECH);
                    startSpeechRecognition();
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getActivity(), "Máy của bạn không hổ trợ mic text vui lòng kiểm tra lại", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });

        // Gọi phương thức để bắt đầu nhận dạng giọng nói khi click vào biểu tượng mic
        callApi();
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_SPECH) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> textList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (textList != null && textList.size() > 0) {
                    String recognizedText = textList.get(0);

                    text_search.setQuery(recognizedText, false);


                }
            }
        }
    }

    private void startSpeechRecognition() {
        if (speechRecognizer != null) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Nói để tìm kiếm");

            speechRecognizer.startListening(intent);
        }
    }

    private void callApi() {
        text_search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!text_search.getQuery().toString().isEmpty()) {

                    // Clear the current views in the GridLayout.
                    show_layout.removeAllViewsInLayout();

                    // Show loading
                    progressBar.setVisibility(View.VISIBLE);


                    MovieService movieService = ApiService.createService(MovieService.class);
                    Call<ApiResponse<List<Movie>>> call = movieService.searchMovie(query);
                    call.enqueue(new Callback<ApiResponse<List<Movie>>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<List<Movie>>> call, Response<ApiResponse<List<Movie>>> response) {
                            if (response.isSuccessful()) {
                                List<Movie> movies = response.body().getData();
                                if (movies != null && !movies.isEmpty()) {
                                    mappingMovie(movies);
                                } else {
                                    noResult();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<List<Movie>>> call, Throwable t) {

                        }
                    });
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }


    private void noResult() {
        progressBar.setVisibility(View.INVISIBLE);
        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(45));
        params2.setMargins(0, dpToPx(10), 0, 0);
        TextView textView = new TextView(getActivity());
        textView.setLayoutParams(params2);
        textView.setText("Không tìm thấy kết quả!");
        textView.setTextSize(18);
        show_layout.addView(textView);
    }


    private void mappingMovie(List<Movie> movies) {
        // Xóa các view hiện tại trong GridLayout.
        show_layout.removeAllViewsInLayout();

        // Tạo một danh sách tạm thời để lưu trữ các ID phim đã hiển thị.
        List<Integer> displayedMovieIds = new ArrayList<>();

        for (Movie movie : movies) {
            int movieId = movie.getId();

            // Kiểm tra xem phim đã được hiển thị trước đó chưa.
            if (displayedMovieIds.contains(movieId)) {
                continue; // Bỏ qua phim nếu đã được hiển thị.
            }

            // Thêm ID phim vào danh sách đã hiển thị.
            displayedMovieIds.add(movieId);

            // Các phần còn lại của mã xử lý hiển thị phim như trước.
            String movieName = movie.getTitle();
            String posterUrl = movie.getPosterHorizontal();

            LinearLayout parent = new LinearLayout(getActivity());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(dpToPx(15), dpToPx(15), dpToPx(15), 0);
            parent.setLayoutParams(params);
            parent.setOrientation(LinearLayout.VERTICAL);
            parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), MoviesInformationActivity.class);
                    intent.putExtra("id", movieId);
                    startActivity(intent);
                }
            });

            // Tạo các view khác như CardView, ImageView và TextView.

            CardView cardView1 = new CardView(getActivity());
            cardView1.setLayoutParams(new CardView.LayoutParams(dpToPx(165), dpToPx(220)));
            cardView1.setRadius(dpToPx(15));
            cardView1.setCardBackgroundColor(getResources().getColor(R.color.white));

            CardView.LayoutParams params3 = new CardView.LayoutParams(dpToPx(160), dpToPx(215), Gravity.CENTER);
            CardView cardView2 = new CardView(getActivity());
            cardView2.setLayoutParams(params3);
            cardView2.setRadius(dpToPx(15));

            ImageView imageView = new ImageView(getActivity());
            Glide.with(getActivity()).load(posterUrl).into(imageView);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            imageView.setBackgroundResource(R.drawable.null_image34);

            LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(45));
            params2.setMargins(0, dpToPx(10), 0, 0);
            TextView textView = new TextView(getActivity());
            textView.setLayoutParams(params2);
            textView.setText(movieName);
            textView.setTextSize(18);
            textView.setMaxLines(2);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            textView.setTextColor(getResources().getColor(R.color.white));

            show_layout.addView(parent);
            parent.addView(cardView1);
            cardView1.addView(cardView2);
            cardView2.addView(imageView);
            parent.addView(textView);
        }
        progressBar.setVisibility(View.INVISIBLE);
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

}