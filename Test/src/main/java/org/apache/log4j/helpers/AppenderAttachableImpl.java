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
                Appender appender = (Appender) appenderList.elementAt(i);
                if (appender != null) {
                    appender.close();
                }
            }
            appenderList.removeAllElements();
        }
    }
}
