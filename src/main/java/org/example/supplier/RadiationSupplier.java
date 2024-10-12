package org.example.supplier;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortInvalidPortException;
import org.example.annotations.Item;
import org.example.annotations.Parameter;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Item(name = "GeigerConnector")
public class RadiationSupplier {
    private SerialPort _comPort;
    private final AtomicBoolean _running = new AtomicBoolean(false);
    private final AtomicBoolean _startEmitting = new AtomicBoolean(false);
    private final Queue<Short> _queue = new ConcurrentLinkedQueue<>();

    @Parameter(name = "model", description = "Counter model")
    private String _model = "GCM-500";

    @Parameter(name = "baudRate", description = "Baud rate of the USB connector")
    public int _baudRate = 115200;

    @Parameter(name = "commPort", description = "Port of the USB connector")
    public String _commPort;

    @Parameter(name = "readByteCount", description = "Number of bytes to read when data is available")
    public int _byteCount = 2;

    @Parameter(name = "maxQueueSize", description = "Maximum queue size before it gets cleared")
    public int _maxQueueSize = 500;

    @Parameter(name = "writeInterval", description = "Write interval, ms")
    public int _writeInterval = 2000;

    @Parameter(name = "readInterval", description = "Read interval, ms")
    public int _readInterval = 0;

    @Parameter(name = "cpmCommand", description = "Command to poll CPM from the Geiger Counter")
    public String _cpmCommand = "<GETCPM>>";

    public boolean initialize() {
        try {
            _comPort = SerialPort.getCommPort(_commPort);
            _comPort.setBaudRate(_baudRate);
            _comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, _readInterval, _writeInterval);
            _comPort.openPort();
            return true;
        } catch (SerialPortInvalidPortException e) {
            System.out.printf("Unable to open COM port %s with %d baud rate %n", _commPort, _baudRate);
            return false;
        }
    }

    public void registerPortDataListener (){
        if (_comPort != null && _comPort.isOpen()) {
            _comPort.addDataListener(new SerialPortDataListener() {
                @Override
                public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_AVAILABLE; }

                @Override
                public void serialEvent(SerialPortEvent event)
                {
                    if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
                        return;
                    byte[] newData = new byte[_comPort.bytesAvailable()];
                    int numRead = _comPort.readBytes(newData, newData.length);
                    if (numRead == 2) {
                        short count = getShortValue(newData);
                        if (_queue.size() > _maxQueueSize) {
                            _queue.clear();
                        }
                        if (_startEmitting.get()) {
                            _queue.add(count);
                        }
                    System.out.printf("Count is %s CPM Queue size after add is %d %n", count, _queue.size());
                    } else {
                        System.out.printf("Unexpected number of bytes read: %d instead of 2 bytes %n", numRead);
                    }
                }
            });
        }
    }


    public void startDataAcquire() {
        _running.set(true);
        while (_running.get()) {
            try {
                _comPort.writeBytes(_cpmCommand.getBytes(), _cpmCommand.getBytes().length);
                Thread.sleep(_writeInterval);
            } catch (InterruptedException e) {
                System.out.printf("Interrupted while waiting for data to read %n, breaking..");
                break;
            }
        }
    }

    private short getShortValue(byte[] twoBytes)
    {
        return (short)((twoBytes[0] << 8) | (twoBytes[1] & 0xFF));
    }

    public Queue<Short> getQueue() {
        return _queue;
    }

    public void startDataEmit() {
        _startEmitting.set(true);
    }

    public void stopDataEmit() {
        _startEmitting.set(false);
    }

}
