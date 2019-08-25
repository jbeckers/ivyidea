/*
 * Copyright 2010 Guy Mahieu
 * Copyright 2011 Maarten Coene
 * Copyright 2019 Joachim Beckers
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

package clarent.untitledplugintest.core;

import org.apache.commons.collections.SortedBag;

import java.util.Comparator;
import java.util.Set;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: guy
 * Date: 28-apr-2008
 * Time: 18:09:00
 * To change this template use File | Settings | File Templates.
 */
public class Test {

    public static void main(String[] args) {
        new SortedBag() {
            public Comparator comparator() {
                return null;
            }

            public Object first() {
                return null;
            }

            public Object last() {
                return null;
            }

            public int getCount(Object o) {
                return 0;
            }

            public boolean add(Object o) {
                return false;
            }

            public boolean add(Object o, int i) {
                return false;
            }

            public boolean remove(Object o) {
                return false;
            }

            public boolean remove(Object o, int i) {
                return false;
            }

            public Set uniqueSet() {
                return null;
            }

            public int size() {
                return 0;
            }

            public boolean containsAll(Collection collection) {
                return false;
            }

            public boolean addAll(Collection c) {
                return false;
            }

            public boolean removeAll(Collection collection) {
                return false;
            }

            public boolean retainAll(Collection collection) {
                return false;
            }

            public void clear() {
            }

            public Iterator iterator() {
                return new Iterator() {
                    @Override
                    public boolean hasNext() {
                        return false;
                    }

                    @Override
                    public Object next() {
                        return null;
                    }
                };
            }

            public boolean isEmpty() {
                return false;
            }

            public boolean contains(Object o) {
                return false;
            }

            public Object[] toArray() {
                return new Object[0];
            }

            public Object[] toArray(Object[] a) {
                return new Object[0];
            }
        };
    }

}
