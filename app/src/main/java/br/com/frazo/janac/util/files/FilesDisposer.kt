package br.com.frazo.janac.util.files

import java.io.File

interface FilesDisposer {

    fun moveToBin(file: File)

    fun restoreFile(fileName: String): File?

    fun clearBin()

}