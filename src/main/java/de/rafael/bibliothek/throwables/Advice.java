/*
 * MIT License
 *
 * Copyright (c) 2023 Rafael
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package de.rafael.bibliothek.throwables;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Rafael K.
 * @since 21:35, 12.06.23
 */

@ControllerAdvice
public class Advice {

    private final ObjectMapper json;

    @Autowired
    public Advice(ObjectMapper json) {
        this.json = json;
    }

    @ExceptionHandler(DownloadFailed.class)
    @ResponseBody
    public ResponseEntity<?> downloadFailed(final DownloadFailed exception) {
        return this.error(HttpStatus.INTERNAL_SERVER_ERROR, "An internal error occurred while serving your download.");
    }

    @ExceptionHandler(DownloadNotFound.class)
    @ResponseBody
    public ResponseEntity<?> downloadNotFound(final DownloadNotFound exception) {
        return this.error(HttpStatus.NOT_FOUND, "Download not found.");
    }

    @ExceptionHandler(ProjectNotFound.class)
    @ResponseBody
    public ResponseEntity<?> projectNotFound(final ProjectNotFound exception) {
        return this.error(HttpStatus.NOT_FOUND, "Project not found.");
    }

    @ExceptionHandler(VersionNotFound.class)
    @ResponseBody
    public ResponseEntity<?> versionNotFound(final VersionNotFound exception) {
        return this.error(HttpStatus.NOT_FOUND, "Version not found.");
    }

    @Contract("_, _ -> new")
    private @NotNull ResponseEntity<?> error(HttpStatus status, String error) {
        return new ResponseEntity<>(
                this.json.createObjectNode().put("error", error),
                status
        );
    }

}
