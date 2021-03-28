/*
 * Copyright (c) 2017 Mika Hämäläinen
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

package com.com.example.application.broadcaster;

import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.shared.Registration;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 */
@Log4j2
public class Broadcaster {

    /**
     * We are having process level reference to all listeners which are
     * UI objects in this case. We need to make sure that each UI is
     * removed from this list.
     *
     * Each browser tab and window is an UI object. We can update everyone
     * who is connected to the server by iterating through UI objects.
     */
    private static final List<SerializableConsumer<List<String>>> listeners = new CopyOnWriteArrayList<>();
    private static final List<SerializableConsumer<String>> messages = new CopyOnWriteArrayList<>();

    /**
     * {@see https://vaadin.com/docs/-/part/framework/advanced/advanced-push.html}
     */
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static void broadcast(List<String> updatedTransactionIds) {
        listeners.forEach(listener -> {
            executorService.execute(() -> listener.accept(updatedTransactionIds));
        });
        log.debug("Notified {} broadcast listeners", listeners.size());
    }

    public static Registration register(final SerializableConsumer<List<String>> listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    public static void broadcastMessage(final String message) {
        messages.forEach(msg -> {
            executorService.execute(() -> msg.accept(message));
        });
        log.debug("Notified {} UI", listeners.size());
    }

    public static Registration registerMessage(final SerializableConsumer<String> message) {
        messages.add(message);
        return () -> messages.remove(message);
    }

}
