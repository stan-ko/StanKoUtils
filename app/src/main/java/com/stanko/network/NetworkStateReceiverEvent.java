package com.stanko.network;

/**
 * by Devlight
 *
 * Authors:
 * Stan Koshutsky <Stan.Koshutsky@gmail.com>
 * Class to be used with EventBus
 */
public class NetworkStateReceiverEvent {

    public final boolean wasNetworkAvailable;
    public final boolean isNetworkAvailable;
    public final boolean doesHostRespond;
    public final NetworkState lastNetworkState;
    public final NetworkState newNetworkState;
    public final String lastNetworkID;
    public final String newNetworkID;


    public NetworkStateReceiverEvent(final boolean wasNetworkAvailable,
                                     final boolean isNetworkAvailable,
                                     final NetworkState lastNetworkState,
                                     final NetworkState newNetworkState,
                                     final String lastNetworkID,
                                     final String newNetworkID) {
        this.wasNetworkAvailable = wasNetworkAvailable;
        this.isNetworkAvailable = isNetworkAvailable;
        this.doesHostRespond = false;
        this.lastNetworkState = lastNetworkState;
        this.newNetworkState = newNetworkState;
        this.lastNetworkID = lastNetworkID;
        this.newNetworkID = newNetworkID;
    }

    public NetworkStateReceiverEvent(final boolean wasNetworkAvailable,
                                     final boolean isNetworkAvailable,
                                     final boolean doesHostRespond,
                                     final NetworkState lastNetworkState,
                                     final NetworkState newNetworkState,
                                     final String lastNetworkID,
                                     final String newNetworkID) {
        this.wasNetworkAvailable = wasNetworkAvailable;
        this.isNetworkAvailable = isNetworkAvailable;
        this.doesHostRespond = doesHostRespond;
        this.lastNetworkState = lastNetworkState;
        this.newNetworkState = newNetworkState;
        this.lastNetworkID = lastNetworkID;
        this.newNetworkID = newNetworkID;
    }

    public boolean isNetworkStateChanged() {
        return wasNetworkAvailable != isNetworkAvailable || lastNetworkID == null && newNetworkID != null || lastNetworkID != null && !lastNetworkID.equals(newNetworkID);
    }


}
