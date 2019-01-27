/* Copyright (c) 2011 Danish Maritime Authority.
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
package dk.dma.epd.common.prototype.service;

import com.bbn.openmap.MapHandlerChild;
import dk.dma.enav.model.geometry.Position;
import dk.dma.epd.common.prototype.EPD;
import dk.dma.epd.common.prototype.status.CloudStatus;
import dk.dma.epd.common.prototype.status.IStatusComponent;
import dk.dma.epd.common.util.Util;
import net.maritimecloud.core.id.MaritimeId;
import net.maritimecloud.net.mms.MmsClient;
import net.maritimecloud.net.mms.MmsClientConfiguration;
import net.maritimecloud.net.mms.MmsConnection;
import net.maritimecloud.net.mms.MmsConnectionClosingCode;
import net.maritimecloud.util.geometry.PositionReader;
import net.maritimecloud.util.geometry.PositionTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Service that provides an interface to the Maritime Cloud connection.
 * <p>
 * For technical reasons, each application should only have one live connection to the maritime cloud.<br/>
 * The purpose of the {@code MaritimeCloudService} is to be the only access point to this service.
 * <p>
 * Clients of this service should hook up a listeners to be notified when the service is running or stopped.
 * <p>
 * Future improvements:
 * <ul>
 * <li>Perform listener tasks in a thread pool</li>
 * </ul>
 */
public class MaritimeCloudService extends MapHandlerChild implements Runnable, IStatusComponent {

    /**
     * Set this flag to true, if you want to log all messages sent and received by the {@linkplain MmsClient}
     */
    private static final boolean LOG_MARITIME_CLOUD_ACTIVITY = false;
    private static final int MARITIME_CLOUD_SLEEP_TIME = 10000;

    private static final Logger LOG = LoggerFactory.getLogger(MaritimeCloudService.class);

    protected MmsClient connection;

    protected List<IMaritimeCloudListener> listeners = new CopyOnWriteArrayList<>();
    protected CloudStatus cloudStatus = new CloudStatus();
    protected String hostPort;
    protected boolean stopped = true;

    /**
     * Constructor
     */
    public MaritimeCloudService() {
    }

    /**
     * Reads the e-Navigation settings for connection parameters
     */
    protected void readEnavSettings() {
        this.hostPort = String.format("%s:%d", EPD.getInstance().getSettings().getCloudSettings().getCloudServerHost(), EPD
                .getInstance().getSettings().getCloudSettings().getCloudServerPort());
    }

    /**
     * Returns a reference to the cloud client connection
     * 
     * @return a reference to the cloud client connection
     */
    public MmsClient getConnection() {
        return connection;
    }

    /**
     * Returns the cloud status
     */
    @Override
    public CloudStatus getStatus() {
        return cloudStatus;
    }

    /*********************************/
    /** Life cycle functionality **/
    /*********************************/

    /**
     * Starts the Maritime cloud client
     */
    public void start() {
        if (!stopped) {
            return;
        }
        // Update the eNav settings
        readEnavSettings();
        stopped = false;
        new Thread(this).start();
    }

    /**
     * Stops the Maritime cloud client
     */
    public synchronized void stop() {
        if (stopped) {
            return;
        }

        this.stopped = true;
        if (connection != null) {
            try {
                connection.close();
                connection.awaitTermination(2, TimeUnit.SECONDS);
            } catch (Exception e) {
                LOG.error("Error terminating cloud connection");
            }
            connection = null;
        }
    }

    /**
     * Returns if there is a live connection to the Maritime Cloud
     * 
     * @return if there is a live connection to the Maritime Cloud
     */
    public synchronized boolean isConnected() {
        // Consider using the isClosed()/isConnected methods of the connection
        return !stopped && connection != null;
    }

    /**
     * Thread run method
     */
    @Override
    public void run() {

        // Start by connecting
        while (!stopped) {
            Util.sleep(MARITIME_CLOUD_SLEEP_TIME);
            
            MaritimeId id = EPD.getInstance().getMaritimeId();
            if (id != null || !(MaritimeCloudUtils.toMmsi(id)==0)) {
                if (initConnection(hostPort, id)) {
                    try {
                        fireConnected(connection);
                    } catch (Exception e) {
                        fireError(e.getMessage());
                    }
                    break;
                }
            }
        }

        // Periodic tasks
        while (!stopped) {
            Util.sleep(MARITIME_CLOUD_SLEEP_TIME);
        }

        // Flag that we are stopped
        fireDisconnected();
    }

