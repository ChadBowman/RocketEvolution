package net.orthus.rocketevolution.Game;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import net.orthus.rocketevolution.R;
import net.orthus.rocketevolution.fuels.Fuel;
import net.orthus.rocketevolution.fuels.KerosenePeroxide;
import net.orthus.rocketevolution.materials.Material;
import net.orthus.rocketevolution.materials.TestMaterial;
import net.orthus.rocketevolution.ui.Launchpad;
import net.orthus.rocketevolution.utility.Utility;

import java.io.File;


public class Game extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Turn title off
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Set to full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Add fuels
        Fuel.fuels.add(Fuel.KEROSENE_PEROXIDE, new KerosenePeroxide("", 810, 8));
        // Add materials
        Material.materials.add(Material.TEST_MAT, new TestMaterial());

        File PLAYER_FILE = new File(getFilesDir(), "player");
        Utility.p("EXISTS: %b", PLAYER_FILE.exists());
        // Load player's file
        Player player = new Player(); //Player.load(PLAYER_FILE);

        // player not there, create new one
        //if(player == null)
        //    player = new Player();



        // Start launchpad up
        setContentView(new Launchpad(this, player));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_game, menu);
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

} // Game
