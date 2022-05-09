package p2i.bocciapp;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;

public class DialogComm extends Dialog {

    EditText edtAddress;
    EditText edtPort;
    Button btnOk;


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

        edtAddress.setText(MainActivity.address);
        edtPort.setText(String.valueOf(MainActivity.PORT));


        this.btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonOKClick();
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


