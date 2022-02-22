package helpers.mediastore

import java.io.Closeable

interface MediastoreFacade : Closeable {

    fun pre(fname: String)

    fun write(buffer: ByteArray, offset: Int, count: Int)
}