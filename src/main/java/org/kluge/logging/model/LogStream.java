package org.kluge.logging.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by giko on 10/13/2014.
 */
public class LogStream {
    private String name;
    private Set<LogNode> logNodes = new HashSet<>();

    public LogStream(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LogStream stream = (LogStream) o;

        if (!name.equals(stream.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public boolean isEmpty() {
        return logNodes.isEmpty();
    }

    public void addNode(LogNode node) {
        logNodes.add(node);
    }

    public void removeNode(LogNode node) {
        logNodes.remove(node);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
