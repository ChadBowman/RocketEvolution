package net.orthus.rocketevolution;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import net.orthus.rocketevolution.engine.Player;
import net.orthus.rocketevolution.evolution.Chromosome;
import net.orthus.rocketevolution.fuels.Fuel;
import net.orthus.rocketevolution.fuels.KerosenePeroxide;
import net.orthus.rocketevolution.materials.Material;
import net.orthus.rocketevolution.materials.TestMaterial;
import net.orthus.rocketevolution.evolution.Generation;
import net.orthus.rocketevolution.rocket.Rocket;
import net.orthus.rocketevolution.ui.Launchpad;
import net.orthus.rocketevolution.utility.Hash;
import net.orthus.rocketevolution.utility.Utility;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;


public class Game extends Activity {

    public static final String PLAYER_FILE = "player.ply";
    private Launchpad launchpad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Turn title off
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Set to full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Add fuels
        Fuel.fuels.add(Fuel.KEROSENE_PEROXIDE, new KerosenePeroxide("", 810, 8));
        // Add materials
        Material.materials.add(Material.TEST_MAT, new TestMaterial());

        launchpad = new Launchpad(this);

        File playerFile = new File(getFilesDir(), PLAYER_FILE);
        Utility.p("Player file exists: %b", playerFile.exists());

        Player player;
        if(!playerFile.exists())
            player = new Player();
        else {
            player = Player.load(playerFile);
            Player.selectedFitness = player.getSelectedFitness();
        }

        Generation currentGen;
        // player is a virgin, let's generate them some fresh rockets ;)
        if(player.getGeneration().size() == 0) {
            currentGen = new Generation(Generation.GENERATION_SIZE);
            currentGen.runSims();
            player.setGeneration(currentGen.getGeneration().keys());

        } else {

            ArrayList<UUID> ids = player.getGeneration();
            currentGen = new Generation();
            Hash<UUID, Rocket> rockets = new Hash<>();

            File c;
            Rocket r;
            for(int i=0; i < ids.size(); i++){
                c = new File(getFilesDir(), ids.get(i).toString() + ".roc");
                r = new Rocket(Chromosome.load(c), ids.get(i));
                rockets.add(ids.get(i), r);
            }
            Utility.p("Loaded %d rockets", rockets.entries());
            currentGen.setGeneration(rockets);
            currentGen.runSims();
        }

        launchpad.setPlayer(player);
        launchpad.setCurrentGen(currentGen);

        // Start launchpad up
    }

    @Override
    public void onResume(){
        super.onResume();
        setContentView(launchpad);
    }


    @Override
    public void onPause(){
        super.onPause();
        if(launchpad.getThread() != null)
            launchpad.kill();

        cleanUp();
    }

    @Override
    public void onStop(){
        super.onStop();

        launchpad.getPlayer().save(getFilesDir());
        launchpad.getCurrentGen().saveAll(getFilesDir());
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

    //===== PRIVATE METHODS
    private void cleanUp(){
        // clean up unneeded files
        File[] fs = getFilesDir().listFiles();
        int i =0;
        try {
            for (File f : fs)
                if (isSafeToDelete(f.getName()))
                    if(!f.delete()) {
                        throw new Exception(String.format("%s failed to delete!", f.getName()));
                    }else
                        i++;

        }catch (Exception e){
            e.printStackTrace();
        }

        Utility.p("%d files deleted", i);
    }

    private boolean isSafeToDelete(String fileName){

        ArrayList<UUID> ids = launchpad.getPlayer().getGeneration();

        if(fileName.equals(PLAYER_FILE))
            return false;

        for(UUID id : ids)
            if(fileName.equals(id.toString() + ".roc"))
                return false;

        return true;
    }

} // Game
