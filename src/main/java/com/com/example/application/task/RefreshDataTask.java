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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT", "IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.com.example.application.task;

import com.com.example.application.broadcaster.Broadcaster;
import com.com.example.application.data.Transaction;
import com.com.example.application.data.TransactionGenerator;
import com.com.example.application.data.TransactionRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

@Log4j2
@Service
public class RefreshDataTask {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> scheduledFuture;

    public void initUpdateGrid() {
        log.debug("Background task called");
        this.scheduledFuture = scheduler.scheduleAtFixedRate(() -> {
            try {
                Broadcaster.broadcast(updatedTransactionIds());
            } catch (RuntimeException e) {
                log.error("Error en runnable Refresh data task");
            }
        }, 0,2, TimeUnit.SECONDS);

    }

    public void stopUpdateGrid() {
        if(Objects.nonNull(scheduledFuture)) {
            log.info("Stoped update grid");
            scheduledFuture.cancel(true);
        }
    }

    public List<String> updatedTransactionIds() {
        return updateRandomTransactions();
    }

    private List<String> updateRandomTransactions() {
        final List<String> updatedTransactions = new CopyOnWriteArrayList<>();
        for (Transaction transaction : TransactionRepository.getInstance().findAll()) {
            if (Math.random() < 0.3) {
                continue;
            }

            BigDecimal newAmount = transaction.getAmount()
                    .add(BigDecimal.valueOf(Math.random() * 100))
                    .setScale(0, RoundingMode.FLOOR);

            transaction.setAmount(newAmount);
            transaction.setUpdated(Instant.now());
            updatedTransactions.add(transaction.getName());
            TransactionRepository.getInstance().update(transaction);
        }
        if (Math.random() < 0.2 && TransactionGenerator.hasMore()) {
            Transaction transaction = TransactionGenerator.create();
            if (TransactionRepository.getInstance().find(transaction.getName()) == null) {
                TransactionRepository.getInstance().insert(transaction);
            }
            updatedTransactions.add(transaction.getName());
        }
        return updatedTransactions;
    }

}
