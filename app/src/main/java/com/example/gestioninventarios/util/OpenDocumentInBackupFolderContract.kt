package com.example.gestioninventarios.util

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.DocumentsContract
import androidx.activity.result.contract.ActivityResultContracts

class OpenDocumentInBackupFolderContract : ActivityResultContracts.OpenDocument() {
    override fun createIntent(context: Context, input: Array<String>): Intent {
        val intent = super.createIntent(context, input)
        if (Build.VERSION.SDK_INT >= 26) {
            val uri = DocumentsContract.buildDocumentUri(
                "com.android.externalstorage.documents",
                "primary:${AlmacenRespaldos.CARPETA_APP}/${AlmacenRespaldos.CARPETA_RESPALDOS}"
            )
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri)
        }
        return intent
    }
}
