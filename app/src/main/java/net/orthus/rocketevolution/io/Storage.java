package net.orthus.rocketevolution.io;

import android.content.Context;

import java.io.File;

/**
 * Created by Chad on 19-Mar-16.
 */
public class Storage {

    private File internal;

    public Storage(Context context){

        internal = context.getFilesDir();
    }




} // Storage
