package com.example.david.fridge;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Main extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    boolean isScan;
    Button expdate,scan;
    EditText etName,etWeight;
    String pName,pWeight,barcode,enterD,enterT,expiredD,resName,resWeight;;
    DatabaseReference ref;
    ImageView img;
    TextView pick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isScan = false;

        etName = (EditText)findViewById(R.id.editText);
        etWeight = (EditText)findViewById(R.id.editText2);
        expdate = (Button)findViewById(R.id.expiration_date);
        scan = (Button)findViewById(R.id.scan);
        img = (ImageView)findViewById(R.id.imageView);
        pick = (TextView)findViewById(R.id.pick);

        expdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment datePicker= new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(),"date picker");

            }
        });

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId()==R.id.scan){
                    IntentIntegrator scanIntegrator = new IntentIntegrator(Main.this);
                    scanIntegrator.initiateScan();
                }
            }
        });

        ref = FirebaseDatabase.getInstance().getReference("products");

        img.setImageResource(R.drawable.no_image);
    }


    public void upload(View view) {
        pName = etName.getText().toString();
        pWeight = etWeight.getText().toString();
        Calendar date=Calendar.getInstance();
        Calendar time=Calendar.getInstance();
        enterD=DateFormat.getDateInstance(DateFormat.SHORT).format(date.getTime());
        SimpleDateFormat mdformat=new SimpleDateFormat("HH:mm:ss");
        enterT=mdformat.format(time.getTime());
        if(pName.equals(""))
            Toast.makeText(this,"You didn't enter a name or scanned anything",Toast.LENGTH_SHORT).show();
        else if(pWeight.equals(""))
            Toast.makeText(this,"You didn't enter a weight or scanned anything",Toast.LENGTH_SHORT).show();
        else{
            String id = ref.push().getKey();
            Product product = new Product(id,pName,enterD,enterT,expiredD,barcode,pWeight,isScan);
            ref.child(id).setValue(product);
            etName.setText("");
            pName = "";
            etWeight.setText("");
            pWeight = "";
            barcode = null;
            pick.setText("");
            isScan = false;
            img.setImageResource(R.drawable.no_image);
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar calendar=Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        expiredD= DateFormat.getDateInstance(DateFormat.SHORT).format(calendar.getTime());
        pick.setText("Date picked: "+expiredD);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanningResult.getContents() != null) {
            barcode = scanningResult.getContents();
            isScan = true;
            Ion.with(getApplicationContext())
                    .load("https://chp.co.il/%D7%91%D7%90%D7%A8%20%D7%A9%D7%91%D7%A2/0/0/"+barcode+"/0")
                    .asString()
                    .setCallback(new FutureCallback<String>() {
                        @Override
                        public void onCompleted(Exception e, String result) {
                            result=result.substring(72,result.indexOf("</title>",49));
                            try {
                                resName = result.substring(0,result.indexOf(","));
                                resWeight = result.substring(result.indexOf(", ")+2);
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }

                            try {
                                if(resName!=null&&resWeight!=null){
                                    etName.setText(""+resName);
                                    etWeight.setText(""+resWeight);
                                }
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                        }
                    });
            try {
                Picasso.get().load("https://m.pricez.co.il/ProductPictures/"+barcode+".jpg").into(img);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else{
            Toast toast = Toast.makeText(getApplicationContext(),
                    "No scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void delete(View view) {
        etName.setText("");
        etWeight.setText("");
        pick.setText("");
        img.setImageResource(R.drawable.no_image);
        isScan = false;
    }
}
