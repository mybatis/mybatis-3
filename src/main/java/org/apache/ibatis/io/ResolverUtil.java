/* Copyright 2005-2006 Tim Fennell
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.io;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.regex.Pattern;

/**
 * <p>ResolverUtil is used to locate classes that are available in the/a class path and meet
 * arbitrary conditions. The two most common conditions are that a class implements/extends
 * another class, or that is it annotated with a specific annotation. However, through the use
 * of the {@link Test} class it is possible to search using arbitrary conditions.</p>
 * <p/>
 * <p>A ClassLoader is used to locate all locations (directories and jar files) in the class
 * path that contain classes within certain packages, and then to load those classes and
 * check them. By default the ClassLoader returned by
 * {@code Thread.currentThread().getContextClassLoader()} is used, but this can be overridden
 * by calling {@link #setClassLoader(ClassLoader)} prior to invoking any of the {@code find()}
 * methods.</p>
 * <p/>
 * <p>General searches are initiated by calling the
 * {@link #find(org.apache.ibatis.io.ResolverUtil.Test, String)} ()} method and supplying
 * a package name and a Test instance. This will cause the named package <b>and all sub-packages</b>
 * to be scanned for classes that meet the test. There are also utility methods for the common
 * use cases of scanning multiple packages for extensions of particular classes, or classes
 * annotated with a specific annotation.</p>
 * <p/>
 * <p>The standard usage pattern for the ResolverUtil class is as follows:</p>
 * <p/>
 * <pre>
 * ResolverUtil&lt;ActionBean&gt; resolver = new ResolverUtil&lt;ActionBean&gt;();
 * resolver.findImplementation(ActionBean.class, pkg1, pkg2);
 * resolver.find(new CustomTest(), pkg1);
 * resolver.find(new CustomTest(), pkg2);
 * Collection&lt;ActionBean&gt; beans = resolver.getClasses();
 * </pre>
 *
 * @author Tim Fennell
 */
public class ResolverUtil<T> {
	/**
	 * An instance of Log to use for logging in this class.
	 */
	private static final Log log = LogFactory.getLog(ResolverUtil.class);

	/**
	 * The magic header that indicates a JAR (ZIP) file.
	 */
	private static final byte[] JAR_MAGIC = {'P', 'K', 3, 4};

	/**
	 * Regular expression that matches a Java identifier.
	 */
	private static final Pattern JAVA_IDENTIFIER_PATTERN = Pattern
			.compile("\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*");

	/**
	 * A simple interface that specifies how to test classes to determine if they
	 * are to be included in the results produced by the ResolverUtil.
	 */
	public static interface Test {
		/**
		 * Will be called repeatedly with candidate classes. Must return True if a class
		 * is to be included in the results, false otherwise.
		 */
		boolean matches(Class<?> type);
	}

	/**
	 * A Test that checks to see if each class is assignable to the provided class. Note
	 * that this test will match the parent type itself if it is presented for matching.
	 */
	public static class IsA implements Test {
		private Class<?> parent;

		/**
		 * Constructs an IsA test using the supplied Class as the parent class/interface.
		 */
		public IsA(Class<?> parentType) {
			this.parent = parentType;
		}

		/**
		 * Returns true if type is assignable to the parent type supplied in the constructor.
		 */
		@SuppressWarnings("unchecked")
		public boolean matches(Class type) {
			return type != null && parent.isAssignableFrom(type);
		}

		@Override
		public String toString() {
			return "is assignable to " + parent.getSimpleName();
		}
	}

	/**
	 * A Test that checks to see if each class is annotated with a specific annotation. If it
	 * is, then the test returns true, otherwise false.
	 */
	public static class AnnotatedWith implements Test {
		private Class<? extends Annotation> annotation;

		/**
		 * Constructs an AnnotatedWith test for the specified annotation type.
		 */
		public AnnotatedWith(Class<? extends Annotation> annotation) {
			this.annotation = annotation;
		}

		/**
		 * Returns true if the type is annotated with the class provided to the constructor.
		 */
		@SuppressWarnings("unchecked")
		public boolean matches(Class type) {
			return type != null && type.isAnnotationPresent(annotation);
		}

		@Override
		public String toString() {
			return "annotated with @" + annotation.getSimpleName();
		}
	}

