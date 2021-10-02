package org.softeg.slartus.forpdacommon

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import org.softeg.slartus.forpdacommon.StringUtils.copyToClipboard
import timber.log.Timber

fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(url)
        )
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            copyToClipboard(context, url)
            Toast.makeText(
                context,
                context.getString(R.string.link_copied_to_buffer),
                Toast.LENGTH_SHORT
            ).show()
            throw ActivityNotFoundException()
        }
    } catch (ex: ActivityNotFoundException) {
        Timber.e(
            NotReportException(context.getString(R.string.no_app_for_link).toString() + ": " + url)
        )
    } catch (ex: Throwable) {
        Timber.e(ex)
    }
}