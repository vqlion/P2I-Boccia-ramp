package p2i.bocciapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.net.*;

public class TrainingActivity extends AppCompatActivity {

    public static final String TAG = "MyLogsTraining"; //this tag is only used to sort the logs I write from the ones the app writes by itself

    Button btnBack;
    Button btnStop;
    Button btnLaunch;
    Button btnRandMode;
    Button btnPosMode;
    ImageButton btnChangeRand;

    LinearLayout layoutControls;
    LinearLayout layoutRand;

    ImageButton ibPlusAngle;
    ImageButton ibMinusAngle;
    ImageButton ibPlusHeight;
    ImageButton ibMinusHeight;

    SeekBar sbHeight;
    SeekBar sbAngle;

    TextView tvAngle;
    TextView tvHeight;
    TextView tvAngleUp;
    TextView tvHeightUp;
    TextView tvRand;

    Graphics graphics;

    int progressHeight;
    int progressAngle;
    int updateValuesTimeout = 150;
    int longPressTimeout = 800;

    InetAddress host;
    static DatagramSocket UDPSocket;
    String serverMessage;

    Handler timerHandler = new Handler();
    Runnable timerRunnablePlusHeight = new Runnable() {

        @Override
        public void run() {
            progressHeight++;
            updateTextView(tvHeightUp, progressHeight);
            sbHeight.setProgress(progressHeight);
            timerHandler.postDelayed(this, updateValuesTimeout);
        }
    };
    Runnable timerRunnableMinusHeight = new Runnable() {

        @Override
        public void run() {
            progressHeight--;
            updateTextView(tvHeightUp, progressHeight);
            sbHeight.setProgress(progressHeight);
            timerHandler.postDelayed(this, updateValuesTimeout);
        }
    };
    Runnable timerRunnablePlusAngle = new Runnable() {

        @Override
        public void run() {
            progressAngle++;
            updateTextView(tvAngleUp, progressAngle);
            sbAngle.setProgress(progressAngle);
            timerHandler.postDelayed(this, updateValuesTimeout);
        }
    };
    Runnable timerRunnableMinusAngle = new Runnable() {

        @Override
        public void run() {
            progressAngle--;
            updateTextView(tvAngleUp, progressAngle);
            sbAngle.setProgress(progressAngle);
            timerHandler.postDelayed(this, updateValuesTimeout);
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_training);

        btnBack = (Button) findViewById(R.id.btnBackA);
        btnStop = (Button) findViewById(R.id.btnStopA);
        btnLaunch = (Button) findViewById(R.id.btnLaunchA);
        btnRandMode = (Button) findViewById(R.id.btnRandMode);
        btnPosMode = (Button) findViewById(R.id.btnPosMode);
        btnChangeRand = (ImageButton) findViewById(R.id.btnChangeRand);
        //btnChangeRand.setVisibility(View.GONE);

        ibPlusAngle = (ImageButton) findViewById(R.id.ibPlusAngle);
        ibPlusHeight = (ImageButton) findViewById(R.id.ibPlusHeight);
        ibMinusAngle = (ImageButton) findViewById(R.id.ibMinusAngle);
        ibMinusHeight = (ImageButton) findViewById(R.id.ibMinusHeight);

        sbAngle = (SeekBar) findViewById(R.id.sbAngle);
        sbHeight = (SeekBar) findViewById(R.id.sbHeight);

        tvHeight = (TextView) findViewById(R.id.tvHeight);
        tvAngle = (TextView) findViewById(R.id.tvAngle);
        tvAngleUp = (TextView) findViewById(R.id.tvAngleUp);
        tvHeightUp = (TextView) findViewById(R.id.tvHeightUp);
        tvRand = (TextView) findViewById(R.id.tvRand);
        //tvRand.setVisibility(View.GONE);

        graphics = (Graphics) findViewById(R.id.graphics);
        graphics.setVisibility(View.GONE);

        layoutControls = (LinearLayout) findViewById(R.id.layoutControls);
        //layoutControls.setVisibility(View.GONE);

        layoutRand = (LinearLayout) findViewById(R.id.layoutRand);
        layoutRand.setVisibility(View.GONE);

        progressHeight = sbHeight.getProgress();
        progressAngle = sbAngle.getProgress();

        tvHeightUp.setText(" " + progressHeight);
        tvAngleUp.setText(" " + progressAngle);

        //starts the UDP communication (socket) with the server specified in address
        try {
            UDPSocket = new DatagramSocket();
            host = InetAddress.getByName(MainActivity.address);
            UDPSend("open:train:");
            UDPReceive();
        } catch (Exception e) {
            Log.e(TAG, "error socket  " + e.getCause().toString());
        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UDPSocket.close();
                finish();
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog("La machine a été arretée." + "\n" + "\n" + "Appuie sur OK pour relancer la machine.", "OK", R.style.DialogThemeAlert);
                UDPSend("stop:stop");
            }
        });

        btnRandMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                graphics.setVisibility(View.VISIBLE);
                layoutRand.setVisibility(View.VISIBLE);
            }
        });

        btnPosMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        btnChangeRand.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    btnChangeRand.setImageResource(R.drawable.ic_baseline_change_circle_clicked_24);
                    graphics.invalidate();
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    btnChangeRand.setImageResource(R.drawable.ic_baseline_change_circle_24);
                }

                return false;
            }
        });

        ibPlusHeight.setOnTouchListener((view, motionEvent) -> {

            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                ibPlusHeight.setImageResource(R.drawable.plus_clicked); //sets a different image when the button is clicked
                progressHeight++;
                updateTextView(tvHeightUp, progressHeight);
                sbHeight.setProgress(progressHeight);
                timerHandler.postDelayed(timerRunnablePlusHeight, longPressTimeout);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                ibPlusHeight.setImageResource(R.drawable.plus); //sets the original image back when the button is unclicked
                timerHandler.removeCallbacks(timerRunnablePlusHeight);
            }
            return false;
        });

        ibPlusAngle.setOnTouchListener((view, motionEvent) -> {

            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                ibPlusAngle.setImageResource(R.drawable.plus_clicked); //sets a different image when the button is clicked
                progressAngle++;
                updateTextView(tvAngleUp, progressAngle);
                sbAngle.setProgress(progressAngle);
                timerHandler.postDelayed(timerRunnablePlusAngle, longPressTimeout);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                ibPlusAngle.setImageResource(R.drawable.plus); //sets the original image back when the button is unclicked
                timerHandler.removeCallbacks(timerRunnablePlusAngle);
            }
            return false;
        });

        ibMinusHeight.setOnTouchListener((view, motionEvent) -> {

            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                ibMinusHeight.setImageResource(R.drawable.minus_clicked); //sets a different image when the button is clicked
                progressHeight--;
                updateTextView(tvHeightUp, progressHeight);
                sbHeight.setProgress(progressHeight);
                timerHandler.postDelayed(timerRunnableMinusHeight, longPressTimeout);

            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                ibMinusHeight.setImageResource(R.drawable.minus); //sets the original image back when the button is unclicked
                timerHandler.removeCallbacks(timerRunnableMinusHeight);
            }
            return false;
        });

        ibMinusAngle.setOnTouchListener((view, motionEvent) -> {

            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                ibMinusAngle.setImageResource(R.drawable.minus_clicked); //sets a different image when the button is clicked
                progressAngle--;
                updateTextView(tvAngleUp, progressAngle);
                sbAngle.setProgress(progressAngle);
                timerHandler.postDelayed(timerRunnableMinusAngle, longPressTimeout);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                ibMinusAngle.setImageResource(R.drawable.minus); //sets the original image back when the button is unclicked
                timerHandler.removeCallbacks(timerRunnableMinusAngle);
            }
            return false;
        });

        sbHeight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progress = i;
                progressHeight = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updateTextView(tvHeightUp, progressHeight);

            }
        });

        sbAngle.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progress = i;
                progressAngle = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updateTextView(tvAngleUp, progressAngle);

            }
        });
    }

    /**
     * Updates a given textview.
     *
     * @param t textview to update
     * @param n content of the textview
     */
    private void updateTextView(TextView t, int n) {
        t.setText(" " + n);
    }

    /**
     * Shows a dialog on top of the app. The method does different things depending on which buttons are displayed
     *
     * @param text          text displayed in the dialog
     * @param textButtonPos text displayed for the positive button
     * @param style         style of the dialog
     */
    private void showDialog(String text, String textButtonPos, int style) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this, style);
        dialog.setMessage(text);
        dialog.setCancelable(false); //set to false so the dialog does not disappear if you click anywhere outside

        dialog.setPositiveButton(
                textButtonPos,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        UDPSend("clos:stop");
                        dialog.cancel();
                    }
                });

        AlertDialog alert = dialog.create();
        alert.show();
    }

    /**
     * Sends a UDP message to a specified server. The server is specified in the MainActivity and the socket is open in the onCreate method.
     *
     * @param m message sent to the server via UDP
     */
    public void UDPSend(String m) {
        final byte[] b = m.getBytes();
        Thread t = new Thread() {
            public void run() {
                try {
                    DatagramPacket packet = new DatagramPacket(b, b.length, host, MainActivity.PORT);
                    UDPSocket.send(packet);
                    Log.d(TAG, "apres send");
                } catch (Exception e) {
                    Log.e(TAG, "error send  " + e.getCause().toString());
                }
            }
        };
        t.start();
    }

    /**
     * Triggers the receiving of a UDP packet from the server via the Receive class
     */
    public void UDPReceive() {
        (new ReceiveTraining()).execute();
    }

    /**
     * Converts a received message from the server in a String, then computes it. This method is called by the Receive class.
     *
     * @param b byte array to convert back in String
     */
    public void fetchReceive(byte[] b) {
        serverMessage = new String(b);
        Log.i(TAG, "message reçu par le serveur : " + serverMessage);
        if (serverMessage.startsWith("open:")) {
            Log.i(TAG, "connecté");
        }
    }
}