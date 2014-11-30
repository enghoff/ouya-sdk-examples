package tv.ouya.examples.keyboard;

import java.util.ArrayList;
import java.util.HashMap;

import tv.ouya.console.api.OuyaActivity;
import tv.ouya.console.api.OuyaController;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends OuyaActivity {
	
	private static final String TAG = MainActivity.class.getSimpleName();
	
	private ArrayList<TextView> mKeys = new ArrayList<TextView>();
	
	private HashMap<String, TextView> mKeyMap = new HashMap<String, TextView>();
	
	private float DEADZONE = 0.25f;
	
	private final String COLOR_NORMAL = "#ffffffff";
	private final String COLOR_SKIPPED = "#ff666666";
	private final String COLOR_INACTIVE = "#ff222222";
	private final String COLOR_LEFT_A = "#ff440000";
	private final String COLOR_LEFT_B = "#ff004400";
	private final String COLOR_RIGHT_A = "#ff000044";
	private final String COLOR_RIGHT_B = "#ff444400";
	
	private final String[] SET_KEYS_1 = new String[]{"Q","W","E","A","S","Z","X"};	
	private final String[] SET_KEYS_2 = new String[]{"R","T","D","F","G","C"};	
	private final String[] SET_KEYS_3 = new String[]{"Y","U","I","H","J","V","B"};	
	private final String[] SET_KEYS_4 = new String[]{"O","P","K","L","N","M"};
	
	private final String[] SUBSET_KEYS_1 = new String[]{"Q","W","E", "A"};
	private final String[] SUBSET_KEYS_2 = new String[]{"S","Z","X"};
	private final String[] SUBSET_KEYS_3 = new String[]{"R","T","D"};
	private final String[] SUBSET_KEYS_4 = new String[]{"F","G","C"};
	private final String[] SUBSET_KEYS_5 = new String[]{"Y","U","I","J"};
	private final String[] SUBSET_KEYS_6 = new String[]{"H","V","B"};
	private final String[] SUBSET_KEYS_7 = new String[]{"O","P","L"};
	private final String[] SUBSET_KEYS_8 = new String[]{"K","N","M"};
	
	private boolean mButtonPressed = false;
	private int mSelectedGroup = 0;
	
	private TextView mSelectedLetter = null;
	
	private TextView mOverlayO = null;
	private TextView mOverlayU = null;
	private TextView mOverlayY = null;
	private TextView mOverlayA = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		FrameLayout content = (FrameLayout)findViewById(android.R.id.content);
		if (null != content) {
			
			LinearLayout rows = new LinearLayout(this);
			rows.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			rows.setOrientation(LinearLayout.VERTICAL);
			rows.setGravity(Gravity.CENTER);
			
			TextView hint = new TextView(this);
			hint.setGravity(Gravity.CENTER);
			hint.setText("Hold the BUTTON_O with AXIS_LS or AXIS_RS to select a letter.");
			rows.addView(hint);
			
			mSelectedLetter = new TextView(this);
			mSelectedLetter.setGravity(Gravity.CENTER);
			mSelectedLetter.setText("Selected Letter: ???");
			rows.addView(mSelectedLetter);
			
			LinearLayout row = new LinearLayout(this);
			row.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			row.setOrientation(LinearLayout.HORIZONTAL);
			addKeys(row, new String[]{"Q","W","E","R","T","Y","U","I","O","P"});
			rows.addView(row);
			
			row = new LinearLayout(this);
			row.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			row.setOrientation(LinearLayout.HORIZONTAL);
			addKeys(row, new String[]{"A","S","D","F","G","H","J","K","L"});
			rows.addView(row);			
			
			row = new LinearLayout(this);
			row.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			row.setOrientation(LinearLayout.HORIZONTAL);
			addKeys(row, new String[]{"Z","X","C","V","B","N","M"});
			rows.addView(row);
			
			content.addView(rows);
			
			mOverlayO = new TextView(this);
			mOverlayO.setText("[O]");
			content.addView(mOverlayO);
			
			mOverlayU = new TextView(this);
			mOverlayU.setText("[U]");
			content.addView(mOverlayU);
			
			mOverlayY = new TextView(this);
			mOverlayY.setText("[Y]");
			content.addView(mOverlayY);
			
			mOverlayA = new TextView(this);
			mOverlayA.setText("[A]");
			content.addView(mOverlayA);
			
			hideOverlay();
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		setDefaultBackgroundColor();
		setDefaultColor();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
		if (keyCode == OuyaController.BUTTON_L1) { 
			for (TextView t : mKeys) {
				String text = t.getText().toString();
				t.setText(text.toUpperCase());
			}
		} else if (keyCode == OuyaController.BUTTON_R1) { 
			for (TextView t : mKeys) {
				String text = t.getText().toString();
				t.setText(text.toLowerCase());
			}
		} else {
			if (keyEvent.getRepeatCount() == 0) {			
				if (mSelectedGroup > 4) {
					selectLetter(keyCode);
				} else {
					if (keyCode == OuyaController.BUTTON_O) {		
						mButtonPressed = true;
						setSelectedGroup(0);
					}
					showSelectedGroup();
				}
			}
		}
		return true;
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent keyEvent) {
		if (keyCode == OuyaController.BUTTON_O) {
			mButtonPressed = false;
			showSelectedGroup();
		}
		return true;
	}
	
	@Override
	public boolean onGenericMotionEvent(MotionEvent motionEvent) {
		
		float lx = motionEvent.getAxisValue(OuyaController.AXIS_LS_X);
		float ly = motionEvent.getAxisValue(OuyaController.AXIS_LS_Y);
		float rx = motionEvent.getAxisValue(OuyaController.AXIS_RS_X);
		float ry = motionEvent.getAxisValue(OuyaController.AXIS_RS_Y);
		
		if (mButtonPressed) {		
			if (Math.abs(lx) > DEADZONE &&
				Math.abs(lx) > DEADZONE) {
				if (lx < 0 &&
					ly < 0) {
					setSelectedGroup(5);
				} else if (lx < 0 &&
					ly > 0) {
					setSelectedGroup(6);
				} else if (lx > 0 &&
					ly < 0) {
					setSelectedGroup(7);
				} else if (lx > 0 &&
					ly > 0) {
					setSelectedGroup(8);
				}
			} else if (Math.abs(rx) > DEADZONE &&
				Math.abs(rx) > DEADZONE) {
				if (rx < 0 &&
					ry < 0) {
					setSelectedGroup(9);
				} else if (rx < 0 &&
					ry > 0) {
					setSelectedGroup(10);
				} else if (rx > 0 &&
					ry < 0) {
					setSelectedGroup(11);
				} else if (rx > 0 &&
					ry > 0) {
					setSelectedGroup(12);
				}
			} else {
				setSelectedGroup(0);
				hideOverlay();
			}
		}
		
		return true;
	}
	
	private String[] getLetters() {
		switch (mSelectedGroup) {
		case 5:
			return new String[]{"Q","W","A","E"};
		case 6:
			return new String[]{"S","Z","X",""};
		case 7:
			return new String[]{"D","R","T", ""};
		case 8:
			return new String[]{"F","C","G",""};
		case 9:
			return new String[]{"Y","U","I","J"};
		case 10:
			return new String[]{"H","V","B",""};
		case 11:
			return new String[]{"O","P","L",""};
		case 12:
			return new String[]{"K","N","M",""};
		}
		return null;
	}
	
	// Use the O U Y A buttons to select the letter
	private void selectLetter(int keyCode) {
		
		String[] letters = getLetters();		
		if (null == letters) {
			return;
		}
		
		switch (keyCode) {
		case OuyaController.BUTTON_O:
			if (!letters[2].equals("")) {
				mSelectedLetter.setText("Selected: "+letters[2]);				
				setSelectedGroup(0);
				hideOverlay();
			}
			break;
		case OuyaController.BUTTON_U:
			if (!letters[0].equals("")) {
				mSelectedLetter.setText("Selected: "+letters[0]);
				setSelectedGroup(0);
				hideOverlay();
			}
			break;
		case OuyaController.BUTTON_Y:
			if (!letters[1].equals("")) {
				mSelectedLetter.setText("Selected: "+letters[1]);
				setSelectedGroup(0);
				hideOverlay();
			}
			break;
		case OuyaController.BUTTON_A:
			if (!letters[3].equals("")) {
				mSelectedLetter.setText("Selected: "+letters[3]);
				setSelectedGroup(0);
				hideOverlay();
			}
			break;
		}
	}
	
	private void addKeys(LinearLayout layout, String[] keys) {
		for (String key : keys) {
			LinearLayout border = new LinearLayout(this);
			border.setPadding(10, 10, 10, 10);
			TextView t = new TextView(this);
			t.setText(key);
			t.setTextSize(36);
			t.setTypeface(null, Typeface.BOLD);
			t.setWidth(125);
			t.setHeight(125);
			t.setGravity(Gravity.CENTER);
			border.addView(t);
			layout.addView(border);
			mKeys.add(t);
			mKeyMap.put(key.toUpperCase(), t);
		}
	}
	
	private void setBackgroundColors(String colorString, String[] keys) {
		int c = Color.parseColor(colorString);
		for (String key : keys) {
			TextView t = mKeyMap.get(key);
			if (null != t) {
				t.setBackgroundColor(c);
			}
		}
	}
	
	private void setColors(String colorString, String[] keys) {
		int c = Color.parseColor(colorString);
		for (String key : keys) {
			TextView t = mKeyMap.get(key);
			if (null != t) {
				t.setTextColor(c);
			}
		}
	}
	
	private void setDefaultBackgroundColor() {
		setBackgroundColors(COLOR_LEFT_A, SET_KEYS_1);
		setBackgroundColors(COLOR_LEFT_B, SET_KEYS_2);
		setBackgroundColors(COLOR_RIGHT_A, SET_KEYS_3);
		setBackgroundColors(COLOR_RIGHT_B, SET_KEYS_4);
	}
	
	private void setDefaultColor() {
		setColors(COLOR_NORMAL, SET_KEYS_1);
		setColors(COLOR_NORMAL, SET_KEYS_2);
		setColors(COLOR_NORMAL, SET_KEYS_3);
		setColors(COLOR_NORMAL, SET_KEYS_4);
	}
	
	private void setInactiveColor() {
		setColors(COLOR_INACTIVE, SET_KEYS_1);
		setColors(COLOR_INACTIVE, SET_KEYS_2);
		setColors(COLOR_INACTIVE, SET_KEYS_3);
		setColors(COLOR_INACTIVE, SET_KEYS_4);
	}
	
	private void showSelectedGroup() {
		if (mSelectedGroup != 0) {
			setInactiveColor();			
		}
		switch (mSelectedGroup) {
		case 0:
			setDefaultBackgroundColor();
			if (mButtonPressed) {
				setInactiveColor();
			} else {
				setDefaultColor();
			}
			break;
		case 5:
			setColors(COLOR_NORMAL, SUBSET_KEYS_1);
			setColors(COLOR_SKIPPED, SUBSET_KEYS_2);
			setColors(COLOR_SKIPPED, SUBSET_KEYS_3);
			setColors(COLOR_SKIPPED, SUBSET_KEYS_4);
			break;
		case 6:
			setColors(COLOR_SKIPPED, SUBSET_KEYS_1);
			setColors(COLOR_NORMAL, SUBSET_KEYS_2);
			setColors(COLOR_SKIPPED, SUBSET_KEYS_3);
			setColors(COLOR_SKIPPED, SUBSET_KEYS_4);
			break;
		case 7:
			setColors(COLOR_SKIPPED, SUBSET_KEYS_1);
			setColors(COLOR_SKIPPED, SUBSET_KEYS_2);
			setColors(COLOR_NORMAL, SUBSET_KEYS_3);
			setColors(COLOR_SKIPPED, SUBSET_KEYS_4);
			break;
		case 8:
			setColors(COLOR_SKIPPED, SUBSET_KEYS_1);
			setColors(COLOR_SKIPPED, SUBSET_KEYS_2);
			setColors(COLOR_SKIPPED, SUBSET_KEYS_3);
			setColors(COLOR_NORMAL, SUBSET_KEYS_4);
			break;
		case 9:
			setColors(COLOR_NORMAL, SUBSET_KEYS_5);
			setColors(COLOR_SKIPPED, SUBSET_KEYS_6);
			setColors(COLOR_SKIPPED, SUBSET_KEYS_7);
			setColors(COLOR_SKIPPED, SUBSET_KEYS_8);
			break;
		case 10:
			setColors(COLOR_SKIPPED, SUBSET_KEYS_5);
			setColors(COLOR_NORMAL, SUBSET_KEYS_6);
			setColors(COLOR_SKIPPED, SUBSET_KEYS_7);
			setColors(COLOR_SKIPPED, SUBSET_KEYS_8);
			break;
		case 11:
			setColors(COLOR_SKIPPED, SUBSET_KEYS_5);
			setColors(COLOR_SKIPPED, SUBSET_KEYS_6);
			setColors(COLOR_NORMAL, SUBSET_KEYS_7);
			setColors(COLOR_SKIPPED, SUBSET_KEYS_8);
			break;
		case 12:
			setColors(COLOR_SKIPPED, SUBSET_KEYS_5);
			setColors(COLOR_SKIPPED, SUBSET_KEYS_6);
			setColors(COLOR_SKIPPED, SUBSET_KEYS_7);
			setColors(COLOR_NORMAL, SUBSET_KEYS_8);
			break;
		}
		
		String[] letters = getLetters();
		if (null != letters) {
			moveToLetter(letters[2], mOverlayO);
			moveToLetter(letters[0], mOverlayU);
			moveToLetter(letters[1], mOverlayY);
			moveToLetter(letters[3], mOverlayA);
		}
	}
	
	private void moveToLetter(final String letter, TextView overlay) {
		if (letter.equals("")) {
			overlay.setVisibility(View.INVISIBLE);
			return;
		}
		
		overlay.setVisibility(View.VISIBLE);
		
		TextView t = mKeyMap.get(letter);
		int[] location = new int[2];
		t.getLocationOnScreen(location);
		overlay.setX(location[0]);
		overlay.setY(location[1]);		
	}
	
	private void setSelectedGroup(int group) {
		//Log.i(TAG, "Set Group: " + group);
		mSelectedGroup = group;
		showSelectedGroup();
	}
	
	private void hideOverlay() {
		mOverlayO.setVisibility(View.INVISIBLE);
		mOverlayU.setVisibility(View.INVISIBLE);
		mOverlayY.setVisibility(View.INVISIBLE);
		mOverlayA.setVisibility(View.INVISIBLE);
	}
}