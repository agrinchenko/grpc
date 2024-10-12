package org.example.client.consumers;

import org.example.annotations.Item;
import org.example.annotations.Parameter;

@Item(name = "StdoutPrinter")
public class StdoutPrinter implements DataDirector
{
    @Parameter(name = "printMessage", description = "Captures the print message to be used in the printouts")
    public String _printMessage = "Received message [%s] %n";

    @Override
    public void direct(int message) {
        System.out.printf(_printMessage, message);
    }

    @Override
    public boolean initialize() {
        return true;
    }
}
