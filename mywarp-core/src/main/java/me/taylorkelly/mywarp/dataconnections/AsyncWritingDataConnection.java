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

package me.taylorkelly.mywarp.dataconnections;

import me.taylorkelly.mywarp.util.profile.Profile;
import me.taylorkelly.mywarp.warp.Warp;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * A {@code DataConnection} that works on top of another {@code DataConnection} and executes every <b>writing</b> task
 * asynchronous within a specified {@link Executor}.
 */
public class AsyncWritingDataConnection extends ForwardingDataConnection {

  private final DataConnection dataConnection;
  private final Executor executor;

  /**
   * Creates an instance. Every call is delegated to the given {@code DataConnection}, writing methods are executed in
   * the given {@code Executor}, reading methods still run in the thread that calls the method.
   *
   * @param dataConnection the {@code DataConnection} whose writing methods should be executed asynchronous
   * @param executor       the {@code Executor} that executes writing methods
   */
  public AsyncWritingDataConnection(DataConnection dataConnection, Executor executor) {
    this.dataConnection = dataConnection;
    this.executor = executor;
  }

  @Override
  protected DataConnection delegate() {
    return dataConnection;
  }

  @Override
  public void addWarp(final Warp warp) {
    executor.execute(new Runnable() {
      @Override
      public void run() {
        delegate().addWarp(warp);
      }
    });
  }

  @Override
  public void removeWarp(final Warp warp) {
    executor.execute(new Runnable() {
      @Override
      public void run() {
        delegate().removeWarp(warp);
      }
    });
  }

  @Override
  public List<Warp> getWarps() {
    return delegate().getWarps();
  }

  @Override
  public void inviteGroup(final Warp warp, final String groupId) {
    executor.execute(new Runnable() {
      @Override
      public void run() {
        delegate().inviteGroup(warp, groupId);
      }
    });
  }

  @Override
  public void invitePlayer(final Warp warp, final Profile playerProfile) {
    executor.execute(new Runnable() {
      @Override
      public void run() {
        delegate().invitePlayer(warp, playerProfile);
      }
    });
  }

  @Override
  public void uninviteGroup(final Warp warp, final String groupId) {
    executor.execute(new Runnable() {
      @Override
      public void run() {
        delegate().uninviteGroup(warp, groupId);
      }
    });
  }

  @Override
  public void uninvitePlayer(final Warp warp, final Profile playerProfile) {
    executor.execute(new Runnable() {
      @Override
      public void run() {
        delegate().uninvitePlayer(warp, playerProfile);
      }
    });
  }

  @Override
  public void updateCreator(final Warp warp) {
    executor.execute(new Runnable() {
      @Override
      public void run() {
        delegate().updateCreator(warp);
      }
    });
  }

  @Override
  public void updateLocation(final Warp warp) {
    executor.execute(new Runnable() {
      @Override
      public void run() {
        delegate().updateLocation(warp);
      }
    });
  }

  @Override
  public void updateType(final Warp warp) {
    executor.execute(new Runnable() {
      @Override
      public void run() {
        delegate().updateType(warp);
      }
    });
  }

  @Override
  public void updateVisits(final Warp warp) {
    executor.execute(new Runnable() {
      @Override
      public void run() {
        delegate().updateVisits(warp);
      }
    });
  }

  @Override
  public void updateWelcomeMessage(final Warp warp) {
    executor.execute(new Runnable() {
      @Override
      public void run() {
        delegate().updateWelcomeMessage(warp);
      }
    });
  }
}
