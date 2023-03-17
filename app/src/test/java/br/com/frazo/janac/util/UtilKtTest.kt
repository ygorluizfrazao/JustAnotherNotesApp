package br.com.frazo.janac.util

import com.google.common.truth.Truth
import org.junit.Test

class UtilKtTest{

    @Test
    fun givenString_CapitalizeWords(){
        Truth.assertThat("john doe".capitalizeWords()).isEqualTo("John Doe")
        Truth.assertThat("John doe".capitalizeWords()).isEqualTo("John Doe")
        Truth.assertThat("john Doe".capitalizeWords()).isEqualTo("John Doe")
        Truth.assertThat("jOHN dOE".capitalizeWords()).isEqualTo("John Doe")
        Truth.assertThat("JOHN   ".capitalizeWords()).isEqualTo("John")
        Truth.assertThat("  ".capitalizeWords()).isEqualTo("  ")
        Truth.assertThat("".capitalizeWords()).isEqualTo("")
        Truth.assertThat("                          ".capitalizeWords()).isEqualTo("                          ")
    }

}