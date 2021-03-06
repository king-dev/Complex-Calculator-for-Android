/*	Copyright 2011 Alexander Bunkenburg alex@inspiracio.com
 * 
 * This file is part of Complex Calculator for Android.
 * 
 * Complex Calculator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Complex Calculator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Complex Calculator for Android. If not, see <http://www.gnu.org/licenses/>.
 * */
package cat.inspiracio.calculator;

import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.KEYCODE_ENTER;
import static android.view.KeyEvent.KEYCODE_EQUALS;

import java.text.ParseException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import cat.inspiracio.numbers.BugException;
import cat.inspiracio.numbers.EC;
import cat.inspiracio.numbers.PartialException;
import cat.inspiracio.parsing.SyntaxTree;
import cat.inspiracio.widget.IMEEditText;

/** The activity for calculation. */
public final class ComplexWorld extends Activity{

	//State -----------------------------------------------------------------------
	
	/** Button for resetting, that is re-centering the plane. */
//	private Button resetButton;
	
	/** Button for clearing, that is deleting all the shown numbers. */
//	private Button clearButton;
	
	/** The world where the numbers are displayed graphically. */
	private WorldRepresentation world;

	/** The text box where the expression is displayed. */
	private EditText display;
	
	//Constructors ----------------------------------------------------------------
	
	public ComplexWorld(){}
	
	//Activity methods ------------------------------------------------------------
	
    /** Called when the activity is first created. */
    @Override public void onCreate(Bundle bundle){
    	super.onCreate(bundle);
        this.setContentView(R.layout.main);
        
//        this.resetButton=(Button)this.findViewById(R.id.resetButton);
//        this.resetButton.setOnClickListener(new View.OnClickListener(){
//			@Override public void onClick(View v){world.reset();}
//		});
        
//        this.clearButton=(Button)this.findViewById(R.id.clearButton);
//        this.clearButton.setOnClickListener(new View.OnClickListener(){
//			@Override public void onClick(View v){world.clear();}
//		});
        
        this.world=(WorldRepresentation)this.findViewById(R.id.canvas);
        this.world.set(this);
        
        View v=this.findViewById(R.id.display);
        IMEEditText it=(IMEEditText)v;
        SoftKeyboard ims=new SoftKeyboard();
        //ims.setContext(this);
        it.setInputMethodService(ims);
        this.display=it;
        this.display.setOnKeyListener(new OnKeyListener(){
            @Override public boolean onKey(View v, int keyCode, KeyEvent event){
                // If the event is a key-down event on the "enter" button
            	int action=event.getAction();
                if(action==ACTION_DOWN){
                	//Gets the expression, calculates the result, and adds it.
                	if(keyCode==KEYCODE_ENTER || keyCode==KEYCODE_EQUALS){
                		doEquals();
                		return true;
                	}
                }
                return false;
            }
        });
        //hides the input method
        Window window=getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        //The text size of the display if 27.
        //float t=display.getPaint().getTextSize();//27
        //if(bundle!=null)this.world.onRestoreInstanceState(bundle);
    }

    /** Create the options menu for the first time. */
    @Override public final boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=this.getMenuInflater();
        inflater.inflate(R.menu.plane_menu, menu);
        return true;
    }
    
    /** Event handler for options menu. */
    @Override public final boolean onOptionsItemSelected(MenuItem item) {
        int itemId=item.getItemId();
        switch(itemId){
        case R.id.reset:
        	this.world.reset();
            return true;
        case R.id.clear:
            this.world.clear();
            return true;
        case R.id.about:
            this.showDialog(DIALOG_ABOUT);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    private static final int DIALOG_ABOUT=0;
    
    @Override protected Dialog onCreateDialog(int id) {
        switch(id) {
        case DIALOG_ABOUT:
        	Context context=this;
        	final TextView message=new TextView(context);
        	final SpannableString s=new SpannableString(context.getText(R.string.dialog_message));
        	Linkify.addLinks(s, Linkify.ALL);
        	message.setText(s);
        	message.setMovementMethod(LinkMovementMethod.getInstance());
        	message.setTextColor(Color.BLACK);

        	AlertDialog.Builder builder=new AlertDialog.Builder(this);
        	builder.setIcon(R.drawable.icon);//Shown before title
        	builder.setTitle(R.string.dialog_title);
        	builder.setView(message);
        	builder.setCancelable(true);
        	builder.setInverseBackgroundForced(true);
        	builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener(){
        		@Override public void onClick(DialogInterface dialog, int id){dialog.dismiss();}
        	});
        	Dialog dialog=builder.create();
        	return dialog;
        }
        return null;
    }
    /** If the user presses BACK:
     * If keyboard is visible, close it. IMEEditText handles that.
     * If keyboard is not visible, close CC.
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override public void onBackPressed(){this.finish();}

	/** Writes the state to bundle. */
	@Override protected final void onSaveInstanceState(Bundle bundle){
		super.onSaveInstanceState(bundle);
		this.world.parcel("inspiracio.calculator.world", bundle);
	}

	@Override protected final void onRestoreInstanceState(Bundle bundle){
		super.onRestoreInstanceState(bundle);
		this.world.unparcel("inspiracio.calculator.world", bundle);
	}

    //Methods ----------------------------------------------------------------
    
	/** Adds a complex number to the display. */
    final void add(EC ec){
    	int start=display.getSelectionStart();
    	int end=display.getSelectionEnd();
    	Editable editable=display.getEditableText();
    	editable.replace(start, end, "(" + ec + ")");
    }

    //Helpers ----------------------------------------------------------------
    
    /** Gets the expression from the display, parses it, evaluates it,
     * and append the result to the display. */
	private void doEquals(){
        Editable editable=display.getText();
        String s=editable.toString();
        display.append(" = ");
        String msg=null;
        try{
            SyntaxTree tree=SyntaxTree.parse(s);
            EC ec=tree.evaluate(null);
            display.append(ec.toString());
            if(this.world!=null)
            	world.add(ec);
            return;
        }catch(BugException be){
        	be.printStackTrace();
        	msg=be.getLocalizedMessage();
        }catch(PartialException pe){
        	pe.printStackTrace();
        	msg=pe.getLocalizedMessage();
        	msg="Undefined: " + msg;
        }catch(ParseException pse){
        	pse.printStackTrace();
        	msg=pse.getLocalizedMessage();
        }
        if(msg!=null){
        	Context context=this.getApplicationContext();
        	Toast toast=Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        	toast.show();
        }
    }

	/** Erases the old displayed result, if there is one.
	 * Erases everything from "=" onwards.
	 * <p>
	 * Every key press should first call this. */
    @SuppressWarnings("unused")
	private void eraseOldResult(){
        String s = display.getText().toString();
        int i = s.lastIndexOf('=');
        if(i != -1){
            int start = display.getSelectionStart();//getCaretPosition();
            int end=display.getSelectionEnd();
            s = s.substring(0, i);
            int k = s.length();
            display.setText(s);
            start = start >= k ? k : start;
            end = end >= k ? k : end;
            //display.setCaretPosition(start);
            display.setSelection(start, end);
        }
    }

}