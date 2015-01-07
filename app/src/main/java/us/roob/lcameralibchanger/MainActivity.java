package us.roob.lcameralibchanger;

import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import rootcheck.Root;


public class MainActivity extends ActionBarActivity {

    Button btn60Fps;
    Button btn120Fps;
    TextView main;
    private final String stockSha = "37ddbd2ebd1460a80252f2d33bb43cc6142d3842d10646d2acdc8d671c83cb0f";
    private final String mod60FpsSha = "1a17cdc851ef0daef9fdcea3f83f9c19b7c2d31802521b1a895096d4b938506c";
    private final String mod120FpsSha = "3b6eefa2e878f7cb583ab07a3a256dc973f83558a20f5df8f63b057a0d7d97c7";
    private final String libLocation = "/system/lib/libmmcamera_imx179.so";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        main = (TextView) findViewById(R.id.mainTextView);
        btn60Fps = (Button) findViewById(R.id.btn60Fps);
        btn120Fps = (Button) findViewById(R.id.btn120FPS);
        //hide buttons and only show them if the deivce is a nexus 5 and rooted.
        btn60Fps.setVisibility(View.GONE);
        btn120Fps.setVisibility(View.GONE);

        File filesDir = getFilesDir();
        //Scanner input = new Scanner(new File(filesDir, filename));

        Root r = new Root();
        // Check to make sure the device is a Nexus 5.
        if (getDeviceName().equalsIgnoreCase("LGE NEXUS 5")) {
            //The app is useless without root.
            if (!r.isDeviceRooted()) {
                Toast.makeText(this, "Device is not rooted. This tool is useless to you.", Toast.LENGTH_SHORT).show();
            } else {
                btn60Fps.setVisibility(View.VISIBLE);
                btn120Fps.setVisibility(View.VISIBLE);
                //main.setText("sha: " + SHA256sum.getLinuxSum());

            }
        } else {
            main.setTextColor(Color.RED);
            main.setText("This app is only for the LGE Nexus 5 :'(");
        }


    }//end onCreate

    public void btn60Fps(View v) {
        String tmpFile = unpackLibFromAssets("libmmcamera_imx179_lrx21o_60hz.so");
        installLibToSystem(tmpFile);
        Toast.makeText(this, "Clicked on Button", Toast.LENGTH_LONG).show();
    }

    public void btn120Fps(View v) {
        String tmpFile = unpackLibFromAssets("libmmcamera_imx179_lrx21o_120hz.so");
        installLibToSystem(tmpFile);
        Toast.makeText(this, "Clicked on Button", Toast.LENGTH_LONG).show();
    }

    public void btnStock(View v) {
        String tmpFile = unpackLibFromAssets("libmmcamera_imx179.so");
        installLibToSystem(tmpFile);

        Toast.makeText(this, "Clicked on Button", Toast.LENGTH_LONG).show();
        main.setText("Sum: " + getCurrentLibSHA());
        //Toast.makeText(this, "Lib exists", Toast.LENGTH_LONG).show();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }


    private String unpackLibFromAssets(String asset) {
        //File f = new File(getCacheDir() + "/" + asset);
        Log.d("Copy", "Asset: " + getCacheDir() + "/" + asset);


        File cacheFile = new File(getCacheDir(), asset);
        try {
            InputStream inputStream = getAssets().open(asset);
            try {
                FileOutputStream outputStream = new FileOutputStream(cacheFile);
                try {
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = inputStream.read(buf)) > 0) {
                        outputStream.write(buf, 0, len);
                    }
                } finally {
                    outputStream.close();
                }
            } finally {
                inputStream.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Log.d("COPY", "Path: " + cacheFile.getPath());
        return cacheFile.getPath();

















//        try {
//
//            InputStream is = getAssets().open(asset);
//            int size = is.available();
//            byte[] buffer = new byte[size];
//            is.read(buffer);
//            is.close();
//
//
//            FileOutputStream fos = new FileOutputStream(f);
//            fos.write(buffer);
//            fos.close();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//
//        Log.d("Copy", "tmpfile Location: " + f.getPath());
//        return f.getPath();
    }

    private void installLibToSystem(String tempFile) {
        ///system/lib/libmmcamera_imx179.so
        File lib = new File("/system/lib/libmmcamera_imx179.so");
        String sum;
        try {
            // Executes the command.
            Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", "mount -o remount,rw /system&&" +
                    "install -m644 " + tempFile + " " + libLocation + "&&"+
                    "mount -o remount,ro /system&&" +
                    "killall mm-qcamera-daemon mediaserver&&" +
                    "killall pkmx.lcamera"});

            // Reads stdout.
            // NOTE: You can write to stdin of the command using
            //       process.getOutputStream().
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            int read;
            char[] buffer = new char[4096];
            StringBuffer output = new StringBuffer();
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            reader.close();

            // Waits for the command to finish.
            process.waitFor();

            sum = output.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    private void mountSystemRW(){

    }

    private String getCurrentLibSHA() {
        ///system/lib/libmmcamera_imx179.so
        File lib = new File("/system/lib/libmmcamera_imx179.so");
        String sum;
        try {
            // Executes the command.
            Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", "sha256sum " + lib});

            // Reads stdout.
            // NOTE: You can write to stdin of the command using
            //       process.getOutputStream().
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            int read;
            char[] buffer = new char[4096];
            StringBuffer output = new StringBuffer();
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            reader.close();

            // Waits for the command to finish.
            process.waitFor();

            sum = output.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


        return sum.split(" ")[0];
    }


    public String runRootCMD(String[] cmd) {
        try {
            Process process = Runtime.getRuntime().exec("su");
            process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return "";
    }


}