	/**
	 * The set of matches being accumulated.
	 */
	private Set<Class<? extends T>> matches = new HashSet<Class<? extends T>>();

	/**
	 * The ClassLoader to use when looking for classes. If null then the ClassLoader returned
	 * by Thread.currentThread().getContextClassLoader() will be used.
	 */
	private ClassLoader classloader;

	/**
	 * Provides access to the classes discovered so far. If no calls have been made to
	 * any of the {@code find()} methods, this set will be empty.
	 *
	 * @return the set of classes that have been discovered.
	 */
	public Set<Class<? extends T>> getClasses() {
		return matches;
	}

	/**
	 * Returns the classloader that will be used for scanning for classes. If no explicit
	 * ClassLoader has been set by the calling, the context class loader will be used.
	 *
	 * @return the ClassLoader that will be used to scan for classes
	 */
	public ClassLoader getClassLoader() {
		return classloader == null ? Thread.currentThread().getContextClassLoader() : classloader;
	}

	/**
	 * Sets an explicit ClassLoader that should be used when scanning for classes. If none
	 * is set then the context classloader will be used.
	 *
	 * @param classloader a ClassLoader to use when scanning for classes
	 */
	public void setClassLoader(ClassLoader classloader) {
		this.classloader = classloader;
	}

	/**
	 * Attempts to discover classes that are assignable to the type provided. In the case
	 * that an interface is provided this method will collect implementations. In the case
	 * of a non-interface class, subclasses will be collected.  Accumulated classes can be
	 * accessed by calling {@link #getClasses()}.
	 *
	 * @param parent	   the class of interface to find subclasses or implementations of
	 * @param packageNames one or more package names to scan (including subpackages) for classes
	 */
	public ResolverUtil<T> findImplementations(Class<?> parent, String... packageNames) {
		if (packageNames == null) return this;

		Test test = new IsA(parent);
		for (String pkg : packageNames) {
			find(test, pkg);
		}

		return this;
	}

	/**
	 * Attempts to discover classes that are annotated with the annotation. Accumulated
	 * classes can be accessed by calling {@link #getClasses()}.
	 *
	 * @param annotation   the annotation that should be present on matching classes
	 * @param packageNames one or more package names to scan (including subpackages) for classes
	 */
	public ResolverUtil<T> findAnnotated(Class<? extends Annotation> annotation, String... packageNames) {
		if (packageNames == null) return this;

		Test test = new AnnotatedWith(annotation);
		for (String pkg : packageNames) {
			find(test, pkg);
		}

		return this;
	}

	/**
	 * Scans for classes starting at the package provided and descending into subpackages.
	 * Each class is offered up to the Test as it is discovered, and if the Test returns
	 * true the class is retained.  Accumulated classes can be fetched by calling
	 * {@link #getClasses()}.
	 *
	 * @param test		an instance of {@link Test} that will be used to filter classes
	 * @param packageName the name of the package from which to start scanning for
	 *                    classes, e.g. {@code net.sourceforge.stripes}
	 */
	public ResolverUtil<T> find(Test test, String packageName) {
		String path = getPackagePath(packageName);

		try {
			List<URL> urls = Collections.list(getClassLoader().getResources(path));
			for (URL url : urls) {
				List<String> children = listClassResources(url, path);
				for (String child : children) {
					addIfMatching(test, child);
				}
			}
		}
		catch (IOException ioe) {

			log.error("Could not read package: " + packageName + " -- ", ioe);
		}

		return this;
	}

