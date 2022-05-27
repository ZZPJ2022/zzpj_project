package com.example.facecloud;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Menu extends AppCompatActivity {
    private String username = "";
    private EditText input;
    Button userDaria;
    Button userMateusz;
    Button userMichal;
    Button userJan;
    Button start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
        userDaria = findViewById(R.id.button1);
        userMateusz = findViewById(R.id.button2);
        userMichal = findViewById(R.id.button3);
        userJan = findViewById(R.id.button4);
        start = findViewById(R.id.button5);
        input = findViewById(R.id.editText1);
        setColor(userMateusz, userMichal, userJan, userDaria);
        setColor(start,true);



        userDaria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setColor(userMateusz, userMichal, userJan, userDaria);
                setColor(userDaria,false);
                input.setText("");
                username = "Daria";
                System.out.println(username);
            }
        });

        userMateusz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setColor(userMateusz, userMichal, userJan, userDaria);
                setColor(userMateusz, false);
                input.setText("");
                username = "Mateusz";
                System.out.println(username);

            }
        });

        userMichal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setColor(userMateusz, userMichal, userJan, userDaria);
                setColor(userMichal, false);
                input.setText("");
                username = "Michał";
                System.out.println(username);
            }
        });

        userJan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setColor(userMateusz, userMichal, userJan, userDaria);
                setColor(userJan, false);
                input.setText("");
                username = "Jan";
                System.out.println(username);
            }
        });


        input.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                username = s.toString();
                System.out.println(username);
            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(username);
                setColor(start,false);
                if (username.equals("")) {
                    Toast.makeText(Menu.this, "Please choose username", Toast.LENGTH_LONG).show();
                    setColor(start,true);
                } else {
                    Intent sendStuff = new Intent(Menu.this, MapsActivity.class);
                    sendStuff.putExtra("username", username);
                    startActivity(sendStuff);
                }
            }
        });
    }

    public static void setColor(Button button, boolean isDefault) {
        if (isDefault) {
            button.setBackgroundColor(android.graphics.Color.parseColor("#3700B3"));
            button.setTextColor(Color.parseColor("#FFFFFF"));
        } else {
            button.setBackgroundColor(android.graphics.Color.parseColor("#FFFFFF"));
            button.setTextColor(Color.parseColor("#3700B3"));
        }
    }

    public static void setColor(Button button1, Button button2, Button button3, Button button4) {
        button1.setBackgroundColor(android.graphics.Color.parseColor("#3700B3"));
        button1.setTextColor(Color.parseColor("#FFFFFF"));
        button2.setBackgroundColor(android.graphics.Color.parseColor("#3700B3"));
        button2.setTextColor(Color.parseColor("#FFFFFF"));
        button3.setBackgroundColor(android.graphics.Color.parseColor("#3700B3"));
        button3.setTextColor(Color.parseColor("#FFFFFF"));
        button4.setBackgroundColor(android.graphics.Color.parseColor("#3700B3"));
        button4.setTextColor(Color.parseColor("#FFFFFF"));
    }
}