    /**
     * Create the Maritime Cloud connection
     */
    private boolean initConnection(String host, MaritimeId id) {
        LOG.info("Connecting to cloud server: " + host + " with maritime id " + id);

        MmsClientConfiguration enavCloudConnection = MmsClientConfiguration.create(id);
        enavCloudConnection.properties().setName(EPD.getInstance().getClass().getSimpleName() + " - mmsi: " + EPD.getInstance().getMmsi());


        // Hook up a position reader
        enavCloudConnection.setPositionReader(new PositionReader() {
            @Override
            public PositionTime getCurrentPosition() {
                long now = System.currentTimeMillis();
                Position pos = EPD.getInstance().getPosition();
                if (pos != null) {
                    return PositionTime.create(pos.getLatitude(), pos.getLongitude(), now);
                } else {
                    return PositionTime.create(0.0, 0.0, System.currentTimeMillis());
                }
            }
        });

        // Check if we need to log the MaritimeCloudConnection activity
        enavCloudConnection.addListener(new MmsConnection.Listener() {
            @Override
            public void connecting(URI host) {
                if (LOG_MARITIME_CLOUD_ACTIVITY) {
                    LOG.info("Connecting to " + host);
                }
            }

            public void connected(URI host) {
                cloudStatus.markCloudReception();
                if (LOG_MARITIME_CLOUD_ACTIVITY) {
                    LOG.info("Connected to " + host);
                }
            }

            @Override
            public void binaryMessageReceived(byte[] message) {
                cloudStatus.markCloudReception();
                if (LOG_MARITIME_CLOUD_ACTIVITY) {
                    LOG.info("Received binary message: " + (message == null ? 0 : message.length) + " bytes");
                }
            }

            @Override
            public void binaryMessageSend(byte[] message) {
                cloudStatus.markSuccesfullSend();
                if (LOG_MARITIME_CLOUD_ACTIVITY) {
                    LOG.info("Sending binary message: " + (message == null ? 0 : message.length) + " bytes");
                }
            }

            @Override
            public void textMessageReceived(String message) {
                cloudStatus.markCloudReception();
                if (LOG_MARITIME_CLOUD_ACTIVITY) {
                    LOG.info("Received text message: " + message);
                }
            }

            @Override
            public void textMessageSend(String message) {
                cloudStatus.markSuccesfullSend();
                if (LOG_MARITIME_CLOUD_ACTIVITY) {
                    LOG.info("Sending text message: " + message);
                }
            }

            @Override
            public void disconnected(MmsConnectionClosingCode closeReason) {
                cloudStatus.markFailedReceive();
                cloudStatus.markFailedSend();
                if (LOG_MARITIME_CLOUD_ACTIVITY) {
                    LOG.info("Disconnected with reason: " + closeReason);
                }
            }
        });

        try {
            enavCloudConnection.setHost(host);
            connection = enavCloudConnection.build();

            if (connection != null) {
//                cloudStatus.markCloudReception();
//                cloudStatus.markSuccesfullSend();
                LOG.info("Connected succesfully to cloud server: " + host + " with shipId " + id);
                return true;
            } else {
                fireError("Failed building a maritime cloud connection");
                return false;
            }
        } catch (Exception e) {
            fireError(e.getMessage());
            cloudStatus.markFailedSend();
            cloudStatus.markFailedReceive();
            LOG.error("Failed to connect to server: " + e);
            return false;
        }
    }

    /*********************************/
    /** Listener functionality **/
    /*********************************/

    /**
     * Adds a listener for cloud connection status changes
     * 
     * @param listener
     *            the listener to add
     */
    public final void addListener(IMaritimeCloudListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener
     * 
     * @param listener
     *            the listener to remove
     */
    public final void removeListener(IMaritimeCloudListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies listeners that a connection has been established to the maritime cloud
     * 
     * @param connection
     *            the connection
     */
    protected void fireConnected(MmsClient connection) {
        for (IMaritimeCloudListener listener : listeners) {
            listener.cloudConnected(connection);
        }
    }

    /**
     * Notifies listeners that a connection has been terminated to the maritime cloud
     */
    protected void fireDisconnected() {
        for (IMaritimeCloudListener listener : listeners) {
            listener.cloudDisconnected();
        }
    }

    /**
     * Notifies listeners that an error has occurred
     * 
     * @param error
     *            the error messsage
     */
    protected void fireError(String error) {
        for (IMaritimeCloudListener listener : listeners) {
            listener.cloudError(error);
        }
    }

    /**
     * Provides a listener interface to the Maritime Cloud connection status.
     * <p>
     * Listeners should provide their own error handling and not throw exceptions in the methods.<br>
     * Neither should not synchronously perform long-lasting tasks.
     */
    public interface IMaritimeCloudListener {

        /**
         * Called when the connection to the maritime cloud has been established.
         * <p>
         * Can be used by listeners to hook up services.
         * 
         * @param connection
         *            the maritime cloud connection
         */
        void cloudConnected(MmsClient connection);

        /**
         * Called when the connection to the maritime cloud has been terminated.
         * <p>
         * Can be used by listeners to clean up.
         */
        void cloudDisconnected();

        /**
         * Called if an error has occurred.
         * 
         * @param error
         *            the error message
         */
        void cloudError(String error);
    }

}
