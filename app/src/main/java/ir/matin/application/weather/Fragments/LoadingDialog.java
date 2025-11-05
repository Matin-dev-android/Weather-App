package ir.matin.application.weather.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import ir.matin.application.weather.R;
import ir.matin.application.weather.Ui.ScaleBtn;

public class LoadingDialog extends DialogFragment {
    private Context context ;
    TextView message;
    TextView title ;

    @SuppressLint({"ClickableViewAccessibility", "MissingInflatedId"})
    @Nullable
    @Override

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.loading_dialog,container,false);
        getDialog().setCancelable(false);
        Button button = view.findViewById(R.id.closeAppBtn);
        button.setOnTouchListener(new ScaleBtn(context));
        button.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SuspiciousIndentation")
            @Override
            public void onClick(View v) {
                dismiss();
                if (getActivity()!= null)
                getActivity().finish();
            }
        });
        message = view.findViewById(R.id.text5_4);
        title = view.findViewById(R.id.dialogTitle);
        return view;
    }

    public LoadingDialog(@Nullable Context context) {
        this.context = context;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        else {
            Log.e("DIALOG : ","something is null");
        }
    }

    public void setMessage(String message){
        if (this.message != null){
            this.message.setText(message);
        }
    }
    public void setTitle(String title){
        if (this.title != null){
            this.title.setText(title);
        }
    }
}
