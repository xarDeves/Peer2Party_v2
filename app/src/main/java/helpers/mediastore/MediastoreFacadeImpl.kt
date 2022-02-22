package helpers.mediastore

import android.content.Context
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream

class MediastoreFacadeImpl(
    private val context: Context
) : MediastoreFacade {

    private lateinit var fos: FileOutputStream

    //FIXME this shouldn't take a context, we cannot instantiate from the model this way
    override fun pre(fname: String) {
        val externalStorageVolumes: Array<out File> =
            ContextCompat.getExternalFilesDirs(context, null)
        val primaryExternalStorage: File = externalStorageVolumes[0]

        val file = File(primaryExternalStorage.absolutePath + fname)

        fos = FileOutputStream(file)
    }

    override fun write(buffer: ByteArray, offset: Int, count: Int) {
        fos.write(buffer, offset, count)
        fos.flush()
    }

    override fun close() {
        fos.close()
    }
}