/**
 * Copyright (C) 2011 - 2014, MyWarp team and contributors
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
package me.taylorkelly.mywarp.utils.commands;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.annotation.Nullable;

import me.taylorkelly.mywarp.MyWarp;

/**
 * An injector creates instances of classes and handles reflection errors.
 */
public class Injector {
    private Object[] args;
    private Class<?>[] argClasses;

    /**
     * Initializes this injector with the given arguments, that will be used to
     * Initialize classes.
     * 
     * @param args
     *            the arguments
     */
    public Injector(Object... args) {
        this.args = args;
        argClasses = new Class[args.length];
        for (int i = 0; i < args.length; ++i) {
            argClasses[i] = args[i].getClass();
        }
    }

    /**
     * Creates an instance of the given class.
     * 
     * @param <T>
     *            the type of the instance
     * @param clazz
     *            the class
     * @return an instance of the class or <code>null</code> if an exception
     *         happens
     */
    @Nullable
    public <T> T getInstance(Class<T> clazz) {
        try {
            Constructor<T> ctr = clazz.getConstructor(argClasses);
            ctr.setAccessible(true);
            return ctr.newInstance(args);
        } catch (NoSuchMethodException e) {
            MyWarp.logger().severe("Error initializing commands class " + clazz + ": ");
            e.printStackTrace();
            return null;
        } catch (InvocationTargetException e) {
            MyWarp.logger().severe("Error initializing commands class " + clazz + ": ");
            e.printStackTrace();
            return null;
        } catch (InstantiationException e) {
            MyWarp.logger().severe("Error initializing commands class " + clazz + ": ");
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e) {
            MyWarp.logger().severe("Error initializing commands class " + clazz + ": ");
            e.printStackTrace();
            return null;
        }
    }
}
