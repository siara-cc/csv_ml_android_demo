/*
 * Copyright (C) 2015 arun@siara.cc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author Arundale R.
 *
 */
package cc.siara.csv_ml_demo;

import java.io.IOException;
import java.io.StringReader;

import org.json.simple.JSONObject;
import org.json.simple.JSONWriter;
import org.w3c.dom.Document;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import cc.siara.csv_ml.MultiLevelCSVParser;
import cc.siara.csv_ml.Util;

public class MainActivity extends ActionBarActivity implements OnItemSelectedListener, OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnXML = (Button) findViewById(R.id.btnXML);
        btnXML.setOnClickListener(this);
        Button btnJSON = (Button) findViewById(R.id.btnJSON);
        btnJSON.setOnClickListener(this);
        Spinner spinner = (Spinner) findViewById(R.id.spExamples);
        spinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.input_examples, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		Spinner spinner = (Spinner) findViewById(R.id.spExamples);
		if (spinner.equals(arg0)) {
           EditText etInput = (EditText) findViewById(R.id.etInput);
           etInput.setText(aExampleCSV[spinner.getSelectedItemPosition()]);
           Button btnXML = (Button) findViewById(R.id.btnXML);
           btnXML.requestFocus();
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}

	void toXML() {
        EditText etInput = (EditText) findViewById(R.id.etInput);
		CheckBox cbPretty = (CheckBox) findViewById(R.id.cbPretty);
		MultiLevelCSVParser parser = new MultiLevelCSVParser();
		try {
			Document doc = parser.parseToDOM(new StringReader(etInput.getText().toString()), false);
			String ex_str = parser.getEx().get_all_exceptions();
			if (ex_str.length() > 0) {
				Toast.makeText(getApplicationContext(), ex_str, Toast.LENGTH_SHORT).show();
				if (parser.getEx().getErrorCode() > 0) return;
			}
			String xmlString = Util.docToString(doc, cbPretty.isChecked());
	        EditText etOutput = (EditText) findViewById(R.id.etOutput);
	        etOutput.setText(xmlString);
			//tfOutputSize.setText(String.valueOf(xmlString.length()));
		} catch (IOException e) {
			Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
		}
    }

    void toJSON() {
        EditText etInput = (EditText) findViewById(R.id.etInput);
		CheckBox cbPretty = (CheckBox) findViewById(R.id.cbPretty);
		MultiLevelCSVParser parser = new MultiLevelCSVParser();
		try {
			JSONObject jo = parser.parseToJSO(new StringReader(etInput.getText().toString()), false);
			String ex_str = parser.getEx().get_all_exceptions();
			if (ex_str.length() > 0) {
				Toast.makeText(getApplicationContext(), ex_str, Toast.LENGTH_LONG).show();
				if (parser.getEx().getErrorCode() > 0) return;
			}
			String outStr = null;
			if (cbPretty.isChecked()) {
				JSONWriter jw = new JSONWriter();
				try {
					jo.writeJSONString(jw);
				} catch (IOException e) {
					e.printStackTrace();
				}
				outStr = jw.toString();
			} else
				outStr = jo.toJSONString();
	        EditText etOutput = (EditText) findViewById(R.id.etOutput);
	        etOutput.setText(outStr);
			//tfOutputSize.setText(String.valueOf(outStr.length()));
		} catch (IOException e) {
			Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
		}
    }

	String[] aExampleCSV = new String[] { "abc,physics,53\nabc,chemistry,65\nxyz,physics,73\nxyz,chemistry,76",
			"csv_ml,1.0,UTF-8,root,no_node_name,inline\nname,subject,marks\nabc,physics,53\nabc,chemistry,65\nxyz,physics,73\nxyz,chemistry,76",
			"csv_ml,1.0,UTF-8,root,with_node_name,inline\nstudent,name,subject,marks\nend_schema\nstudent,abc,physics,53\nstudent,abc,chemistry,65\nstudent,xyz,physics,73\nstudent,xyz,chemistry,76",
			"csv_ml,1.0\nstudent,name,subject,marks\n1,abc,physics,53\n1,abc,chemistry,65\n1,xyz,physics,73\n1,xyz,chemistry,76",
			"csv_ml,1.0\nstudent,name,subject,marks\nfaculty,name,subject\n1,abc,physics,53\n1,abc,chemistry,65\n1,xyz,physics,73\n1,xyz,chemistry,76\n2,pqr,physics\n2,bcd,chemistry",
			"csv_ml,1.0\nstudent,name,age\n education,course_name,year_passed\n  subject,name,marks\n1,abc,24\n 1,bs,2010\n  1,physics,53\n  1,chemistry,65\n 1,ms,2012\n  1,physics,74\n  1,chemistry,75\n1,xyz,24\n 1,bs,2010\n  1,physics,67\n  1,chemistry,85",
			"csv_ml,1.0\nstudent,name,age\n education,course_name,year_passed\n  subject,name,marks\n references,name,company,designation\n1,abc,24\n 1,bs,2010\n  1,physics,53\n  1,chemistry,65\n 1,ms,2012\n  1,physics,74\n  1,chemistry,75\n 2,pqr,bbb,executive\n 2,mno,bbb,director\n1,xyz,24\n 1,bs,2010\n  1,physics,67\n  1,chemistry,85",
			"csv_ml,1.0\nstudent,name,age\n1,a\n1,b,23,His record is remarkable\n1,c,24,His record is remarkable,His performance is exemplary",
			"csv_ml,1.0\nstudent\n name\n age\n1\n 1,a\n 2,23",
			"csv_ml,1.0\nsample,text\n1,No quote\n1, No quote with preceding space\n1,With quote (\")\n1,\"With quotes, and \"\"comma\"\"\"\n1, \"With quotes, (space ignored)\"\n1, \"\"\"Enclosed, with double quote\"\"\"\n1, \"\"\"Single, preceding double quote\"\n1, \"Double quote, suffix\"\"\"\n1, \"Double quote, (\"\") in the middle\"\n1, \"More\n\nthan\n\none\n\nline\"",
			"/* You can have comments anywhere,\n   even at the beginning\n*/\ncsv_ml,1.0\n\n/* And empty lines like this */\n\nsample,text1,text2\n1,/* This is a comment */ \"hello\", \"world\" /* End of line comment */\n1,/* This is also a comment */, \"/* But this isn't */\"\n\n1,\"third\", \"line\" /* Multiline\ncomment */\n/* Comment at beginning of line */1, \"fourth\" , \"line\"",
			"csv_ml,1.0,UTF-8,data\nstudent,name,age\n1,a,24",
			"csv_ml,1.0,UTF-8,student\nstudent,name,age\n1,a,24",
			"csv_ml,1.0,UTF-8,student\nstudent,name,age\n1,a,24\n1,b,35",
			"csv_ml,1.0,UTF-8,student\nstudent,name,age\nfaculty,name,age\n1,a,24\n2,b,45",
			"csv_ml,1.0\nour:student,his:name,age,xmlns:his,xmlns:our\n1,a,24,http://siara.cc/his,http://siara.cc/our\n1,b,26,http://siara.cc/his,http://siara.cc/our",
			"csv_ml,1.0,UTF-8,root/our='http://siara.cc/our' his='http://siara.cc/his'\nour:student,his:name,age\n1,a,24\n1,b,26",
			"csv_ml,1.0,UTF-8,xsl:stylesheet/xsl='http://www.w3.org/1999/XSL/Transform'\nxsl:stylesheet,version\n xsl:template,match\n  xsl:value-of,select\n1,1.0\n 1,//student\n  1,@name\n  1,@age",
			"csv_ml,1.0,UTF-8,xsl:stylesheet/xsl='http://www.w3.org/1999/XSL/Transform'\n01,xsl:value-of,select\n02,xsl:for-each,select\n 01\nxsl:stylesheet,version\n xsl:template,match\n  01,02\n1,1.0\n 1,//student\n  01,@name\n  01,@age\n  02,education\n   01,@course_name\n   01,@year_passed",
			"csv_ml,1.0\nstudent,name(40)text,subject(30)text,marks(3)integer\n1,abc,physics,53\n1,xyz,physics,73",
			"csv_ml,1.0\nstudent,name(40)text,subject(30)text=physics,marks(3)integer\n1,abc,pqr,maths,53\n1,xyz,,chemistry,73",
			"csv_ml,1.0\nstudent,name(40)text,nick(30)text=null,subject(30)text,marks(3)integer\n1,abc,pqr,physics,53\n1,xyz,,physics,73",
			"csv_ml,1.0\nstudent,name(40)text,nick(30)text=,subject(30)text,marks(3)integer\n1,abc,pqr,physics,53\n1,xyz,,physics,73",
			"csv_ml,1.0\nstudent,name(40)text,subject(30)text,\"marks(6,2)numeric\"\n1,abc,physics,53.34\n1,xyz,physics,73.5",
			"csv_ml,1.0\nstudent,name,subject,marks,birth_date()date,join_date_time()datetime\n1,abc,physics,53.34,1982-01-23,2014-02-22 09:30:00\n1,xyz,physics,73.5,1985-11-12,2014-02-24 15:45:30",
			"csv_ml,1.0\nstudent,id,name,subject,marks\n1,,abc,physics,53\n1,,abc,chemistry,54\n1,3,xyz,physics,73\n1,*4,xyz,physics,73",
			"csv_ml,1.0\nstudent,name,age\n education,course_name,year_passed\n references,name,company,designation\n1,abc,24\n 1,bs,2010\n 1,ms,2012\n 2,pqr,bbb,executive\n 2,mno,bbb,director's secretary"};

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btnXML) {
		   toXML();
		}
		if (v.getId() == R.id.btnJSON) {
		   toJSON();
		}
	}

}
