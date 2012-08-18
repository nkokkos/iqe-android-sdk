package com.iqengines.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class TutorialActivity extends Activity {
	
    private Button closeButton;
    private ImageButton nextButton;
    private ImageButton previousButton;
    private TextView titleTutorial;
    private ImageView scrollView;
    private int page = 1;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.tutorial);
        initUi();
    }
   

    private void initUi() {
    	
    	final TutorialActivity activity = this;
        closeButton = (Button) findViewById(R.id.closeButton);
        nextButton = (ImageButton) findViewById(R.id.nextButton);
        previousButton = (ImageButton) findViewById(R.id.previousButton);
        titleTutorial = (TextView) findViewById(R.id.tv_pagetitle);
        scrollView = (ImageView) findViewById(R.id.layout_scrollView);
        
        closeButton.setOnClickListener(new View.OnClickListener() {
        	
            @Override
            public void onClick(View v) {
            	Intent intent = new Intent(TutorialActivity.this,
						DemoActivity.class);
				startActivity(intent);
				finish();
            }
            
            
        });
        
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                page++;
                updatePage(activity);
            }
            
            
        });
        
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                page--;
                updatePage(activity);
            }
            
            
        });
        
    }
    
    static public void updatePage(TutorialActivity activity){
    	
    	switch(activity.page){
    	
    	case 1 : activity.scrollView.setImageResource(R.drawable.tutorial1);
    	         activity.titleTutorial.setText(R.string.title_tutorial_1);
    	         activity.previousButton.setVisibility(4);
    	break;
     	case 2 : activity.scrollView.setImageResource(R.drawable.tutorial2);
     			 activity.titleTutorial.setText(R.string.title_tutorial_2);
     			 activity.previousButton.setVisibility(0);
     			 activity.nextButton.setVisibility(0);
    	break;
    	case 3 : activity.scrollView.setImageResource(R.drawable.tutorial3);
    		     activity.titleTutorial.setText(R.string.title_tutorial_3);
    		     activity.nextButton.setVisibility(4);
    	break;
    	default : activity.finish();
    	break;
    	}
    	return;
    }

}
