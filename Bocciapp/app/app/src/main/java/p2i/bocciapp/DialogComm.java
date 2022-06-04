package p2i.bocciapp;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.service.autofill.FieldClassification;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import androidx.annotation.NonNull;

public class DialogComm extends Dialog {

    EditText edtAddress;
    EditText edtPort;
    Button btnOk;
    Switch switchMatch;
    Switch switchTraining;
    Switch switchTimer;


    interface FullNameListener {
        public void fullNameEntered(String fullName);
    }

    public DialogComm(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_dialog_comm);

        edtAddress = (EditText) findViewById(R.id.edtAddress);
        edtPort = (EditText) findViewById(R.id.edtPort);
        btnOk = (Button) findViewById(R.id.btnOk);
        switchMatch = (Switch) findViewById(R.id.switchMatch);
        switchTraining = (Switch) findViewById(R.id.switchTraining);
        switchTimer = (Switch) findViewById(R.id.switchTimer);

        switchMatch.setChecked(MatchActivity.ENABLE_MATCH);
        switchTraining.setChecked(TrainingActivity.ENABLE_TRAINING);
        switchTimer.setChecked(MatchActivity.ENABLE_TIMER);
        MatchActivity.ENABLE_MATCH = switchMatch.isChecked();
        TrainingActivity.ENABLE_TRAINING = switchTraining.isChecked();
        MatchActivity.ENABLE_TIMER = switchTimer.isChecked();

        edtAddress.setText(MainActivity.address);
        edtPort.setText(String.valueOf(MainActivity.PORT));


        this.btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonOKClick();
            }
        });

        switchMatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MatchActivity.ENABLE_MATCH = ((Switch) view).isChecked();
            }
        });

        switchTraining.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TrainingActivity.ENABLE_TRAINING = ((Switch) view).isChecked();
            }
        });

        switchTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MatchActivity.ENABLE_TIMER = ((Switch) view).isChecked();
            }
        });

    }

    private void buttonOKClick()  {
        if(!edtAddress.getText().toString().isEmpty()) {
            MainActivity.address = edtAddress.getText().toString(); //changes the IP address of the server
        }
        if(!edtPort.getText().toString().isEmpty()) {
            MainActivity.PORT = Integer.parseInt(edtPort.getText().toString()); //changes the PORT
        }
        dismiss();
    }
}
