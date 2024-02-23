package com.example.spgunlp.ui.visit

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spgunlp.databinding.FragmentObsBinding
import com.example.spgunlp.model.AppMessage
import com.example.spgunlp.model.CONTENT_TYPE
import com.example.spgunlp.ui.BaseFragment
import com.example.spgunlp.ui.profile.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.Date

class ObservationsFragment(
    private var principleId: Int,
    private var principleName: String,
    private var email: String
) : BaseFragment(), MessageClickListener {
    constructor() : this(0, "Principio", "user@mail.com")

    private val messagesViewModel: MessagesViewModel by activityViewModels()
    private val bundleViewModel: BundleViewModel by activityViewModels()
    private val visitViewModel: VisitViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()

    private var _binding: FragmentObsBinding? = null

    private val binding get() = _binding!!
    private val messagesList = mutableListOf<AppMessage>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentObsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (bundleViewModel.isObservationsStateEmpty()) {
            binding.detailTitle.text = principleName
            populateMessages()
        }

        binding.btnSend.setOnClickListener {
            val data = binding.inputMsg.text.toString()
            binding.inputMsg.setText("")
            //TODO send real type and data
            val idVisit = visitViewModel.visit.value!!.id
            if (idVisit != null) {
                Log.i("VISIT_ID_MESSAGES", idVisit.toString())
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                val isoDateTimeString = sdf.format(Date())
                profileViewModel.getPerfilByEmail(email).also {
                    it.observe(viewLifecycleOwner) { profile ->
                        Log.i("PROFILE_OBSERVATIONS", profile.toString())
                        val name = profile?.nombre ?: "user"
                        val message =
                            AppMessage(
                                0,
                                idVisit,
                                principleId,
                                CONTENT_TYPE.TEXT,
                                data,
                                isoDateTimeString,
                                AppMessage.ChatUser(email, name)
                            )
                        messagesViewModel.addMessage(message)
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        bundleViewModel.saveObservationsState(principleId, principleName, email)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            principleName = bundleViewModel.getPrincipleObsName().toString()
            principleId = bundleViewModel.getPrincipleId() ?: 0
            email = bundleViewModel.getEmail().toString()
            binding.detailTitle.text = principleName
            populateMessages()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun populateMessages() {
        val visitId = visitViewModel.visit.value!!.id
        Log.i("VISIT_ID_MESSAGES", id.toString())
        if (visitId != null)
            messagesViewModel.getMessagesByVisitPrinciple(visitId.toLong(), principleId.toLong())
                .also {
                    it.observe(
                        viewLifecycleOwner
                    ) { messages ->
                        Log.i("MESSAGES", messages.toString())
                        val messagesNotAdded = messages.filter { message ->
                            !messagesList.contains(message)
                        }
                        messagesList.addAll(messagesNotAdded)
                        updateRecycler()
                    }
                }
    }

    private fun updateRecycler() {
        binding.messagesList.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = ObservationsAdapter(messagesList, email, this@ObservationsFragment)
        }
    }

    override fun onClick(message: AppMessage) {
        //TODO open if message is image or audio
    }
}
