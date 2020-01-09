import com.epam.drill.core.concurrency.BackgroundThread
import com.epam.drill.core.concurrency.BackgroundThread2
import com.epam.drill.core.concurrency.BackgroundThread3
import com.epam.drill.internal.socket.setup_buffer_size
import com.epam.drill.internal.socket.socket_get_error
import com.epam.drill.io.ktor.utils.io.internal.utils.test.make_socket_non_blocking
import com.epam.drill.net.NativeAsyncServer
import com.epam.drill.net.NativeSocketServer
import com.epam.drill.net.PosixException
import com.epam.drill.ws.WsFrame
import com.epam.drill.ws.WsOpcode
import com.epam.drill.ws.readWsFrame
import com.epam.drill.ws.sendWsFrame
import kotlinx.cinterop.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.io.bits.reverseByteOrder
import kotlinx.io.core.ByteOrder
import kotlinx.io.core.IoBuffer
import kotlinx.io.core.toByteArray
import kotlinx.io.core.writeFully
import kotlinx.io.internal.utils.KX_SOCKET
import platform.posix.*


object Echo {
    data class Client(val fd: Int, val headers: String, val cln: NativeAsyncServer) {
        suspend fun close() {
            cln.sendWsFrame(WsFrame(byteArrayOf(), WsOpcode.Close))
            com.epam.drill.net.close(cln.socket.sockfds.toULong())
        }
    }

    data class SingleConnectServer(val fd: KX_SOCKET, val port: Int, val accepted: Channel<Client> = Channel())

    suspend fun startServer() = memScoped {
        val serverAddr = alloc<sockaddr_in>()
        with(serverAddr) {
            memset(this.ptr, 0, sockaddr_in.size.convert())
            sin_family = AF_INET.convert()
            sin_port = htons(0.toUShort())
        }

        val acceptor = bind(serverAddr)
        val addrSizeResult = alloc<UIntVar>()
        addrSizeResult.value = sockaddr_in.size.convert()
        getsockname(
            acceptor, serverAddr.ptr.reinterpret(),
            addrSizeResult.ptr.reinterpret()
        ).checkError("getsockname()")
        val server = SingleConnectServer(acceptor, htons(serverAddr.sin_port).toInt())
        BackgroundThread {
            while (true) {
                val accepted: KX_SOCKET = accept(acceptor)
                delay(100)
                setup_buffer_size(accepted)
                val processHeaders = processHeaders(accepted)
                val nativeSocket = NativeSocketServer(accepted)
                val nativeAsyncServer = NativeAsyncServer(nativeSocket)
                BackgroundThread3 {
                    server.accepted.send(Client(accepted, processHeaders, nativeAsyncServer))
                }
                BackgroundThread2 {
                    while (true) {
                        val frame = nativeAsyncServer.readWsFrame()
                        try {
                            when (frame.type) {
                                WsOpcode.Close -> {
                                    nativeAsyncServer.sendWsFrame(WsFrame(byteArrayOf(), WsOpcode.Close))
                                }
                                WsOpcode.Ping -> {
                                    nativeAsyncServer.sendWsFrame(WsFrame(frame.data, WsOpcode.Pong))
                                }
                                WsOpcode.Pong -> {
                                }
                                else -> {
                                    nativeAsyncServer.sendWsFrame(frame)
                                }
                            }
                        } catch (ex: Exception) {
                            break
                        }
                    }
                }
            }
        }
        server
    }

    private suspend fun accept(acceptor: Int): KX_SOCKET {
        val zero: KX_SOCKET = 0.convert()
        var accepted: KX_SOCKET = zero
        while (accepted == zero) {
            delay(500)
            val result = accept(acceptor, null, null)

            if (result < zero || result == KX_SOCKET.MAX_VALUE) {
                val error = socket_get_error()
                if (error != EAGAIN && error != EWOULDBLOCK) {
                    throw PosixException.forErrno(error, "accept()")
                }
            } else {
                accepted = result
                accepted.makeNonBlocking()
            }

        }
        return accepted
    }

    private fun processHeaders(accepted: KX_SOCKET): String {
        val buffer = IoBuffer.Pool.borrow()
        buffer.resetForWrite()
        //read connect headers
        val headersSize = kotlinx.io.streams.recv(accepted, buffer, 0)
        val dst = ByteArray(headersSize.convert())
        buffer.readFully(dst, 0, headersSize.convert())
        val headers = dst.decodeToString()
        buffer.resetForWrite()
        buffer.writeFully("101 OK\n\n".toByteArray())
        kotlinx.io.streams.send(accepted, buffer, 0)
        return headers
    }

    private suspend fun bind(serverAddr: sockaddr_in): Int {
        val acceptor = socket(AF_INET, SOCK_STREAM, 0).checkError("socket()")
        setup_buffer_size(acceptor)
        acceptor.makeNonBlocking()
        bind(acceptor, serverAddr.ptr.reinterpret(), sockaddr_in.size.convert()).let { rc ->
            if (rc != 0) {
                delay(1000)
                bind(serverAddr)
            }
        }
        listen(acceptor, 10).checkError("listen()")
        return acceptor
    }


    private fun KX_SOCKET.makeNonBlocking() {
        make_socket_non_blocking(this)
    }

    @Suppress("unused")
    internal fun Int.checkError(action: String = ""): Int = when {
        this < 0 -> memScoped { throw PosixException.forErrno(posixFunctionName = action) }
        else -> this
    }

    @Suppress("unused")
    internal fun Long.checkError(action: String = ""): Long = when {
        this < 0 -> memScoped { throw PosixException.forErrno(posixFunctionName = action) }
        else -> this
    }

    private val ZERO: size_t = 0u

    @Suppress("unused")
    internal fun size_t.checkError(action: String = ""): size_t = when (this) {
        ZERO -> errno.let { errno ->
            when (errno) {
                0 -> this
                else -> memScoped { throw PosixException.forErrno(posixFunctionName = action) }
            }
        }
        else -> this
    }

    private fun htons(value: UShort): uint16_t = when (ByteOrder.BIG_ENDIAN) {
        ByteOrder.nativeOrder() -> value
        else -> value.toShort().reverseByteOrder().toUShort()
    }

}