	/**
	 * Recursively list all resources under the given URL that appear to define a Java class.
	 * Matching resources will have a name that ends in ".class" and have a relative path such that
	 * each segment of the path is a valid Java identifier. The resource paths returned will be
	 * relative to the URL and begin with the specified path.
	 *
	 * @param url  The URL of the parent resource to search.
	 * @param path The path with which each matching resource path must begin, relative to the URL.
	 * @return A list of matching resources. The list may be empty.
	 * @throws IOException
	 */
	protected List<String> listClassResources(URL url, String path) throws IOException {
		log.debug("Listing classes in " + url);

		InputStream is = null;
		try {
			List<String> resources = new ArrayList<String>();

			// First, try to find the URL of a JAR file containing the requested resource. If a JAR
			// file is found, then we'll list child resources by reading the JAR.
			URL jarUrl = findJarForResource(url, path);
			if (jarUrl != null) {
				is = jarUrl.openStream();
				resources = listClassResources(new JarInputStream(is), path);
			} else {
				List<String> children = new ArrayList<String>();
				try {
					if (isJar(url)) {
						// Some versions of JBoss VFS might give a JAR stream even if the resource
						// referenced by the URL isn't actually a JAR
						is = url.openStream();
						JarInputStream jarInput = new JarInputStream(is);
						for (JarEntry entry; (entry = jarInput.getNextJarEntry()) != null;) {
							log.debug("Jar entry: " + entry.getName());
							if (isRelevantResource(entry.getName())) {
								children.add(entry.getName());
							}
						}
					} else {
						// Some servlet containers allow reading from "directory" resources like a
						// text file, listing the child resources one per line.
						is = url.openStream();
						BufferedReader reader = new BufferedReader(new InputStreamReader(is));
						for (String line; (line = reader.readLine()) != null;) {
							log.debug("Reader entry: " + line);
							if (isRelevantResource(line)) {
								children.add(line);
							}
						}
					}
				}
				catch (FileNotFoundException e) {
					/*
										 * For file URLs the openStream() call might fail, depending on the servlet
										 * container, because directories can't be opened for reading. If that happens,
										 * then list the directory directly instead.
										 */
					if ("file".equals(url.getProtocol())) {
						File file = new File(url.getFile());
						log.debug("Listing directory " + file.getAbsolutePath());
						if (file.isDirectory()) {
							children = Arrays.asList(file.list(new FilenameFilter() {
								public boolean accept(File dir, String name) {
									return isRelevantResource(name);
								}
							}));
						}
					} else {
						// No idea where the exception came from so rethrow it
						throw e;
					}
				}

				// The URL prefix to use when recursively listing child resources
				String prefix = url.toExternalForm();
				if (!prefix.endsWith("/"))
					prefix = prefix + "/";

				// Iterate over each immediate child, adding classes and recursing into directories
				for (String child : children) {
					String resourcePath = path + "/" + child;
					if (child.endsWith(".class")) {
						log.debug("Found class file: " + resourcePath);
						resources.add(resourcePath);
					} else {
						URL childUrl = new URL(prefix + child);
						resources.addAll(listClassResources(childUrl, resourcePath));
					}
				}
			}

			return resources;
		}
		finally {
			try {
				is.close();
			}
			catch (Exception e) {
			}
		}
	}

	/**
	 * List the names of the entries in the given {@link JarInputStream} that begin with the
	 * specified {@code path}. Entries will match with or without a leading slash.
	 *
	 * @param jar  The JAR input stream
	 * @param path The leading path to match
	 * @return The names of all the matching entries
	 * @throws IOException
	 */
	protected List<String> listClassResources(JarInputStream jar, String path) throws IOException {
		// Include the leading and trailing slash when matching names
		if (!path.startsWith("/"))
			path = "/" + path;
		if (!path.endsWith("/"))
			path = path + "/";

		// Iterate over the entries and collect those that begin with the requested path
		List<String> resources = new ArrayList<String>();
		for (JarEntry entry; (entry = jar.getNextJarEntry()) != null;) {
			if (!entry.isDirectory()) {
				// Add leading slash if it's missing
				String name = entry.getName();
				if (!name.startsWith("/"))
					name = "/" + name;

				// Check file name
				if (name.endsWith(".class") && name.startsWith(path)) {
					log.debug("Found class file: " + name);
					resources.add(name.substring(1)); // Trim leading slash
				}
			}
		}
		return resources;
	}

