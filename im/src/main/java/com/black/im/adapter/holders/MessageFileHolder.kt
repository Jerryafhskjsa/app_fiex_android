package com.black.im.adapter.holders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.black.im.R
import com.black.im.model.chat.MessageInfo
import com.black.im.util.FileUtil.FormetFileSize
import com.black.im.util.ToastUtil.toastLongMessage
import com.black.im.widget.MessageLayoutUI
import com.tencent.imsdk.TIMCallBack
import com.tencent.imsdk.TIMFileElem

class MessageFileHolder(itemView: View, properties: MessageLayoutUI.Properties?) : MessageContentHolder(itemView, properties) {
    private var fileNameText: TextView? = null
    private var fileSizeText: TextView? = null
    private var fileStatusText: TextView? = null
    private var fileIconImage: ImageView? = null
    override fun getVariableLayout(): Int {
        return R.layout.message_adapter_content_file
    }

    override fun initVariableViews() {
        fileNameText = rootView.findViewById(R.id.file_name_tv)
        fileSizeText = rootView.findViewById(R.id.file_size_tv)
        fileStatusText = rootView.findViewById(R.id.file_status_tv)
        fileIconImage = rootView.findViewById(R.id.file_icon_iv)
    }

    override fun layoutVariableViews(msg: MessageInfo?, position: Int) {
        val elem = msg?.element as? TIMFileElem ?: return
        val fileElem = elem
        val path = msg.dataPath
        fileNameText?.text = fileElem.fileName
        val size = FormetFileSize(fileElem.fileSize)
        fileSizeText?.text = size
        msgContentFrame.setOnClickListener { toastLongMessage("文件路径:$path") }
        if (msg.isSelf) {
            if (msg.status == MessageInfo.MSG_STATUS_SENDING) {
                fileStatusText?.setText(R.string.sending)
            } else if (msg.status == MessageInfo.MSG_STATUS_SEND_SUCCESS || msg.status == MessageInfo.MSG_STATUS_NORMAL) {
                fileStatusText?.setText(R.string.sended)
            }
        } else {
            if (msg.status == MessageInfo.MSG_STATUS_DOWNLOADING) {
                fileStatusText?.setText(R.string.downloading)
            } else if (msg.status == MessageInfo.MSG_STATUS_DOWNLOADED) {
                fileStatusText?.setText(R.string.downloaded)
            } else if (msg.status == MessageInfo.MSG_STATUS_UN_DOWNLOAD) {
                fileStatusText?.setText(R.string.un_download)
                msgContentFrame.setOnClickListener {
                    msg.status = MessageInfo.MSG_STATUS_DOWNLOADING
                    sendingProgress.visibility = View.VISIBLE
                    fileStatusText?.setText(R.string.downloading)
                    fileElem.getToFile(path!!, object : TIMCallBack {
                        override fun onError(code: Int, desc: String) {
                            toastLongMessage("getToFile fail:$code=$desc")
                            fileStatusText?.setText(R.string.un_download)
                            sendingProgress.visibility = View.GONE
                        }

                        override fun onSuccess() {
                            msg.dataPath = path
                            fileStatusText?.setText(R.string.downloaded)
                            msg.status = MessageInfo.MSG_STATUS_DOWNLOADED
                            sendingProgress.visibility = View.GONE
                            msgContentFrame.setOnClickListener { toastLongMessage("文件路径:$path") }
                        }
                    })
                }
            }
        }
    }
}