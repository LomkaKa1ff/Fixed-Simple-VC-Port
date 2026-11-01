package dev.fixedvoiceport;

/** Implemented on the SVC server by our mixin. */
public interface VoicePortAccess {
    void fixedVoicePort$selectPort(int port) throws Exception;
}
