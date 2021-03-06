package com.revenco.blesdk.interfaces;

import com.revenco.blesdk.client.DataProviderException;

/**
 * Notifies when server-side beacon data are available from a web service.
 */
public interface BeaconDataNotifier {
    /**
     * This method is called after a request to get or sync beacon data
     * If fetching data was successful, the data is returned and the exception is null.
     * If fetching of the data is not successful, an exception is provided.
     * @param data
     * @param exception
     */
    public void beaconDataUpdate(  BeaconData data, DataProviderException exception);
}
