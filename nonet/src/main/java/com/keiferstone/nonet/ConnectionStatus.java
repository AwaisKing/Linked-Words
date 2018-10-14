package com.keiferstone.nonet;


import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.keiferstone.nonet.ConnectionStatus.CONNECTED;
import static com.keiferstone.nonet.ConnectionStatus.DISCONNECTED;
import static com.keiferstone.nonet.ConnectionStatus.UNKNOWN;

@Retention( RetentionPolicy.SOURCE )
@IntDef( {CONNECTED, DISCONNECTED, UNKNOWN} )
public @interface ConnectionStatus {
    int CONNECTED = 100;
    int DISCONNECTED = 101;
    int UNKNOWN = 102;
}
