package com.black.base.observe

import android.widget.PopupWindow
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.Exceptions
import io.reactivex.functions.Cancellable
import io.reactivex.internal.disposables.CancellableDisposable
import io.reactivex.internal.disposables.DisposableHelper
import io.reactivex.internal.fuseable.SimpleQueue
import io.reactivex.internal.queue.SpscLinkedArrayQueue
import io.reactivex.internal.util.AtomicThrowable
import io.reactivex.plugins.RxJavaPlugins
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

open class ObservablePopupWindow<T> : Observable<T?>(), PopupWindow.OnDismissListener {
    protected var emitter: ObservableEmitter<T>? = null
    private val source: ObservableOnSubscribe<T>

    init {
        source = ObservableOnSubscribe { emitter -> this@ObservablePopupWindow.emitter = emitter }
    }

    override fun onDismiss() {
        if (emitter != null) {
            emitter!!.onComplete()
        }
    }

    override fun subscribeActual(observer: Observer<in T?>) {
        val parent = WindowEmitter(observer)
        observer.onSubscribe(parent)
        try {
            source.subscribe(parent)
        } catch (ex: Throwable) {
            Exceptions.throwIfFatal(ex)
            parent.onError(ex)
        }
    }

    internal class WindowEmitter<T>(val observer: Observer<in T>) : AtomicReference<Disposable?>(), ObservableEmitter<T>, Disposable {
        override fun onNext(t: T) {
            if (t == null) {
                onError(NullPointerException("onNext called with null. Null values are generally not allowed in 2.x operators and sources."))
                return
            }
            if (!isDisposed) {
                observer.onNext(t)
            }
        }

        override fun onError(t: Throwable) {
            if (!tryOnError(t)) {
                RxJavaPlugins.onError(t)
            }
        }

        override fun tryOnError(t: Throwable): Boolean {
            var throwable: Throwable? = t
            if (throwable == null) {
                throwable = NullPointerException("onError called with null. Null values are generally not allowed in 2.x operators and sources.")
            }
            if (!isDisposed) {
                try {
                    observer.onError(throwable)
                } finally {
                    dispose()
                }
                return true
            }
            return false
        }

        override fun onComplete() {
            if (!isDisposed) {
                try {
                    observer.onComplete()
                } finally {
                    dispose()
                }
            }
        }

        override fun setDisposable(d: Disposable?) {
            DisposableHelper.set(this, d)
        }

        override fun setCancellable(c: Cancellable?) {
            setDisposable(CancellableDisposable(c))
        }

        override fun serialize(): ObservableEmitter<T> {
            return SerializedEmitter(this)
        }

        override fun dispose() {
            DisposableHelper.dispose(this)
        }

        override fun isDisposed(): Boolean {
            return DisposableHelper.isDisposed(get())
        }

        override fun toString(): String {
            return String.format("%s{%s}", javaClass.simpleName, super.toString())
        }

        companion object {
            private const val serialVersionUID = -3434801548987643227L
        }

    }

    internal class SerializedEmitter<T>(val emitter: ObservableEmitter<T>) : AtomicInteger(), ObservableEmitter<T> {
        companion object {
            private const val serialVersionUID = 4883307006032401862L
        }

        val error: AtomicThrowable = AtomicThrowable()
        private val queue: SpscLinkedArrayQueue<T> = SpscLinkedArrayQueue(16)
        @Volatile
        var done = false

        override fun onNext(t: T) {
            if (emitter.isDisposed || done) {
                return
            }
            if (t == null) {
                onError(NullPointerException("onNext called with null. Null values are generally not allowed in 2.x operators and sources."))
                return
            }
            if (get() == 0 && compareAndSet(0, 1)) {
                emitter.onNext(t)
                if (decrementAndGet() == 0) {
                    return
                }
            } else {
                val q: SimpleQueue<T> = queue
                synchronized(q) { q.offer(t) }
                if (andIncrement != 0) {
                    return
                }
            }
            drainLoop()
        }

        override fun onError(t: Throwable) {
            if (!tryOnError(t)) {
                RxJavaPlugins.onError(t)
            }
        }

        override fun tryOnError(t: Throwable): Boolean {
            var throwable: Throwable? = t
            if (emitter.isDisposed || done) {
                return false
            }
            if (throwable == null) {
                throwable = NullPointerException("onError called with null. Null values are generally not allowed in 2.x operators and sources.")
            }
            if (error.addThrowable(throwable)) {
                done = true
                drain()
                return true
            }
            return false
        }

        override fun onComplete() {
            if (emitter.isDisposed || done) {
                return
            }
            done = true
            drain()
        }

        private fun drain() {
            if (andIncrement == 0) {
                drainLoop()
            }
        }

        private fun drainLoop() {
            val e = emitter
            val q = queue
            val error = error
            var missed = 1
            while (true) {
                while (true) {
                    if (e.isDisposed) {
                        q.clear()
                        return
                    }
                    if (error.get() != null) {
                        q.clear()
                        e.onError(error.terminate())
                        return
                    }
                    val d = done
                    val v = q.poll()
                    val empty = v == null
                    if (d && empty) {
                        e.onComplete()
                        return
                    }
                    if (empty) {
                        break
                    }
                    e.onNext(v!!)
                }
                missed = addAndGet(-missed)
                if (missed == 0) {
                    break
                }
            }
        }

        override fun setDisposable(d: Disposable?) {
            emitter.setDisposable(d)
        }

        override fun setCancellable(c: Cancellable?) {
            emitter.setCancellable(c)
        }

        override fun isDisposed(): Boolean {
            return emitter.isDisposed
        }

        override fun serialize(): ObservableEmitter<T> {
            return this
        }

        override fun toString(): String {
            return emitter.toString()
        }

        override fun toByte(): Byte {
            return toInt().toByte()
        }

        override fun toChar(): Char {
            return toInt().toChar()
        }

        override fun toShort(): Short {
            return toInt().toShort()
        }
    }
}
