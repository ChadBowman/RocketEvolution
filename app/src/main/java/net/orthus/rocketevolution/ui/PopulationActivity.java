package net.orthus.rocketevolution.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;

import net.orthus.rocketevolution.R;
import net.orthus.rocketevolution.population.Generation;
import net.orthus.rocketevolution.rocket.Rocket;
import net.orthus.rocketevolution.utility.Hash;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PopulationActivity extends AppCompatActivity {

    ArrayList<ArrayList<String>> population;
    Hash<String, List<String>> generations;
    List<String> genList;
    ExpandableListView expList;
    ExpandableListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_population);

        expList = (ExpandableListView) findViewById(R.id.pop_list);
        population = (ArrayList<ArrayList<String>>) savedInstanceState.get("pop");


        genList = new ArrayList<>(generations.keys());

        //adapter = new ExpandableListAdapter(this, generations, genList);
        //expList.setAdapter(adapter);
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
