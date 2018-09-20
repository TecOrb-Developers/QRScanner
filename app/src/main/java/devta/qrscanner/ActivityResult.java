package devta.qrscanner;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.vision.barcode.Barcode;

public class ActivityResult extends AppCompatActivity {

    public static final String kResults = "list_of_barcode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        setTitle("Results");

        String[] listOfBarcode = getIntent().getStringArrayExtra(kResults);

        if(listOfBarcode == null || listOfBarcode.length == 0) return;

        ListView resultList = findViewById(R.id.list_barcode);

        resultList.setAdapter(new ArrayAdapter<>(this,
                R.layout.support_simple_spinner_dropdown_item, listOfBarcode));

    }
}
