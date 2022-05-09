package p2i.bocciapp;

import android.os.AsyncTask;
import android.util.Log;

import java.net.DatagramPacket;

public class ReceiveTraining extends AsyncTask<Void, byte[], Void> {
    protected Void doInBackground(Void... voids) {
        try {
            while (true) {
                byte[] data = new byte[1024]; //buffer that will contain the data
                DatagramPacket packet = new DatagramPacket(data, data.length);
                TrainingActivity.UDPSocket.receive(packet);
                int size = packet.getLength();
                publishProgress(java.util.Arrays.copyOf(data, size));
                Log.d(TrainingActivity.TAG, "fin réception");
            }
        } catch (Exception e) {}
        return null;
    }

    /**
     * Sends a received byte array to another class for it to interpret it.
     * @param data byte array to send to other activities
     */
    protected void onProgressUpdate(byte[]... data) {
        byte[] b = data[0];
        (new TrainingActivity()).fetchReceive(b);
    }
}
