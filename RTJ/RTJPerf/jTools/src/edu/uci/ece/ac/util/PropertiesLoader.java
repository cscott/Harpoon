// ************************************************************************
//    $Id: PropertiesLoader.java,v 1.1 2002-07-02 15:35:46 wbeebee Exp $
// ************************************************************************
//
//                               jTools
//
//               Copyright (C) 2001-2002 by Angelo Corsaro.
//                         <corsaro@ece.uci.edu>
//                          All Rights Reserved.
//
//   Permission to use, copy, modify, and distribute this software and
//   its  documentation for any purpose is hereby  granted without fee,
//   provided that the above copyright notice appear in all copies and
//   that both that copyright notice and this permission notice appear
//   in  supporting  documentation. I don't make  any  representations
//   about the  suitability  of this  software for any  purpose. It is
//   provided "as is" without express or implied warranty.
//
//
// *************************************************************************
//  
// *************************************************************************

package edu.uci.ece.ac.jargo;

// -- Java's JDK Import --
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * This class loads a set of properties, given the name of the file
 * that supposedly contain those. The property file is searched in the
 * standard location like the home dir etc. A specific location for
 * the file can be set by using the -D option while running the
 * application that uses this class. As an example, if you need to
 * read the properties specified in the file "myapp.properties" you
 * could define its location by passing to the JVM the following
 * command line argument:
 *
 *          -DmyPropertyName.path = /some/path/in/my/fs
 *
 * It is also possible to specify a global load path for properties
 * files. This can be done by property defining the property
 * "properties.load.path". To do this simply add something like this
 * while launching your app:
 *
 *          -Dproperties.load.path = /the/property/load/path/for/my/app
 *
 *
 * @author <a href="mailto:corsaro@doc.ece.uci.edu">Angelo Corsaro</a>
 * @version 1.0
 */
public class PropertiesLoader {

    private final static String DIR_EXT = ".dir";
    private final static String PROP_EXT = ".properties";
    public  final static String PROPERTIES_LOAD_PATH_PROPERTY = "properties.load.path";
    
    /**
     * Loads a set of properties that are contained in a file
     * that has the same name as the one of the property set.
     *
     * @param propertySetName a <code>String</code> representing the
     * property set name. Note that the file containing the property
     * set should be named exactly as the property set plus the
     * "properties" extension. As an example, if you have a set of
     * properties representing your application defaults settings,
     * you could name the property set as "myApp" and the file
     * containing the properties for this category should be
     * "myApp.properties".
     *
     * @param propertyName a <code>String</code> value
     * @param customEnvironmentProperties a <code>Properties</code> value
     * @return a <code>Properties</code> value
     * @exception IOException if an error occurs
     */
    static Properties loadProperties(String propertyName,
                                     Properties customEnvironmentProperties)
        throws IOException
    {
        Properties[] properties;
        int index = 0;
        if (customEnvironmentProperties != null) {
             properties = new Properties[2];
             properties[index++] = customEnvironmentProperties;
             
        }
        else
            properties = new Properties[1];

        properties[index] = System.getProperties();
        
        
        String pathProperty = propertyName + DIR_EXT;
        String propertieFilePath = resolveProperty(pathProperty, properties);
        
        if (propertieFilePath == null)
            propertieFilePath = resolveProperty(PROPERTIES_LOAD_PATH_PROPERTY, properties);
        
        if (propertieFilePath == null)
            propertieFilePath = resolveProperty("user.home", properties);
                                                   
        Properties retVal = null;
        
        try {
            String fullyQualifiedFileName = propertieFilePath + "/" +
                propertyName + PROP_EXT;
            
            java.io.FileInputStream propertyStream =
                new java.io.FileInputStream(new java.io.File(fullyQualifiedFileName));
            retVal = new Properties();
            retVal.load(propertyStream);
        }
        catch (java.io.IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        return retVal;
    }


    /**
     * Loads a set of properties that are contained in a file
     * that has the same name as the one of the property set.
     *
     * @param propertySetName a <code>String</code> representing the
     * property set name. Note that the file containing the property
     * set should be named exactly as the property set plus the
     * "properties" extension. As an example, if you have a set of
     * properties representing your application defaults settings,
     * you could name the property set as "myApp" and the file
     * containing the properties for this category should be
     * "myApp.properties".
     *
     * @param propertyName a <code>String</code> value
     * @return a <code>Properties</code> value
     * @exception IOException if an error occurs
     */
    public static Properties loadProperties(String propertyName) throws IOException {
        return loadProperties(propertyName, null);
    }


    private static String resolveProperty(String propertyName, Properties[]  properties) {
        String propertyValue = null;
        int index = 0;
        
        while (propertyValue == null && index < properties.length) {
            propertyValue = properties[index].getProperty(propertyName);
            ++index;
        }

        return propertyValue;
    }

}
