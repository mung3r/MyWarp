/*
 * Copyright (C) 2011 - 2015, MyWarp team and contributors
 *
 * This file is part of MyWarp.
 *
 * MyWarp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyWarp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyWarp. If not, see <http://www.gnu.org/licenses/>.
 */

package me.taylorkelly.mywarp.bukkit.util.jdbc;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Objects;

import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.sql.DataSource;

/**
 * A {@link DataSource} implementation that uses a single {@link Connection}. {@code Connection}s returned by this the
 * {@code DataSource} are actually wrapper around the original {@code Connection} that forbid closing.
 * <p>Obviously this class is not threadsafe.</p>
 */
public class SingleConnectionDataSource implements DataSource {

  private String url;

  @Nullable
  private String username;

  @Nullable
  private String password;

  @Nullable
  private Connection target;

  @Nullable
  private Connection proxy;

  /**
   * Creates a new instance that connects to a database using the given {@code url}.
   *
   * @param url the database url
   * @see DriverManager#getConnection(String)
   */
  public SingleConnectionDataSource(String url) {
    this.url = url;
  }

  /**
   * Creates a new instance that connects to a database using the given {@code url}, {@code username} and {@code
   * password}.
   *
   * @param url      the database url
   * @param username the database user
   * @param password the password of the databse user
   * @see DriverManager#getConnection(String, String, String)
   */
  public SingleConnectionDataSource(String url, String username, String password) {
    this.url = url;
    this.username = username;
    this.password = password;
  }

  /**
   * Creates a new instance that uses the given {@code Connection}. All {@code Connection}s returned by this instance
   * will be wrapped versions of it.
   *
   * @param target the {@code Connection}
   */
  public SingleConnectionDataSource(Connection target) {
    this.target = target;
    this.proxy = getCloseSuppressingConnectionProxy(target);
  }

  @Override
  public Connection getConnection() throws SQLException {
    if (target == null || proxy == null) {
      initiate();
    }
    if (target.isClosed()) {
      throw new SQLException("The target connection is closed.");
    }

    return proxy;
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    if (Objects.equal(this.username, username) && Objects.equal(this.password, password)) {
      throw new SQLException(
          "SingleConnectionDataSource does not support retrieving of connections with custom username and password.");
    }
    return getConnection();
  }

  @Override
  public int getLoginTimeout() throws SQLException {
    return 0;
  }

  @Override
  public void setLoginTimeout(int timeout) throws SQLException {
    throw new UnsupportedOperationException("setLoginTimeout");
  }

  @Override
  public PrintWriter getLogWriter() {
    throw new UnsupportedOperationException("getLogWriter");
  }

  @Override
  public void setLogWriter(PrintWriter pw) throws SQLException {
    throw new UnsupportedOperationException("setLogWriter");
  }

  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
    //method was added to CommonDataSource in Java7
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T unwrap(Class<T> iface) throws SQLException {
    if (iface.isInstance(this)) {
      return (T) this;
    }
    throw new SQLException(
        "DataSource of type [" + getClass().getName() + "] cannot be unwrapped as [" + iface.getName() + "]");
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return iface.isInstance(this);
  }

  /**
   * Initiates target and proxy {@code Connection}.
   *
   * @throws SQLException          if a database error occurs
   * @throws IllegalStateException if {@code url} is not set
   */
  private void initiate() throws SQLException {
    checkState(url != null, "To lazy initialize the target connection, 'url' must be set.");

    close();

    target = DriverManager.getConnection(url, username, password);
    proxy = getCloseSuppressingConnectionProxy(target);
  }

  /**
   * Closes the target {@code Connection}.
   */
  public void close() {
    if (target != null) {
      try {
        target.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Wrap the given {@code Connection} with a proxy that delegates every method call,
   * but suppresses close calls.
   *
   * @param target the {@code Connection} to wrap
   * @return the wrapped {@code Connection}
   */
  private Connection getCloseSuppressingConnectionProxy(Connection target) {
    return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class<?>[]{Connection.class},
                                               new CloseSuppressingInvocationHandler(target));
  }


  /**
   * Invocation handler that suppresses close calls on JDBC Connections.
   */
  private static class CloseSuppressingInvocationHandler implements InvocationHandler {

    private final Connection target;

    /**
     * Creates an instance that works on the given {@code Connection}.
     *
     * @param target the {@code Connection}
     */
    public CloseSuppressingInvocationHandler(Connection target) {
      this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      // Invocation on ConnectionProxy interface coming in...

      if (method.getName().equals("equals")) {
        // Only consider equal when proxies are identical.
        return (proxy == args[0]);
      } else if (method.getName().equals("hashCode")) {
        // Use hashCode of Connection proxy.
        return System.identityHashCode(proxy);
      } else if (method.getName().equals("unwrap")) {
        if (((Class<?>) args[0]).isInstance(proxy)) {
          return proxy;
        }
      } else if (method.getName().equals("isWrapperFor")) {
        if (((Class<?>) args[0]).isInstance(proxy)) {
          return true;
        }
      } else if (method.getName().equals("close")) {
        // Handle close method: don't pass the call on.
        return null;
      } else if (method.getName().equals("isClosed")) {
        return false;
      }

      // Invoke method on target Connection.
      try {
        return method.invoke(this.target, args);
      } catch (InvocationTargetException ex) {
        throw ex.getTargetException();
      }
    }
  }
}
