package com.google.code.play;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class PidFileDeleter
    extends Thread
{
    private static PidFileDeleter INSTANCE;

    private Set<File> pidFiles = new HashSet<File>( 1 );

    private PidFileDeleter()
    {
        super( "PidFileDeleter Shutdown Hook" );
    }

    public static synchronized PidFileDeleter getInstance()
    {
        if ( INSTANCE == null )
        {
            INSTANCE = new PidFileDeleter();
            Runtime.getRuntime().addShutdownHook( INSTANCE );
        }
        return INSTANCE;
    }

    public void add( File pidFile )
    {
        pidFiles.add( pidFile );
    }

    public void remove( File pidFile )
    {
        pidFiles.remove( pidFile );
    }

    public void run()
    {
        for ( File pidFile : pidFiles )
        {
            if ( pidFile != null && pidFile.isFile() )
            {
                if ( !pidFile.delete() )
                {
                    System.out.println( String.format( "Cannot delete %s file", pidFile.getAbsolutePath() ) );
                }
            }
        }
    }

}
