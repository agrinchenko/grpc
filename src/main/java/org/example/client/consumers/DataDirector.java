package org.example.client.consumers;

public interface DataDirector {
    void direct(int message);
    boolean initialize();
}
