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
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.net.*;
import java.util.Objects;

public class TrainingActivity extends AppCompatActivity {

    public static final String TAG = "MyLogsTraining"; //this tag is only used to sort the logs I write from the ones the app writes by itself

    Button btnBack;
    Button btnStop;
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

    TextView tvPos1;
    TextView tvPos2;
    TextView tvPos3;
    TextView tvPos4;

    ImageButton btnPlay1;
    ImageButton btnPlay2;
    ImageButton btnPlay3;
    ImageButton btnPlay4;
    ImageButton btnDel1;
    ImageButton btnDel2;
    ImageButton btnDel3;
    ImageButton btnDel4;

    TableLayout tablePos;

    Graphics graphics;

    int anglePos1;
    int heightPos1;
    int anglePos2;
    int heightPos2;
    int anglePos3;
    int heightPos3;
    int anglePos4;
    int heightPos4;

    int progressHeight;
    int progressAngle;
    int updateValuesTimeout = 150;
    int longPressTimeout = 800;

    boolean mode;
    boolean modePos;
    boolean heightMoving;
    boolean angleMoving;

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

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_training);

        btnBack = (Button) findViewById(R.id.btnBackA);
        btnStop = (Button) findViewById(R.id.btnStopA);
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
        sbAngle.setMax(99);
        sbHeight.setMax(99);

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

        tvPos1 = (TextView) findViewById(R.id.tvPos1);
        tvPos2 = (TextView) findViewById(R.id.tvPos2);
        tvPos3 = (TextView) findViewById(R.id.tvPos3);
        tvPos4 = (TextView) findViewById(R.id.tvPos4);

        btnPlay1 = (ImageButton) findViewById(R.id.btnPlay1);
        btnPlay2 = (ImageButton) findViewById(R.id.btnPlay2);
        btnPlay3 = (ImageButton) findViewById(R.id.btnPlay3);
        btnPlay4 = (ImageButton) findViewById(R.id.btnPlay4);

        btnDel1 = (ImageButton) findViewById(R.id.btnDel1);
        btnDel2 = (ImageButton) findViewById(R.id.btnDel2);
        btnDel3 = (ImageButton) findViewById(R.id.btnDel3);
        btnDel4 = (ImageButton) findViewById(R.id.btnDel4);

        tablePos = (TableLayout) findViewById(R.id.tablePos);
        tablePos.setVisibility(View.GONE);

        layoutRand = (LinearLayout) findViewById(R.id.layoutRand);
        layoutRand.setVisibility(View.GONE);

        progressHeight = sbHeight.getProgress();
        progressAngle = sbAngle.getProgress();

        tvHeightUp.setText(" " + progressHeight);
        tvAngleUp.setText(" " + progressAngle);

        mode = false;
        modePos = false;

        //starts the UDP communication (socket) with the server specified in address
        try {
            UDPSocket = new DatagramSocket();
            host = InetAddress.getByName(MainActivity.address);
            UDPSend("open:train:");
            UDPReceive();
        } catch (Exception e) {
            Log.e(TAG, "error socket  " + Objects.requireNonNull(e.getCause()));
        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mode) {
                    tablePos.setVisibility(View.GONE);
                    graphics.setVisibility(View.GONE);
                    layoutRand.setVisibility(View.GONE);
                    mode = false;
                    modePos = false;
                } else {
                    UDPSocket.close();
                    finish();
                }
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
                tablePos.setVisibility(View.GONE);
                mode = true;
                modePos = false;
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

        btnPosMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tablePos.setVisibility(View.VISIBLE);
                graphics.setVisibility(View.GONE);
                layoutRand.setVisibility(View.GONE);
                mode = true;
                modePos = true;
            }
        });

        btnPlay1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doBtnPlay((ImageButton) view);
            }
        });

        btnDel1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doBtnDel((ImageButton) view);
            }
        });

        btnPlay2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doBtnPlay((ImageButton) view);
            }
        });

        btnDel2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doBtnDel((ImageButton) view);
            }
        });

        btnPlay3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doBtnPlay((ImageButton) view);
            }
        });

        btnDel3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doBtnDel((ImageButton) view);
            }
        });

        btnPlay4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doBtnPlay((ImageButton) view);
            }
        });

        btnDel4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doBtnDel((ImageButton) view);
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
                if (!modePos) setHeightPos(progressHeight);
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
                if (!modePos) setAnglePos(progressAngle);
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
                if (!modePos) setHeightPos(progressHeight);
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
                if (!modePos) setAnglePos(progressAngle);
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
                updateTextView(tvHeightUp, progressHeight);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (!modePos) setHeightPos(progressHeight);
            }
        });

        sbAngle.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progress = i;
                progressAngle = progress;
                updateTextView(tvAngleUp, progressAngle);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (!modePos) setAnglePos(progressAngle);
            }
        });
    }

    /**
     * Method to execute when a play button is pressed
     *
     * @param v button pressed
     */
    @SuppressLint("SetTextI18n")
    public void doBtnPlay(ImageButton v) {
        if (v == btnPlay1) {
            if (tvPos1.getText().equals("")) {
                tvPos1.setText(tvAngleUp.getText() + " ; " + tvHeightUp.getText());
                anglePos1 = progressAngle;
                heightPos1 = progressHeight;
            } else {
                setRampPos(anglePos1, heightPos1);
            }
        }
        if (v == btnPlay2) {
            if (tvPos2.getText().equals("")) {
                tvPos2.setText(tvAngleUp.getText() + " ; " + tvHeightUp.getText());
                anglePos2 = progressAngle;
                heightPos2 = progressHeight;
            } else {
                setRampPos(anglePos2, heightPos2);
            }
        }
        if (v == btnPlay3) {
            if (tvPos3.getText().equals("")) {
                tvPos3.setText(tvAngleUp.getText() + " ; " + tvHeightUp.getText());
                anglePos3 = progressAngle;
                heightPos3 = progressHeight;
            } else {
                setRampPos(anglePos3, heightPos3);
            }
        }
        if (v == btnPlay4) {
            if (tvPos4.getText().equals("")) {
                tvPos4.setText(tvAngleUp.getText() + " ; " + tvHeightUp.getText());
                anglePos4 = progressAngle;
                heightPos4 = progressHeight;
            } else {
                setRampPos(anglePos4, heightPos4);
            }
        }
    }

    /**
     * Method to execute when a delete button is pressed
     *
     * @param v button pressed
     */
    public void doBtnDel(ImageButton v) {
        if (v == btnDel1) {
            tvPos1.setText("");
            anglePos1 = 0;
            heightPos1 = 0;
        }
        if (v == btnDel2) {
            tvPos2.setText("");
            anglePos2 = 0;
            heightPos2 = 0;
        }
        if (v == btnDel3) {
            tvPos3.setText("");
            anglePos3 = 0;
            heightPos3 = 0;
        }
        if (v == btnDel4) {
            tvPos4.setText("");
            anglePos4 = 0;
            heightPos4 = 0;
        }
    }

    /**
     * Sends UDP message to the ramp with a specific height and angle
     *
     * @param angle  the angle the ramp should move to
     * @param height the height the ramp sould be at
     */
    public void setRampPos(int angle, int height) {
        setAnglePos(angle);
        setHeightPos(height);
        showToast("Ne changez pas la position de la rampe jusqu'à ce qu'elle s'arrête", Toast.LENGTH_SHORT);
        Log.i(TAG, "ramp pos : " + angle + "   " + height);
    }

    /**
     * Sends UDP message to the ramp with a spcific angle
     *
     * @param angle the angle the ramp should move to
     */
    public void setAnglePos(int angle) {
        String a = "angl:";
        a += angle < 10 ? 0 : angle / 10;
        a += "ten:" + angle % 10;
        UDPSend(a);
    }

    /**
     * Sends UDP message to the ramp with a specific height
     *
     * @param height the heihgt the ramp should be at
     */
    public void setHeightPos(int height) {
        String h = "heig:";
        h += height < 10 ? 0 : height / 10;
        h += "ten:" + height % 10;
        UDPSend(h);
    }

    /**
     * Updates a given textview.
     *
     * @param t textview to update
     * @param n content of the textview
     */
    @SuppressLint("SetTextI18n")
    private void updateTextView(TextView t, int n) {
        t.setText(" " + n);
    }

    /**
     * Shows a toast on top of the app
     *
     * @param text   text to display in the toast
     * @param length length duration of the toast
     */
    private void showToast(String text, int length) {
        Toast toast = Toast.makeText(getApplicationContext(), text, length);
        toast.show();
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
                    Log.e(TAG, "error send  " + Objects.requireNonNull(e.getCause()));
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