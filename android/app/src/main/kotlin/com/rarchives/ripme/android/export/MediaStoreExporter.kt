package com.rarchives.ripme.android.export

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import java.io.File

/**
 * Copies a finished rip's files into the system MediaStore's Downloads collection, under
 * "Download/RipMe/<album>". This is this app's answer to app-scoped external storage
 * (`getExternalFilesDir`, see RipMeApplication) being wiped on uninstall - a rip that's only ever
 * lived under that app-private directory disappears the moment the app does; exporting a copy into
 * the shared Downloads collection is how a rip outlives that (plan's "Export to Downloads").
 *
 * Surfaced from two call sites (both do the actual `export()` call off the main thread and then
 * report the [Result] through `LocalSnackbarHostState`): HistoryScreen's per-row action, and
 * RipScreen's "rip just completed" card.
 */
object MediaStoreExporter {

    /** One export attempt's outcome - enough to build a Snackbar message from. */
    data class ExportResult(val filesCopied: Int, val relativePath: String)

    /**
     * `MediaStore.Downloads` (the write target here) was added in API 29 (Q); this app's `minSdk`
     * is 26 (see :app/build.gradle.kts's comment on `java.nio.file` desugaring), so there is a
     * real API 26-28 gap where export isn't possible through this collection. The legacy
     * pre-scoped-storage alternative (`WRITE_EXTERNAL_STORAGE` + writing directly under
     * `Environment.getExternalStoragePublicDirectory`) would close that gap but adds a second
     * storage code path and a permission this app otherwise never needs - out of scope for a
     * v1/preview app (see android/README.md's known gaps); callers check this before calling
     * [export] and disable/explain the action instead.
     */
    fun isSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    /**
     * Copies every regular file under [albumDir] (recursively, but flattened into one destination
     * folder - a handful of rippers write a per-item description .txt file alongside the media,
     * but none nest media itself in sub-sub-folders deep enough for flattening to collide in
     * practice) into `Download/RipMe/<albumDir's own folder name>`. Reusing that folder name
     * (rather than a HistoryRow's human-entered URL or title) sidesteps needing a second filename
     * sanitizer: the ripper already made it filesystem-safe when it created [albumDir].
     *
     * Does not check for a prior export of the same file before inserting - MediaStore itself
     * auto-suffixes a colliding `DISPLAY_NAME` under the same `RELATIVE_PATH` (e.g.
     * "image (1).jpg") rather than failing, so a second tap of "Export to Downloads" on the same
     * album is safe, just wasteful (duplicate copies, not corruption or an error). Good enough for
     * a v1 action a user presses once per finished rip; see android/README.md's known gaps.
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun export(context: Context, albumDir: File): Result<ExportResult> {
        if (!isSupported()) {
            return Result.failure(UnsupportedOperationException("Export to Downloads needs Android 10 or newer"))
        }
        if (!albumDir.isDirectory) {
            return Result.failure(IllegalArgumentException("Rip folder no longer exists: $albumDir"))
        }
        val files = albumDir.walkTopDown().filter { it.isFile }.toList()
        if (files.isEmpty()) {
            return Result.failure(IllegalStateException("No files found in $albumDir"))
        }

        val relativePath = "${Environment.DIRECTORY_DOWNLOADS}/RipMe/${albumDir.name}"
        val resolver = context.contentResolver
        var copied = 0

        for (file in files) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, file.name)
                put(MediaStore.Downloads.RELATIVE_PATH, relativePath)
                put(MediaStore.Downloads.MIME_TYPE, guessMimeType(file.name))
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: continue
            resolver.openOutputStream(uri)?.use { out ->
                file.inputStream().use { input -> input.copyTo(out) }
            }
            values.clear()
            values.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            copied++
        }

        return if (copied == 0) {
            Result.failure(IllegalStateException("MediaStore rejected every file in $albumDir"))
        } else {
            Result.success(ExportResult(copied, relativePath))
        }
    }

    private fun guessMimeType(fileName: String): String {
        val extension = fileName.substringAfterLast('.', missingDelimiterValue = "").lowercase()
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "application/octet-stream"
    }
}
