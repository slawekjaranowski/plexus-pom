package org.codehaus.plexus.component.configurator.converters.composite;

/*
 * The MIT License
 *
 * Copyright (c) 2004, The Codehaus
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.converters.AbstractConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.ConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.util.StringUtils;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * @author <a href="mailto:michal@codehaus.org">Michal Maczka</a>
 * @version $Id$
 */
public class CollectionConverter extends AbstractConfigurationConverter
{
    public boolean canConvert( Class type )
    {
        return Collection.class.isAssignableFrom( type );
    }

    public Object fromConfiguration( ConverterLookup converterLookup,
                                     PlexusConfiguration configuration,
                                     Class type,
                                     Class baseType,
                                     ClassLoader classLoader )
        throws ComponentConfigurationException
    {
        Collection retValue = null;

        Class implementation = getClassForImplementationHint( null, configuration, classLoader );

        if ( implementation != null )
        {
            retValue = ( Collection ) instantiateObject( implementation );
        }
        else
        {
            // we can have 2 cases here:
            //  - provided collection class which is not abstract
            //     like Vector, ArrayList, HashSet - so we will just instantantiate it
            // - we have an abtract class so we have to use default collection type
            int modifiers = type.getModifiers();

            if ( Modifier.isAbstract( modifiers ) )
            {
                retValue = getDefaultCollection( type );
            }
            else
            {
                try
                {
                    retValue = ( Collection ) type.newInstance();
                }
                catch ( Exception e )
                {
                    String msg = "An attempt to convert configuration entry "+
                            configuration.getName() +
                            "' into Collection object failed: " + e.getMessage();

                    throw new ComponentConfigurationException( msg );
               }
            }
        }
        // now we have collection and we have to add some objects to it

        for ( int i = 0; i < configuration.getChildCount(); i++ )
        {
            PlexusConfiguration c = configuration.getChild( i );
            //Object o = null;

            String configEntry = c.getName();

            String basePackage = baseType.getPackage().getName();

            String name = StringUtils.capitalizeFirstLetter( fromXML( configEntry ) );

            String classname = basePackage + "." + name;

            Class childType = getClassForImplementationHint( null, c, classLoader );

            if ( childType == null )
            {
                childType = loadClass( classname, classLoader );
            }

            ConfigurationConverter converter = converterLookup.lookupConverterForType( childType );

            Object object = converter.fromConfiguration( converterLookup, c, childType, baseType, classLoader );

            retValue.add( object );
        }

        return retValue;
    }

    protected Collection getDefaultCollection( Class collectionType )
    {
        Collection retValue = null;

        if ( List.class.isAssignableFrom( collectionType ) )
        {
            retValue = new ArrayList();
        }
        else if ( Set.class.isAssignableFrom( collectionType ) )
        {
            retValue = new HashSet();
        }

        return retValue;
    }

}
