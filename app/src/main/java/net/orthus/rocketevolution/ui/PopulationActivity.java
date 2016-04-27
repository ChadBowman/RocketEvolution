package net.orthus.rocketevolution.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;

import net.orthus.rocketevolution.R;
import net.orthus.rocketevolution.utility.Hash;

import java.util.ArrayList;
import java.util.List;

public class PopulationActivity extends AppCompatActivity {

    ArrayList<ArrayList<String>> population;
    Hash<String, List<String>> generations = new Hash<>();
    List<String> genList = new ArrayList<>();
    ExpandableListView expList;
    ExpandableListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_population);

        expList = (ExpandableListView) findViewById(R.id.pop_list);

        population = (ArrayList<ArrayList<String>>) getIntent().getExtras().get("pop");
        //String r = (String) getIntent().getExtras().get("test");

        for(int i=0; i < population.size(); i++){
            genList.add("Generation " + i+1);
            generations.add("Generation " + i+1, population.get(i));
        }
        ///ArrayList<String> l = new ArrayList<>();
        //l.add(r);
        //genList.add("Generation 1");
        //generations.add("Generation 1", l);

        adapter = new ExpandableListAdapter(this, generations, genList);
        expList.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_population, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

} // PopulationActivity
