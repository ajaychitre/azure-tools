package com.dilteam.restServer.coreApp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TweetsPublisher implements StatusListener {
    private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(TweetsPublisher.class);

    private int counter = 0;

    ConnectionStringBuilder connStr;

    final Gson gson = new GsonBuilder().create();

    // The Executor handles all asynchronous tasks and this is passed to the EventHubClient instance.
    // The enables the user to segregate their thread pool based on the work load.
    // This pool can then be shared across multiple EventHubClient instances.
    // The following sample uses a single thread executor, as there is only one EventHubClient instance,
    // handling different flavors of ingestion to Event Hubs here.
    final ExecutorService executorService = Executors.newSingleThreadExecutor();

    // Each EventHubClient instance spins up a new TCP/SSL connection, which is expensive.
    // It is always a best practice to reuse these instances. The following sample shows this.
    private EventHubClient ehClient = null;

    public TweetsPublisher(@Value("${namespace}") String namespace,
                           @Value("${eventHubName}") String eventHubName,
                           @Value("${sasKeyName}") String sasKeyName,
                           @Value("${sasKey}") String sasKey
                           ) throws IOException {
        connStr = new ConnectionStringBuilder()
                .setNamespaceName(namespace)
                .setEventHubName(eventHubName)
                .setSasKeyName(sasKeyName)
                .setSasKey(sasKey);
        try {
            ehClient = EventHubClient.createSync(connStr.toString(), executorService);
        } catch (EventHubException e) {
            e.printStackTrace();
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                LOGGER.info("Inside shutdown hook! Shuttng down Publisher!");
                try {
                    // close the client at the end of your program
                    ehClient.closeSync();
                    executorService.shutdown();
                    LOGGER.info("Publisher was shut down successfully!");
                } catch (Exception e) {
                    LOGGER.info("Error encountered while shutting down: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public void onStatus(Status status) {
        if (status.getLang() == null || !status.getLang().equalsIgnoreCase("en")) {
            return;
        }
        if (++counter % 100 == 0) {
            LOGGER.info("$$$$$$  Total no. of tweets so far: " + counter + " at: "
                    + new Date(System.currentTimeMillis()).toString());
        }

        final byte[] payloadBytes = gson.toJson(status).getBytes(Charset.defaultCharset());
        EventData sendEvent = EventData.create(payloadBytes);

        try {
            ehClient.sendSync(sendEvent);
        } catch (EventHubException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
//        LOGGER.info("onDeletionNotice: " + statusDeletionNotice.toString());
    }

    @Override
    public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
        LOGGER.info("onTrackLimitationNotice: " + numberOfLimitedStatuses);
    }

    @Override
    public void onScrubGeo(long userId, long upToStatusId) {
        LOGGER.info("onScrubGeo: " + userId + "\t" + upToStatusId);
    }

    @Override
    public void onStallWarning(StallWarning warning) {
        LOGGER.info("onStallWarning: " + warning.toString());
    }

    @Override
    public void onException(Exception ex) {
        LOGGER.info("onException: " + ex.getMessage());
    }
}
