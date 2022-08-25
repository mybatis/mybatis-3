/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

public final class MavenWrapperDownloader
{
    private static final String WRAPPER_VERSION = "3.1.1";

    private static final boolean VERBOSE = Boolean.parseBoolean( System.getenv( "MVNW_VERBOSE" ) );

    /**
     * Default URL to download the maven-wrapper.jar from, if no 'downloadUrl' is provided.
     */
    private static final String DEFAULT_DOWNLOAD_URL =
        "https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/" + WRAPPER_VERSION
            + "/maven-wrapper-" + WRAPPER_VERSION + ".jar";

    /**
     * Path to the maven-wrapper.properties file, which might contain a downloadUrl property to use instead of the
     * default one.
     */
    private static final String MAVEN_WRAPPER_PROPERTIES_PATH = ".mvn/wrapper/maven-wrapper.properties";

    /**
     * Path where the maven-wrapper.jar will be saved to.
     */
    private static final String MAVEN_WRAPPER_JAR_PATH = ".mvn/wrapper/maven-wrapper.jar";

    /**
     * Name of the property which should be used to override the default download url for the wrapper.
     */
    private static final String PROPERTY_NAME_WRAPPER_URL = "wrapperUrl";

    public static void main( String[] args )
    {
        if ( args.length == 0 )
        {
            System.err.println( " - ERROR projectBasedir parameter missing" );
            System.exit( 1 );
        }

        log( " - Downloader started" );
        final String dir = args[0].replace( "..", "" ); // Sanitize path
        final Path projectBasedir = Paths.get( dir ).toAbsolutePath().normalize();
        if ( !Files.isDirectory( projectBasedir, LinkOption.NOFOLLOW_LINKS ) )
        {
            System.err.println( " - ERROR projectBasedir not exists: " + projectBasedir );
            System.exit( 1 );
        }

        log( " - Using base directory: " + projectBasedir );

        // If the maven-wrapper.properties exists, read it and check if it contains a custom
        // wrapperUrl parameter.
        Path mavenWrapperPropertyFile = projectBasedir.resolve( MAVEN_WRAPPER_PROPERTIES_PATH );
        String url = readWrapperUrl( mavenWrapperPropertyFile );

        try
        {
            Path outputFile = projectBasedir.resolve( MAVEN_WRAPPER_JAR_PATH );
            createDirectories( outputFile.getParent() );
            downloadFileFromURL( url, outputFile );
            log( "Done" );
            System.exit( 0 );
        }
        catch ( IOException e )
        {
            System.err.println( "- Error downloading" );
            e.printStackTrace();
            System.exit( 1 );
        }
    }

    private static void downloadFileFromURL( String urlString, Path destination ) throws IOException
    {
        log( " - Downloading to: " + destination );
        if ( System.getenv( "MVNW_USERNAME" ) != null && System.getenv( "MVNW_PASSWORD" ) != null )
        {
            final String username = System.getenv( "MVNW_USERNAME" );
            final char[] password = System.getenv( "MVNW_PASSWORD" ).toCharArray();
            Authenticator.setDefault( new Authenticator()
            {
                @Override
                protected PasswordAuthentication getPasswordAuthentication()
                {
                    return new PasswordAuthentication( username, password );
                }
            } );
        }
        URL website = new URL( urlString );
        try ( InputStream inStream = website.openStream() ) {
            Files.copy( inStream, destination, StandardCopyOption.REPLACE_EXISTING );
        }
        log( " - Downloader complete" );
    }

    private static void createDirectories(Path outputPath) throws IOException
    {
        if ( !Files.isDirectory( outputPath, LinkOption.NOFOLLOW_LINKS ) ) {
            Path createDirectories = Files.createDirectories( outputPath );
            log( " - Directories created: " + createDirectories );
        }
    }

    private static String readWrapperUrl( Path mavenWrapperPropertyFile )
    {
        String url = DEFAULT_DOWNLOAD_URL;
        if ( Files.exists( mavenWrapperPropertyFile, LinkOption.NOFOLLOW_LINKS ) )
        {
            log( " - Reading property file: " + mavenWrapperPropertyFile );
            try ( InputStream in = Files.newInputStream( mavenWrapperPropertyFile, StandardOpenOption.READ ) )
            {
                Properties mavenWrapperProperties = new Properties();
                mavenWrapperProperties.load( in );
                url = mavenWrapperProperties.getProperty( PROPERTY_NAME_WRAPPER_URL, DEFAULT_DOWNLOAD_URL );
            }
            catch ( IOException e )
            {
                System.err.println( " - ERROR loading '" + MAVEN_WRAPPER_PROPERTIES_PATH + "'" );
            }
        }
        log( " - Downloading from: " + url );
        return url;
    }

    private static void log( String msg )
    {
        if ( VERBOSE )
        {
            System.out.println( msg );
        }
    }

}
