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

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.json.simple.JSONObject;
import org.json.simple.JSONWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

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
import cc.siara.csv_ml.DBBind;
import cc.siara.csv_ml.MultiLevelCSVParser;
import cc.siara.csv_ml.Outputter;
import cc.siara.csv_ml.Util;

/**
 * Android application to demonstrate the features of MultiLevelCSVParser
 * 
 * @author Arundale R.
 */
public class MainActivity extends ActionBarActivity implements
        OnItemSelectedListener, OnClickListener {

    /* (non-Javadoc)
     * @see android.support.v7.app.ActionBarActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Set event handlers
        setContentView(R.layout.activity_main);
        Button btnXML = (Button) findViewById(R.id.btnXML);
        btnXML.setOnClickListener(this);
        Button btnJSON = (Button) findViewById(R.id.btnJSON);
        btnJSON.setOnClickListener(this);
        Button btnXPath = (Button) findViewById(R.id.btnXPath);
        btnXPath.setOnClickListener(this);
        Button btnDDLDML = (Button) findViewById(R.id.btnDDLDML);
        btnDDLDML.setOnClickListener(this);
        Button btnXMLtoCSV = (Button) findViewById(R.id.btnXMLtoCSV);
        btnXMLtoCSV.setOnClickListener(this);
        Spinner spinner = (Spinner) findViewById(R.id.spExamples);
        spinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.input_examples,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

    }

    /* (non-Javadoc)
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Context menu not used
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /* (non-Javadoc)
     * @see android.widget.AdapterView.OnItemSelectedListener#onItemSelected(android.widget.AdapterView, android.view.View, int, long)
     */
    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
            long arg3) {
        // Sets Input text and XPath text based on selected example
        Spinner spinner = (Spinner) findViewById(R.id.spExamples);
        if (spinner.equals(arg0)) {
            EditText etInput = (EditText) findViewById(R.id.etInput);
            EditText etXPath = (EditText) findViewById(R.id.etXPath);
            int selectedPos = spinner.getSelectedItemPosition();
            etInput.setText(aExampleCSV[selectedPos]);
            etXPath.setText(aExampleXPath[selectedPos]);
            Button btnXML = (Button) findViewById(R.id.btnXML);
            btnXML.requestFocus();
        }
    }

    /* (non-Javadoc)
     * @see android.widget.AdapterView.OnItemSelectedListener#onNothingSelected(android.widget.AdapterView)
     */
    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }

    /**
     * Transforms csv_ml to xml
     */
    void toXML() {
        EditText etInput = (EditText) findViewById(R.id.etInput);
        CheckBox cbPretty = (CheckBox) findViewById(R.id.cbPretty);
        MultiLevelCSVParser parser = new MultiLevelCSVParser();
        try {
            Document doc = parser.parseToDOM(new StringReader(etInput.getText()
                    .toString()), false);
            String ex_str = parser.getEx().get_all_exceptions();
            if (ex_str.length() > 0) {
                Toast.makeText(getApplicationContext(), ex_str,
                        Toast.LENGTH_SHORT).show();
                if (parser.getEx().getErrorCode() > 0)
                    return;
            }
            String xmlString = Util.docToString(doc, cbPretty.isChecked());
            if (xmlString.length() > 5
                    && xmlString.substring(0, 5).equals("<?xml"))
                xmlString = xmlString.substring(xmlString.indexOf(">") + 1);
            EditText etOutput = (EditText) findViewById(R.id.etOutput);
            etOutput.setText(xmlString);
            // tfOutputSize.setText(String.valueOf(xmlString.length()));
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Evaluates given XPath from Input box against Document generated by
     * parsing csv_ml in input box and sets value or node list to output box.
     */
    void processXPath() {
        EditText etInput = (EditText) findViewById(R.id.etInput);
        EditText etXPath = (EditText) findViewById(R.id.etXPath);
        CheckBox cbPretty = (CheckBox) findViewById(R.id.cbPretty);
        XPath xpath = XPathFactory.newInstance().newXPath();
        MultiLevelCSVParser parser = new MultiLevelCSVParser();
        Document doc = null;
        try {
            doc = parser.parseToDOM(new StringReader(etInput.getText()
                    .toString()), false);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        if (doc == null)
            return;
        StringBuffer out_str = new StringBuffer();
        try {
            XPathExpression expr = xpath.compile(etXPath.getText().toString());
            try {
                Document outDoc = Util.parseXMLToDOM("<output></output>");
                Element rootElement = outDoc.getDocumentElement();
                NodeList ret = (NodeList) expr.evaluate(doc,
                        XPathConstants.NODESET);
                for (int i = 0; i < ret.getLength(); i++) {
                    Object o = ret.item(i);
                    if (o instanceof String) {
                        out_str.append(o);
                    } else if (o instanceof Node) {
                        Node n = (Node) o;
                        short nt = n.getNodeType();
                        switch (nt) {
                        case Node.TEXT_NODE:
                        case Node.ATTRIBUTE_NODE:
                        case Node.CDATA_SECTION_NODE: // Only one value gets
                                                      // evaluated?
                            if (out_str.length() > 0)
                                out_str.append(',');
                            if (nt == Node.ATTRIBUTE_NODE)
                                out_str.append(n.getNodeValue());
                            else
                                out_str.append(n.getTextContent());
                            break;
                        case Node.ELEMENT_NODE:
                            rootElement.appendChild(outDoc.importNode(n, true));
                            break;
                        }
                    }
                }
                if (out_str.length() > 0) {
                    rootElement.setTextContent(out_str.toString());
                    out_str.setLength(0);
                }
                out_str.append(Util.docToString(outDoc, true));
            } catch (Exception e) {
                // Thrown most likely because the given XPath evaluates to a
                // string
                out_str.append(expr.evaluate(doc));
            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        if (out_str.length() > 5 && out_str.substring(0, 5).equals("<?xml"))
            out_str.delete(0, out_str.indexOf(">") + 1);
        EditText etOutput = (EditText) findViewById(R.id.etOutput);
        etOutput.setText(out_str.toString());
        // tfOutputSize.setText(String.valueOf(xmlString.length()));
    }

    /**
     * Parses input from textbox and generates DDL/DML statements and sets to
     * output text box
     */
    private void toDDLDML() {
        EditText etInput = (EditText) findViewById(R.id.etInput);
        MultiLevelCSVParser parser = new MultiLevelCSVParser();
        Document doc = null;
        try {
            doc = parser.parseToDOM(new StringReader(etInput.getText()
                    .toString()), false);
            String ex_str = parser.getEx().get_all_exceptions();
            if (ex_str.length() > 0) {
                Toast.makeText(getApplicationContext(), ex_str,
                        Toast.LENGTH_LONG).show();
                if (parser.getEx().getErrorCode() > 0)
                    return;
            }
            StringBuffer out_str = new StringBuffer();
            DBBind.generateDDL(parser.getSchema(), out_str);
            if (out_str.length() > 0) {
                out_str.append("\r\n");
                DBBind.generate_dml_recursively(parser.getSchema(),
                        doc.getDocumentElement(), "", out_str);
            } else {
                out_str.append("No schema");
            }
            EditText etOutput = (EditText) findViewById(R.id.etOutput);
            etOutput.setText(out_str.toString());
            //tfOutputSize.setText(String.valueOf(out_str.length()));
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(),
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    /**
     * Converts XML in output text box back to csv_ml and sets to input text box
     */
    private void xmlToCSV() {
        EditText etOutput = (EditText) findViewById(R.id.etInput);
        Document doc = null;
        try {
            doc = DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse(new InputSource(new StringReader(etOutput.getText().toString())));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    "Could not parse. XML expected in Output text box",
                    Toast.LENGTH_LONG).show();
            return;
        }
        String out_str = Outputter.generate(doc);
        EditText etInput = (EditText) findViewById(R.id.etInput);
        etInput.setText(out_str.toString());
        etInput.setText(out_str);
        //tfInputSize.setText(String.valueOf(out_str.length()));
    }

    /**
     * Transforms csv_ml to JSON Object
     */
    void toJSON() {
        EditText etInput = (EditText) findViewById(R.id.etInput);
        CheckBox cbPretty = (CheckBox) findViewById(R.id.cbPretty);
        MultiLevelCSVParser parser = new MultiLevelCSVParser();
        try {
            JSONObject jo = parser.parseToJSO(new StringReader(etInput
                    .getText().toString()), false);
            String ex_str = parser.getEx().get_all_exceptions();
            if (ex_str.length() > 0) {
                Toast.makeText(getApplicationContext(), ex_str,
                        Toast.LENGTH_LONG).show();
                if (parser.getEx().getErrorCode() > 0)
                    return;
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
            // tfOutputSize.setText(String.valueOf(outStr.length()));
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    // List of example csv_ml strings
    String[] aExampleCSV = new String[] {
            "abc,physics,53\nabc,chemistry,65\nxyz,physics,73\nxyz,chemistry,76",
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
            "csv_ml,1.0\nstudent,name,age\n education,course_name,year_passed\n references,name,company,designation\n1,abc,24\n 1,bs,2010\n 1,ms,2012\n 2,pqr,bbb,executive\n 2,mno,bbb,director's secretary" };

    // List of XPath strings
    String[] aExampleXPath = new String[] {
            "concat('Total of xyz:', sum(root/n1[@c1='xyz']/@c3))",
            "concat('Total of xyz:', sum(root/n1[@name='xyz']/@marks))",
            "concat('Total of xyz:', sum(root/student[@name='xyz']/@marks))",
            "concat('Total of xyz:', sum(root/student[@name='xyz']/@marks))",
            "concat('Total of xyz:', sum(root/student[@name='xyz']/@marks))",
            "/root/student[education/subject/@marks > 80]",
            "/root/student[education/subject/@marks > 80]",
            "/root/student[@name='c']/text()", "/root/student/name/text()",
            "/root/sample[3]/@text", "/root/sample[2]/@text2",
            "/data/student/@name", "/student/@name", "/root", "/root",
            "/root/our:student[his:name='b']",
            "/root/our:student[his:name='b']", "/xsl:stylesheet",
            "/xsl:stylesheet", "", "", "", "", "", "", "", "", "", "" };

    /* (non-Javadoc)
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnXML) {
            toXML();
        }
        if (v.getId() == R.id.btnJSON) {
            toJSON();
        }
        if (v.getId() == R.id.btnXPath) {
            processXPath();
        }
    }

}
