package com.google.code.play;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * Custom implementation of java.util.Properties that preserves the key-order from the file
 * and that reads the properties-file in UTF-8.
 */
public class OrderSafeProperties
    extends Properties
{
    private static final long serialVersionUID = 1L;

    // set used to preserve key order
    private final LinkedHashSet<Object> keys = new LinkedHashSet<Object>();

    @Override
    public void load( InputStream inputStream )
        throws IOException
    {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // read all lines from file as utf-8
        BufferedReader confReader = new BufferedReader( new InputStreamReader( inputStream, "UTF-8" ) );
        String line = confReader.readLine();
        while ( line != null )
        {
            // escape "special-chars" (to utf-16 on the format \\uxxxx) in lines and store as iso-8859-1
            // see info about escaping - http://download.oracle.com/javase/1.5.0/docs/api/java/util/Properties.html -
            // "public void load(InputStream inStream)"

            // due to "...by the rule above, single and double quote characters preceded
            // by a backslash still yield single and double quote characters, respectively."
            // we must transform \" => " and \' => ' before escaping to prevent escaping the backslash
            line = line.replaceAll( "\\\\\"", "\"" ).replaceAll( "(^|[^\\\\])(\\\\')", "$1'" );

            String escapedLine = StringEscapeUtils.escapeJava( line ) + "\n";
            // remove escaped backslashes
            escapedLine = escapedLine.replaceAll( "\\\\\\\\", "\\\\" );
            out.write( escapedLine.getBytes( "iso-8859-1" ) );

            line = confReader.readLine();
        }

        // read properties-file with regular java.util.Properties impl
        super.load( new ByteArrayInputStream( out.toByteArray() ) );
    }

    @Override
    public Enumeration<Object> keys()
    {
        return Collections.<Object> enumeration( keys );
    }

    @Override
    public Set<Object> keySet()
    {
        return keys;
    }

    @Override
    public Object put( Object key, Object value )
    {
        keys.add( key );
        return super.put( key, value );
    }

    @Override
    public Object remove( Object o )
    {
        keys.remove( o );
        return super.remove( o );
    }

    @Override
    public void clear()
    {
        keys.clear();
        super.clear();
    }

    @Override
    public void putAll( Map<? extends Object, ? extends Object> map )
    {
        keys.addAll( map.keySet() );
        super.putAll( map );
    }

    @Override
    public Set<Map.Entry<Object, Object>> entrySet()
    {
        Set<Map.Entry<Object, Object>> entrySet = new LinkedHashSet<Map.Entry<Object, Object>>( keys.size() );
        for ( Object key : keys )
        {
            entrySet.add( new Entry( key, get( key ) ) );
        }

        return entrySet;
    }

    private static class Entry
        implements Map.Entry<Object, Object>
    {
        private final Object key;

        private final Object value;

        private Entry( Object key, Object value )
        {
            this.key = key;
            this.value = value;
        }

        public Object getKey()
        {
            return key;
        }

        public Object getValue()
        {
            return value;
        }

        public Object setValue( Object o )
        {
            throw new IllegalStateException( "not implemented" );
        }
    }

}
