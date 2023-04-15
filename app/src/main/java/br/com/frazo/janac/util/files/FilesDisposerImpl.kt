package br.com.frazo.janac.util.files

import java.io.File

class FilesDisposerImpl : FilesDisposer {

    private val bin = mutableListOf<File>()

    override fun moveToBin(file: File) {
        bin.add(file)
    }

    override fun restoreFile(fileName: String): File? {
        return bin.firstOrNull { it.name == fileName }.also {
            it?.let { file ->
                bin.remove(file)
            }
        }
    }

    override fun clearBin() {
        bin.forEach{
            if(it.exists()){
                it.delete()
            }
        }
        bin.clear()
    }

}