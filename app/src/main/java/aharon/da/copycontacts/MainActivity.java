package aharon.da.copycontacts;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import aharon.da.copycontacts.R;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<Contact> contactList = new ArrayList<>();
    RecyclerView recyclerView;
    ImageView searchIcon;
    EditText searchEditText;
    TextView appTitle;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         recyclerView = findViewById(R.id.rv);
       // appTitle = findViewById(R.id.appTitle);
        searchIcon = findViewById(R.id.searchIcon);
        searchEditText = findViewById(R.id.searchEditText);
        searchEditText.requestFocus();
        searchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchIcon.setVisibility(View.GONE);
                searchEditText.setVisibility(View.VISIBLE);
            }
        });
        // תחבר את המאזין לשדה החיפוש
        searchEditText.addTextChangedListener(new TextWatcher() {
            ContactsDatabaseManager dataBaseHelper=new ContactsDatabaseManager(MainActivity.this);
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (dataBaseHelper.searchContact(s.toString())!=null){
                    List<Contact>getData=dataBaseHelper.searchContact(s.toString());
                    ContactAdapter adapter = new ContactAdapter(getData,MainActivity.this);
                    recyclerView.setAdapter(adapter);}

            }
        });


        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ContactsDatabaseManager databaseManager = new ContactsDatabaseManager(MainActivity.this);
        if (databaseManager.getAllContacts()!=null){
            List<Contact>getData=databaseManager.getAllContacts();
            if (getData.size()==0){
                readContacts();
            }
            else {
                for (int i = 0; i < getData.size(); i++) {
                Log.i("log",getData.get(i).toString());
                }
            ContactAdapter adapter = new ContactAdapter(getData,this);
            recyclerView.setAdapter(adapter);
            }
        }

    }
    @SuppressLint("Range")
    private void readContacts() {
        ContactsDatabaseManager databaseManager = new ContactsDatabaseManager(MainActivity.this);
        Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                 String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                 String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor phoneCursor = getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id},
                            null);
                    while (phoneCursor.moveToNext()) {
                        String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        System.out.println("Name: " + name + ", Phone Number: " + phoneNumber);

                        contactList.add(new Contact(name, phoneNumber));
                        databaseManager.addData(name,phoneNumber);
                        // התאמת נתוני הרשימה ל-RecyclerView באמצעות מתאם
                    }

                    phoneCursor.close();
                }

            }
            cursor.close();
            ContactAdapter adapter = new ContactAdapter(contactList,this);
            recyclerView.setAdapter(adapter);
        }}
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
            // קוד פעולה שתתבצע כאשר לוחץ על מקש מספר
            int pressedNumber = keyCode - KeyEvent.KEYCODE_0;
            searchIcon.setVisibility(View.GONE);
            searchEditText.setVisibility(View.VISIBLE);
            searchEditText.requestFocus(); // העברת הפוקוס ל-EditText
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if (id==R.id.refresh){
            Toast.makeText(this, "Plese with ...", Toast.LENGTH_SHORT).show();
            ContactsDatabaseManager dbManager = new ContactsDatabaseManager(this);
            dbManager.deleteAllContacts();
            readContacts();
        }
        else{
                AlertDialog.Builder about = new AlertDialog.Builder(MainActivity.this);
                about.setTitle("About");
                about.setMessage("Developed by daappisrael@gmail.com || @ 2024");
                about.setPositiveButton("close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                AlertDialog dialog = about.create();
                dialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

}