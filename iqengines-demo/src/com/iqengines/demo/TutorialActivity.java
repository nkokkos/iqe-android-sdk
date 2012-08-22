package com.iqengines.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;

public class TutorialActivity extends Activity {
	
    private ImageButton goBack;
    private ImageButton historyButton;
    private ImageButton tutoButton;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.tutorial);
        initUi();
    }
   

    private void initUi() {
    	
        goBack = (ImageButton) findViewById(R.id.goBack);
        historyButton = (ImageButton) findViewById(R.id.historyButton);
        tutoButton = (ImageButton) findViewById(R.id.tutoButton);
        
        goBack.setOnClickListener(new View.OnClickListener() {
        	
            @Override
            public void onClick(View v) {
            	Intent intent = new Intent(TutorialActivity.this,
						DemoActivity.class);
				startActivity(intent);
				finish();
            }
            
            
        });
        
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	Intent intent = new Intent(TutorialActivity.this,
						HistoryActivity.class);
				startActivity(intent);
				finish();
            }
            
            
        });
        
        tutoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
            }
            
            
        });
        
    }
   

}
