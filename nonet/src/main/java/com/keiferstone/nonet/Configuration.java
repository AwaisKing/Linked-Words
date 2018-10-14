package com.keiferstone.nonet;

import android.util.Patterns;

@SuppressWarnings( "WeakerAccess" )
public final class Configuration {
    static final String DEFAULT_ENDPOINT = "http://gstatic.com/generate_204";
    static final int DEFAULT_TIMEOUT = 10;
    static final int DEFAULT_CONNECTED_POLL_FREQUENCY = 60;
    static final int DEFAULT_DISCONNECTED_POLL_FREQUENCY = 1;

    static final int NEVER = Integer.MAX_VALUE;

    private String endpoint;
    private int timeout;
    private int connectedPollFrequency;
    private int disconnectedPollFrequency;

    Configuration() {
        endpoint = DEFAULT_ENDPOINT;
        timeout = DEFAULT_TIMEOUT;
        connectedPollFrequency = DEFAULT_CONNECTED_POLL_FREQUENCY;
        disconnectedPollFrequency = DEFAULT_DISCONNECTED_POLL_FREQUENCY;
    }

    String getEndpoint() {
        return endpoint;
    }

    private void setEndpoint(String endpoint) {
        if (endpoint == null || !Patterns.WEB_URL.matcher(endpoint).matches()) {
            throw new IllegalArgumentException("Endpoint must be a valid URL. Supplied URL: " + endpoint);
        } else {
            this.endpoint = endpoint;
        }
    }

    int getTimeout() {
        return timeout;
    }

    private void setTimeout(int timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("Timeout must be non-negative. Supplied timeout: " + timeout);
        } else {
            this.timeout = timeout;
        }
    }

    int getConnectedPollFrequency() {
        return connectedPollFrequency;
    }

    private void setConnectedPollFrequency(int pollFrequency) {
        if (pollFrequency <= 0) {
            throw new IllegalArgumentException("Poll frequency must be positive. Supplied poll frequency: " + pollFrequency);
        } else {
            this.connectedPollFrequency = pollFrequency;
        }
    }

    int getDisconnectedPollFrequency() {
        return disconnectedPollFrequency;
    }

    private void setDisconnectedPollFrequency(int pollFrequency) {
        if (pollFrequency <= 0) {
            throw new IllegalArgumentException("Poll frequency must be positive. Supplied poll frequency: " + pollFrequency);
        } else {
            this.disconnectedPollFrequency = pollFrequency;
        }
    }

    @SuppressWarnings( "unused" )
    public static class Builder {
        final Configuration configuration;

        public Builder() {
            configuration = new Configuration();
        }

        /**
         * Set the endpoint to check connectivity against. Must be a valid URL.
         *
         * @param endpoint The endpoint to poll.
         * @return This {@link Configuration.Builder}.
         * @throws IllegalArgumentException If endpoint is invalid.
         */
        public Builder endpoint(String endpoint) {
            configuration.setEndpoint(endpoint);
            return this;
        }

        /**
         * Set the timeout when checking connectivity. Must be non-negative.
         *
         * @param timeout The timeout for connectivity polls.
         * @return This {@link Configuration.Builder}.
         * @throws IllegalArgumentException If timeout is negative.
         */
        public Builder timeout(int timeout) {
            configuration.setTimeout(timeout);
            return this;
        }

        /**
         * Set the poll frequency when checking connectivity while connected. Must be positive.
         * Set to Configuration.NEVER to disable polling while connected.
         *
         * @param pollFrequency The frequency to poll for connectivity.
         * @return This {@link Configuration.Builder}.
         * @throws IllegalArgumentException If pollFrequency is non-positive.
         */
        public Builder connectedPollFrequency(int pollFrequency) {
            configuration.setConnectedPollFrequency(pollFrequency);
            return this;
        }

        /**
         * Set the poll frequency when checking connectivity while disconnected. Must be positive.
         * Set to {@code Configuration.NEVER} to disable polling while connected.
         *
         * @param pollFrequency The frequency to poll for connectivity.
         * @return This {@link Configuration.Builder}.
         * @throws IllegalArgumentException If pollFrequency is non-positive.
         */
        public Builder disconnectedPollFrequency(int pollFrequency) {
            configuration.setDisconnectedPollFrequency(pollFrequency);
            return this;
        }

        /**
         * Build the configuration.
         *
         * @return The {@link Configuration}.
         */
        public Configuration build() {
            return configuration;
        }
    }
}