	/**
	 * Attempts to deconstruct the given URL to find a JAR file containing the resource referenced
	 * by the URL. That is, assuming the URL references a JAR entry, this method will return a URL
	 * that references the JAR file containing the entry. If the JAR cannot be located, then this
	 * method returns null.
	 *
	 * @param url  The URL of the JAR entry.
	 * @param path The path by which the URL was requested from the class loader.
	 * @return The URL of the JAR file, if one is found. Null if not.
	 * @throws MalformedURLException
	 */
	protected URL findJarForResource(URL url, String path) throws MalformedURLException {
		log.debug("Find JAR URL: " + url);

		// If the file part of the URL is itself a URL, then that URL probably points to the JAR
		try {
			for (; ;) {
				url = new URL(url.getFile());
				log.debug("Inner URL: " + url);
			}
		}
		catch (MalformedURLException e) {
			// This will happen at some point and serves a break in the loop
		}

		// Look for the .jar extension and chop off everything after that
		StringBuilder jarUrl = new StringBuilder(url.toExternalForm());
		int index = jarUrl.lastIndexOf(".jar");
		if (index >= 0) {
			jarUrl.setLength(index + 4);
			log.debug("Extracted JAR URL: " + jarUrl);
		} else {
			log.debug("Not a JAR: " + jarUrl);
			return null;
		}

		// Try to open and test it
		try {
			URL testUrl = new URL(jarUrl.toString());
			if (isJar(testUrl)) {
				return testUrl;
			} else {
				// WebLogic fix: check if the URL's file exists in the filesystem.
				log.debug("Not a JAR: " + jarUrl);
				jarUrl.replace(0, jarUrl.length(), testUrl.getFile());
				File file = new File(jarUrl.toString());

				// File name might be URL-encoded
//				if (!file.exists()) {
//					file = new File(StringUtil.urlDecode(jarUrl.toString()));
//				}

				if (file.exists()) {
					log.debug("Trying real file: " + file.getAbsolutePath());
					testUrl = file.toURI().toURL();
					if (isJar(testUrl)) {
						return testUrl;
					}
				}
			}
		}
		catch (MalformedURLException e) {
			log.warn("Invalid JAR URL: " + jarUrl);
		}

		log.debug("Not a JAR: " + jarUrl);
		return null;
	}

	/**
	 * Converts a Java package name to a path that can be looked up with a call to
	 * {@link ClassLoader#getResources(String)}.
	 *
	 * @param packageName The Java package name to convert to a path
	 */
	protected String getPackagePath(String packageName) {
		return packageName == null ? null : packageName.replace('.', '/');
	}

	/**
	 * Returns true if the name of a resource (file or directory) is one that matters in the search
	 * for classes. Relevant resources would be class files themselves (file names that end with
	 * ".class") and directories that might be a Java package name segment (java identifiers).
	 *
	 * @param resourceName The resource name, without path information
	 */
	protected boolean isRelevantResource(String resourceName) {
		return resourceName != null
				&& (resourceName.endsWith(".class") || JAVA_IDENTIFIER_PATTERN
				.matcher(resourceName).matches());
	}

	/**
	 * Returns true if the resource located at the given URL is a JAR file.
	 *
	 * @param url The URL of the resource to test.
	 */
	protected boolean isJar(URL url) {
		return isJar(url, new byte[JAR_MAGIC.length]);
	}

	/**
	 * Returns true if the resource located at the given URL is a JAR file.
	 *
	 * @param url	The URL of the resource to test.
	 * @param buffer A buffer into which the first few bytes of the resource are read. The buffer
	 *               must be at least the size of {@link #JAR_MAGIC}. (The same buffer may be reused
	 *               for multiple calls as an optimization.)
	 */
	protected boolean isJar(URL url, byte[] buffer) {
		InputStream is = null;
		try {
			is = url.openStream();
			is.read(buffer, 0, JAR_MAGIC.length);
			if (Arrays.equals(buffer, JAR_MAGIC)) {
				log.debug("Found JAR: " + url);
				return true;
			}
		}
		catch (Exception e) {
			// Failure to read the stream means this is not a JAR
		}
		finally {
			try {
				is.close();
			}
			catch (Exception e) {
			}
		}

		return false;
	}

	/**
	 * Add the class designated by the fully qualified class name provided to the set of
	 * resolved classes if and only if it is approved by the Test supplied.
	 *
	 * @param test the test used to determine if the class matches
	 * @param fqn  the fully qualified name of a class
	 */
	@SuppressWarnings("unchecked")
	protected void addIfMatching(Test test, String fqn) {
		try {
			String externalName = fqn.substring(0, fqn.indexOf('.')).replace('/', '.');
			ClassLoader loader = getClassLoader();
			log.debug("Checking to see if class " + externalName + " matches criteria [" + test + "]");

			Class type = loader.loadClass(externalName);
			if (test.matches(type)) {
				matches.add((Class<T>) type);
			}
		}
		catch (Throwable t) {
			log.warn("Could not examine class '" + fqn + "'" + " due to a " +
					t.getClass().getName() + " with message: " + t.getMessage());
		}
	}
}
