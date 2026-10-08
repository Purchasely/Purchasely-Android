package com.purchasely.samplev2.presentation.util

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import io.purchasely.ext.PLYConnection
import io.purchasely.ext.PLYCustomScreen


fun getAppCustomScreen(
    context: Context,
    useFragment: Boolean,
    vendorId: String?,
    connections: List<PLYConnection>,
    backgroundColor: String?,
    onExecuteConnection: (PLYConnection) -> Unit,
    onPrevious: () -> Unit
): PLYCustomScreen =
    if (useFragment) {
        PLYCustomScreen.Fragment(
            CustomFragment.newInstance(
                vendorId = vendorId ?: "N/A",
                connections = connections,
                backgroundColor = backgroundColor,
                onExecuteConnection = { onExecuteConnection(it) },
                onPrevious = { onPrevious() }
            )
        )
    } else {
        PLYCustomScreen.View(
            getCustomScreenView(
                context = context,
                vendorId = vendorId ?: "N/A",
                backgroundColor = backgroundColor,
                connections = connections,
                onExecuteConnection = { onExecuteConnection(it) },
                onPrevious = { onPrevious() }
            )
        )
    }


private fun getCustomScreenView(
    context: Context,
    vendorId: String,
    backgroundColor: String?,
    type: String = "View",
    connections: List<PLYConnection>,
    onExecuteConnection: (PLYConnection) -> Unit,
    onPrevious: () -> Unit
): View {

    return LinearLayout(context).apply {
        setBackgroundColor((backgroundColor ?: "#FFFFFF").toColorInt())
        setPadding(50, 400, 50, 100)
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        // Title
        addView(TextView(context).apply {
            setTextColor(Color.WHITE)
            text = "Custom Login $type"
            textSize = 24f
            textAlignment = View.TEXT_ALIGNMENT_CENTER
        })

        // Vendor ID
        addView(TextView(context).apply {
            setTextColor(Color.WHITE)
            text = vendorId
            textSize = 18f
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 32) }
        })

        // Spacer
        addView(View(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0
            ).apply { weight = 1f }
        })

        // One button per connection
        connections.forEach { connection ->
            addView(Button(context).apply {
                text = connection.id
                setOnClickListener { onExecuteConnection(connection) }
            })
        }

        // Back button
        addView(Button(context).apply {
            text = "⬅️ Back"
            setOnClickListener { onPrevious() }
        })
    }
}


class CustomFragment : Fragment() {

    var onExecuteConnection: ((PLYConnection) -> Unit)? = null
    var onBackClicked: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val vendorId = requireArguments().getString(ARG_VENDOR_ID) ?: "Unknown"
        val backgroundColor = requireArguments().getString(ARG_BG_COLOR)
        val connections =
            requireArguments().getParcelableArrayList<PLYConnection>(ARG_CONNECTIONS)
                ?: emptyList()

        return getCustomScreenView(
            context = requireContext(),
            vendorId = vendorId,
            type = "Fragment",
            connections = connections,
            backgroundColor = backgroundColor,
            onExecuteConnection = { connection ->
                onExecuteConnection?.invoke(connection)
            },
            onPrevious = { onBackClicked?.invoke() }
        )
    }

    companion object {
        private const val ARG_VENDOR_ID = "vendor_id"
        private const val ARG_CONNECTIONS = "connections"
        private const val ARG_BG_COLOR = "bg_color"

        fun newInstance(
            vendorId: String,
            connections: List<PLYConnection>,
            backgroundColor: String?,
            onExecuteConnection: (PLYConnection) -> Unit,
            onPrevious: () -> Unit
        ): CustomFragment {

            return CustomFragment().apply {
                this.onExecuteConnection = onExecuteConnection
                this.onBackClicked = onPrevious

                arguments = Bundle().apply {
                    putString(ARG_VENDOR_ID, vendorId)
                    putParcelableArrayList(ARG_CONNECTIONS, ArrayList(connections))
                    putString(ARG_BG_COLOR, backgroundColor)
                }
            }
        }
    }
}

