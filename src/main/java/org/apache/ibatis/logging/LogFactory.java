/*
 *    Copyright 2009-2013 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.logging;

import java.lang.reflect.Constructor;

import org.apache.ibatis.logging.commons.JakartaCommonsLoggingImpl;
import org.apache.ibatis.logging.log4j.Log4jImpl;
import org.apache.ibatis.logging.nologging.NoLoggingImpl;
import org.apache.ibatis.logging.slf4j.Slf4jImpl;
import org.apache.ibatis.logging.stdout.StdOutImpl;

public final class LogFactory {

	/**
	 * Marker to be used by logging implementations that support
	 */
	public static final String MARKER = "MYBATIS";

	private static Constructor<? extends Log> logConstructor;

	/**
	 * 按顺序尝试加载各种日志实现。找到一种可以使用的日志系统
	 */
	static {
		tryImplementation(new Runnable() {
			public void run() {
				useSlf4jLogging();
			}
		});
		tryImplementation(new Runnable() {
			public void run() {
				useCommonsLogging();
			}
		});
		tryImplementation(new Runnable() {
			public void run() {
				useLog4J2Logging();
			}
		});
		tryImplementation(new Runnable() {
			public void run() {
				useLog4JLogging();
			}
		});
		tryImplementation(new Runnable() {
			public void run() {
				useJdkLogging();
			}
		});
		tryImplementation(new Runnable() {
			public void run() {
				useNoLogging();
			}
		});
	}

	private LogFactory() {
		// disable construction
	}

	public static Log getLog(Class<?> aClass) {
		return getLog(aClass.getName());
	}

	public static Log getLog(String logger) {
		try {
			return logConstructor.newInstance(new Object[] { logger });
		} catch (Throwable t) {
			throw new LogException("Error creating logger for logger " + logger + ".  Cause: " + t, t);
		}
	}

	public static synchronized void useCustomLogging(Class<? extends Log> clazz) {
		setImplementation(clazz);
	}

	public static synchronized void useSlf4jLogging() {
		setImplementation(org.apache.ibatis.logging.slf4j.Slf4jImpl.class);
	}

	public static synchronized void useCommonsLogging() {
		setImplementation(org.apache.ibatis.logging.commons.JakartaCommonsLoggingImpl.class);
	}

	public static synchronized void useLog4JLogging() {
		setImplementation(org.apache.ibatis.logging.log4j.Log4jImpl.class);
	}

	public static synchronized void useLog4J2Logging() {
		setImplementation(org.apache.ibatis.logging.log4j2.Log4j2Impl.class);
	}

	public static synchronized void useJdkLogging() {
		setImplementation(org.apache.ibatis.logging.jdk14.Jdk14LoggingImpl.class);
	}

	public static synchronized void useStdOutLogging() {
		setImplementation(org.apache.ibatis.logging.stdout.StdOutImpl.class);
	}

	public static synchronized void useNoLogging() {
		setImplementation(org.apache.ibatis.logging.nologging.NoLoggingImpl.class);
	}

	/**
	 * 尝试Log的Implementation实现是否可行。主要作用：1.如果日志实现已经找到，则不再继续尝试。 2.把
	 * {@link #setImplementation(Class)}的异常捕捉
	 * @param runnable
	 */
	// TODO 为什么用Runnable.run()实现？把传入的方法设为synchronized貌似也没用？
	private static void tryImplementation(Runnable runnable) {
		if (logConstructor == null) {
			try {
				runnable.run();
			} catch (Throwable t) {
				// ignore
			}
		}
	}

	/**
	 * 设定Log的实现
	 * 
	 * @param implClass
	 *            Log实现类 ,例如{@link Slf4jImpl} {@link Log4jImpl}
	 *            {@link StdOutImpl} {@link NoLoggingImpl}
	 *            {@link JakartaCommonsLoggingImpl}
	 */
	private static void setImplementation(Class<? extends Log> implClass) {
		try {
			// 反射得到参数为String的构造方法,创建实体。如果成功说明有相应Log的组件
			Constructor<? extends Log> candidate = implClass.getConstructor(new Class[] { String.class });
			Log log = candidate.newInstance(new Object[] { LogFactory.class.getName() });
			log.debug("Logging initialized using '" + implClass + "' adapter.");
			logConstructor = candidate;
		} catch (Throwable t) {
			throw new LogException("Error setting Log implementation.  Cause: " + t, t);
		}
	}

}
