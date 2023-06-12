package de.rafael.bibliothek.throwables;

import java.io.Serial;

/**
 * @author Rafael K.
 * @since 19:52, 12.06.23
 */

public class DownloadFailed extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 4789397072200030700L;

    public DownloadFailed(Throwable cause) {
        super(cause);
    }

}
