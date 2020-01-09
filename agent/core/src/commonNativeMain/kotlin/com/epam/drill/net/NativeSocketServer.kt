package com.epam.drill.net

class NativeSocketServer(val sockfds: Int) : NativeSocket(sockfds) {

    private var isRunning = true
    override fun isAlive() = isRunning
    override fun setIsAlive(isAlive: Boolean) {
        isRunning = false
    }
}