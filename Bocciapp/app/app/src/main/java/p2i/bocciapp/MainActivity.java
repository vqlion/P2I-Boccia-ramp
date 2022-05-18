package p2i.bocciapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    Button btnMatch;
    Button btnTrain;
    ImageButton btnComm;

    Context context = this;

    public static int PORT = 5000; //port used by the server
    public static String address = "192.168.53.233"; //IP address of the server


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        btnMatch = (Button) findViewById(R.id.btnMatch);
        btnTrain = (Button) findViewById(R.id.btnTrain);
        btnComm = (ImageButton) findViewById(R.id.btnComm); //btn to change the server settings

        btnMatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, MatchActivity.class);
                startActivity(i);
            }
        });

        btnTrain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, TrainingActivity.class);
                startActivity(i);
            }
        });

        btnComm.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                (new DialogComm(context)).show();
            }
        });
    }
}