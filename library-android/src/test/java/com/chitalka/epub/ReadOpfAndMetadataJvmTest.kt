package com.chitalka.epub

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** JVM unit test (без Android runtime): readOpf + pickDcText на минимальном распакованном макете EPUB. */
class ReadOpfAndMetadataJvmTest {

    @Test
    fun readOpfFromUnpackedFixture_extractsTitleAndCreator() {
        val dest = File.createTempFile("chitalka-opf-test-", "-dir").apply { delete(); mkdirs() }
        try {
            File(dest, "META-INF").mkdirs()
            File(dest, "OEBPS").mkdirs()
            File(dest, "META-INF/container.xml").writeText(
                """
                <?xml version="1.0"?>
                <container xmlns="urn:oasis:names:tc:opendocument:xmlns:container" version="1.0">
                  <rootfiles>
                    <rootfile full-path="OEBPS/content.opf" media-type="application/oebps-package+xml"/>
                  </rootfiles>
                </container>
                """.trimIndent(),
            )
            File(dest, "OEBPS/content.opf").writeText(
                """
                <?xml version="1.0" encoding="UTF-8"?>
                <package xmlns="http://www.idpf.org/2007/opf" unique-identifier="bookid" version="3.0">
                  <metadata xmlns:dc="http://purl.org/dc/elements/1.1/">
                    <dc:title>Test Title Fixture</dc:title>
                    <dc:creator>Test Author Fixture</dc:creator>
                  </metadata>
                </package>
                """.trimIndent(),
            )
            val rootUri = ensureFileUri(dest.absolutePath).let { if (it.endsWith("/")) it else "$it/" }
            val r = readOpfFromUnpackedRootFiles(rootUri)
            assertTrue("OPF XML пустой", r.opfXml.isNotBlank())
            assertEquals("Test Title Fixture", pickDcText(r.opfXml, "title").trim())
            assertEquals("Test Author Fixture", pickDcText(r.opfXml, "creator").trim())
        } finally {
            dest.deleteRecursively()
        }
    }
}
