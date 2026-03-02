package com.example.c_advanced_core.b_iterable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Реализовать структуру BrowserHistory, которая хранит посещённые страницы и даёт итератор.

 * Требования:

 * Хранение на базе List<String>.
 * Кастомный Iterator<String>:
 * поддерживает next, hasNext, remove.
 * remove должен удалять текущий элемент корректно и без ConcurrentModificationException.
 * Добавить 2 режима обхода:
 * от старых к новым,
 * от новых к старым.
 */

public class FBrowserHistory implements Iterable<String> {
    private final List<String> pages = new ArrayList<>();

    private final Mode mode;

    public FBrowserHistory(Mode mode) {
        this.mode = mode;
    }

    @Override
    public Iterator<String> iterator() {
        return new Iterator<>() {
            private int currentIndex =
                    mode == Mode.FROM_OLD_TO_NEW
                            ? 0
                            : pages.size() - 1;

            @Override
            public boolean hasNext() {
                return mode == Mode.FROM_OLD_TO_NEW
                        ? currentIndex <= pages.size() -1
                        : currentIndex >= 0;
            }

            @Override
            public String next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                int value = currentIndex;
                if (mode == Mode.FROM_OLD_TO_NEW) {
                    currentIndex++;
                } else {
                    currentIndex--;
                }
                return pages.get(value);
            }

            @Override
            public void remove() {
                if (mode == Mode.FROM_OLD_TO_NEW) {
                    pages.remove(currentIndex - 1);
                    currentIndex--;
                } else {
                    pages.remove(currentIndex + 1);
                }
            }
        };
    }

    public static void main(String[] args) {
        forEachTest(Mode.FROM_OLD_TO_NEW);
        forEachTest(Mode.FROM_NEW_TO_OLD);
        removeTest("www.aaa.com", Mode.FROM_OLD_TO_NEW);
        removeTest("www.bbb.com", Mode.FROM_NEW_TO_OLD);
        removeTest("www.google.com", Mode.FROM_OLD_TO_NEW);
    }

    private static void removeTest(String pageToRemove, Mode mode) {
        System.out.println("Remove test");
        FBrowserHistory history = new FBrowserHistory(mode);
        history.addPage("www.aaa.com");
        history.addPage("www.bbb.com");
        history.addPage("www.ccc.com");
        Iterator<String> iterator = history.iterator();
        while (iterator.hasNext()) {
            String page = iterator.next();
            if (page.equals(pageToRemove)) {
                iterator.remove();
            } else {
                System.out.println(page);
            }
        }
        System.out.println("End of test");
    }

    private static void forEachTest(Mode mode) {
        System.out.println("Mode: " + mode);
        FBrowserHistory history = new FBrowserHistory(mode);
        history.addPage("www.aaa.com");
        history.addPage("www.bbb.com");
        history.addPage("www.ccc.com");
        history.print(history);
        System.out.println("End of test");
    }

    private void print(FBrowserHistory history) {
        for (String page : history) {
            System.out.println(page);
        }
    }

    private void addPage(String page) {
        pages.add(page);
    }
}
