/*
 * Copyright (c) 2005, 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package chloroplastInterface;

import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.concurrent.*;

import java.awt.event.*;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

import sun.awt.AppContext;
import sun.swing.AccumulativeRunnable;

public abstract class CustomSwingWorker<T, V> implements RunnableFuture<T> {

    private static final int MAX_WORKER_THREADS = 100;
    private volatile int progress;

    private volatile StateValue state;
    private final FutureTask<T> future;
    private final PropertyChangeSupport propertyChangeSupport;
    private AccumulativeRunnable<V> doProcess;
    private AccumulativeRunnable<Integer> doNotifyProgressChange;

    private final AccumulativeRunnable<Runnable> doSubmit = getDoSubmit();

    public enum StateValue {
        /**
         * Initial {@code SwingWorker} state.
         */
        PENDING,
        /**
         * {@code SwingWorker} is {@code STARTED}
         * before invoking {@code doInBackground}.
         */
        STARTED,

        /**
         * {@code SwingWorker} is {@code DONE}
         * after {@code doInBackground} method
         * is finished.
         */
        DONE
    }

    public CustomSwingWorker() {
        Callable<T> callable =
                new Callable<T>() {
            @Override
            public T call() throws Exception {
                setState(StateValue.STARTED);
                return doInBackground();
            }
        };

        future = new FutureTask<T>(callable) {
            @Override
            protected void done() {
                doneEDT();
                setState(StateValue.DONE);
            }
        };

        state = StateValue.PENDING;
        propertyChangeSupport = new SwingWorkerPropertyChangeSupport(this);
        doProcess = null;
        doNotifyProgressChange = null;
    }


    protected abstract T doInBackground() throws Exception ;

    @Override
    public final void run() {
        future.run();
    }

    @SafeVarargs
    protected final void publish(V... chunks) {
        synchronized (this) {
            if (doProcess == null) {
                doProcess = new AccumulativeRunnable<V>() {
                    @Override
                    public void run(List<V> args) {
                        process(args);
                    }
                    @Override
                    protected void submit() {
                        doSubmit.add(this);
                    }
                };
            }
        }
        doProcess.add(chunks);
    }

    protected void process(List<V> chunks) {
    }


    protected void done() {
    }

    protected final void setProgress(int progress) {
        if (progress < 0 || progress > 100) {
            throw new IllegalArgumentException("the value should be from 0 to 100");
        }
        if (this.progress == progress) {
            return;
        }
        int oldProgress = this.progress;
        this.progress = progress;
        if (! getPropertyChangeSupport().hasListeners("progress")) {
            return;
        }
        synchronized (this) {
            if (doNotifyProgressChange == null) {
                doNotifyProgressChange =
                        new AccumulativeRunnable<Integer>() {
                    @Override
                    public void run(List<Integer> args) {
                        firePropertyChange("progress",
                                args.get(0),
                                args.get(args.size() - 1));
                    }
                    @Override
                    protected void submit() {
                        doSubmit.add(this);
                    }
                };
            }
        }
        doNotifyProgressChange.add(oldProgress, progress);
    }


    public final int getProgress() {
        return progress;
    }

    public final void execute() {
        getWorkersExecutorService().execute(this);
    }

    // Future methods START
    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean cancel(boolean mayInterruptIfRunning) {
        return future.cancel(mayInterruptIfRunning);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isCancelled() {
        return future.isCancelled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isDone() {
        return future.isDone();
    }


    @Override
    public final T get() throws InterruptedException, ExecutionException {
        return future.get();
    }


    @Override
    public final T get(long timeout, TimeUnit unit) throws InterruptedException,
    ExecutionException, TimeoutException {
        return future.get(timeout, unit);
    }

    // Future methods END

    // PropertyChangeSupports methods START
    /**
     * Adds a {@code PropertyChangeListener} to the listener list. The listener
     * is registered for all properties. The same listener object may be added
     * more than once, and will be called as many times as it is added. If
     * {@code listener} is {@code null}, no exception is thrown and no action is taken.
     *
     * <p>
     * Note: This is merely a convenience wrapper. All work is delegated to
     * {@code PropertyChangeSupport} from {@link #getPropertyChangeSupport}.
     *
     * @param listener the {@code PropertyChangeListener} to be added
     */
    public final void addPropertyChangeListener(PropertyChangeListener listener) {
        getPropertyChangeSupport().addPropertyChangeListener(listener);
    }

    /**
     * Removes a {@code PropertyChangeListener} from the listener list. This
     * removes a {@code PropertyChangeListener} that was registered for all
     * properties. If {@code listener} was added more than once to the same
     * event source, it will be notified one less time after being removed. If
     * {@code listener} is {@code null}, or was never added, no exception is
     * thrown and no action is taken.
     *
     * <p>
     * Note: This is merely a convenience wrapper. All work is delegated to
     * {@code PropertyChangeSupport} from {@link #getPropertyChangeSupport}.
     *
     * @param listener the {@code PropertyChangeListener} to be removed
     */
    public final void removePropertyChangeListener(PropertyChangeListener listener) {
        getPropertyChangeSupport().removePropertyChangeListener(listener);
    }

    /**
     * Reports a bound property update to any registered listeners. No event is
     * fired if {@code old} and {@code new} are equal and non-null.
     *
     * <p>
     * This {@code SwingWorker} will be the source for
     * any generated events.
     *
     * <p>
     * When called off the <i>Event Dispatch Thread</i>
     * {@code PropertyChangeListeners} are notified asynchronously on
     * the <i>Event Dispatch Thread</i>.
     * <p>
     * Note: This is merely a convenience wrapper. All work is delegated to
     * {@code PropertyChangeSupport} from {@link #getPropertyChangeSupport}.
     *
     *
     * @param propertyName the programmatic name of the property that was
     *        changed
     * @param oldValue the old value of the property
     * @param newValue the new value of the property
     */
    public final void firePropertyChange(String propertyName, Object oldValue,
            Object newValue) {
        getPropertyChangeSupport().firePropertyChange(propertyName,
                oldValue, newValue);
    }

    /**
     * Returns the {@code PropertyChangeSupport} for this {@code SwingWorker}.
     * This method is used when flexible access to bound properties support is
     * needed.
     * <p>
     * This {@code SwingWorker} will be the source for
     * any generated events.
     *
     * <p>
     * Note: The returned {@code PropertyChangeSupport} notifies any
     * {@code PropertyChangeListener}s asynchronously on the <i>Event Dispatch
     * Thread</i> in the event that {@code firePropertyChange} or
     * {@code fireIndexedPropertyChange} are called off the <i>Event Dispatch
     * Thread</i>.
     *
     * @return {@code PropertyChangeSupport} for this {@code SwingWorker}
     */
    public final PropertyChangeSupport getPropertyChangeSupport() {
        return propertyChangeSupport;
    }

    // PropertyChangeSupports methods END

    /**
     * Returns the {@code SwingWorker} state bound property.
     *
     * @return the current state
     */
    public final StateValue getState() {
        /*
         * DONE is a speacial case
         * to keep getState and isDone is sync
         */
        if (isDone()) {
            return StateValue.DONE;
        } else {
            return state;
        }
    }

    /**
     * Sets this {@code SwingWorker} state bound property.
     * @param state the state to set
     */
    private void setState(StateValue state) {
        StateValue old = this.state;
        this.state = state;
        firePropertyChange("state", old, state);
    }

    /**
     * Invokes {@code done} on the EDT.
     */
    private void doneEDT() {
        Runnable doDone =
                new Runnable() {
            @Override
            public void run() {
                done();
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            doDone.run();
        } else {
            doSubmit.add(doDone);
        }
    }


    /**
     * returns workersExecutorService.
     *
     * returns the service stored in the appContext or creates it if
     * necessary.
     *
     * @return ExecutorService for the {@code SwingWorkers}
     */
    private static synchronized ExecutorService getWorkersExecutorService() {
        final AppContext appContext = AppContext.getAppContext();
        ExecutorService executorService =
                (ExecutorService) appContext.get(CustomSwingWorker.class);
        if (executorService == null) {
            //this creates daemon threads.
            ThreadFactory threadFactory =
                    new ThreadFactory() {
                final ThreadFactory defaultFactory =
                        Executors.defaultThreadFactory();
                @Override
                public Thread newThread(final Runnable r) {
                    Thread thread =
                            defaultFactory.newThread(r);
                    thread.setName("SwingWorker-"
                            + thread.getName());
                    thread.setDaemon(true);
                    return thread;
                }
            };

            executorService =
                    new ThreadPoolExecutor(MAX_WORKER_THREADS, MAX_WORKER_THREADS,
                            10L, TimeUnit.MINUTES,
                            new LinkedBlockingQueue<Runnable>(),
                            threadFactory);
            appContext.put(CustomSwingWorker.class, executorService);

            // Don't use ShutdownHook here as it's not enough. We should track
            // AppContext disposal instead of JVM shutdown, see 6799345 for details
            final ExecutorService es = executorService;
            appContext.addPropertyChangeListener(AppContext.DISPOSED_PROPERTY_NAME,
                    new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent pce) {
                    boolean disposed = (Boolean)pce.getNewValue();
                    if (disposed) {
                        final WeakReference<ExecutorService> executorServiceRef =
                                new WeakReference<ExecutorService>(es);
                        final ExecutorService executorService =
                                executorServiceRef.get();
                        if (executorService != null) {
                            AccessController.doPrivileged(
                                    new PrivilegedAction<Void>() {
                                        @Override
                                        public Void run() {
                                            executorService.shutdown();
                                            return null;
                                        }
                                    }
                                    );
                        }
                    }
                }
            }
                    );
        }
        return executorService;
    }

    private static final Object DO_SUBMIT_KEY = new StringBuilder("doSubmit");
    private static AccumulativeRunnable<Runnable> getDoSubmit() {
        synchronized (DO_SUBMIT_KEY) {
            final AppContext appContext = AppContext.getAppContext();
            Object doSubmit = appContext.get(DO_SUBMIT_KEY);
            if (doSubmit == null) {
                doSubmit = new DoSubmitAccumulativeRunnable();
                appContext.put(DO_SUBMIT_KEY, doSubmit);
            }
            return (AccumulativeRunnable<Runnable>) doSubmit;
        }
    }
    private static class DoSubmitAccumulativeRunnable
    extends AccumulativeRunnable<Runnable> implements ActionListener {
        private final static int DELAY = 1000 / 30;
        @Override
        protected void run(List<Runnable> args) {
            for (Runnable runnable : args) {
                runnable.run();
            }
        }
        @Override
        protected void submit() {
            Timer timer = new Timer(DELAY, this);
            timer.setRepeats(false);
            timer.start();
        }
        @Override
        public void actionPerformed(ActionEvent event) {
            run();
        }
    }

    private class SwingWorkerPropertyChangeSupport
    extends PropertyChangeSupport {
        SwingWorkerPropertyChangeSupport(Object source) {
            super(source);
        }
        @Override
        public void firePropertyChange(final PropertyChangeEvent evt) {
            if (SwingUtilities.isEventDispatchThread()) {
                super.firePropertyChange(evt);
            } else {
                doSubmit.add(
                        new Runnable() {
                            @Override
                            public void run() {
                                SwingWorkerPropertyChangeSupport.this
                                .firePropertyChange(evt);
                            }
                        });
            }
        }
    }
}