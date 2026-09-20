package org.apache.log4j.helpers;

import java.util.Vector;

public class AppenderAttachableImpl {

    public static class Appender {
        public void close() {}
    }

    protected Vector appenderList = new Vector();

    public void addAppender(Appender newAppender) {
        if (!appenderList.contains(newAppender)) {
            appenderList.addElement(newAppender);
        }
    }

    public void removeAllAppenders() {
        if (appenderList != null) {
            int len = appenderList.size();
            for (int i = 0; i < len; i++) {
                // Read operation on shared Vector
                Appender a = (Appender) appenderList.elementAt(i);
                if (a != null) {
                    a.close();
                }
            }
            // Write operation on shared Vector
            appenderList.removeAllElements();
        }
    }
}
