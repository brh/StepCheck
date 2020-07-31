package com.brh.stepcheck.ui.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.brh.stepcheck.R
import com.brh.stepcheck.databinding.MainFragmentBinding
import com.brh.stepcheck.model.State
import com.brh.stepcheck.model.StepData
import com.google.android.material.snackbar.Snackbar

class MainFragment : Fragment() {

    private val FIT_PERMISSION_REQUEST = 44

    private lateinit var binding: MainFragmentBinding
    private lateinit var viewModel: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        binding = MainFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.checkFitPermissions(this, FIT_PERMISSION_REQUEST)

        viewModel.stepsLiveData.observe(viewLifecycleOwner, Observer {state->
            when(state) {
                is State.Content->{
                    setupRecyclerView(state.steps)
                }
                is State.Loading ->{}
                is State.Error->{
                    Snackbar.make(binding.recyclerView, R.string.error, Snackbar.LENGTH_INDEFINITE)
                        .apply {
                            setAction(R.string.ok){
                                this.dismiss()
                            }
                            show()
                        }
                }
            }

        })
    }

    private fun setupRecyclerView(steps: List<StepData>) {
        binding.recyclerView.apply {
            binding.progress.visibility = View.GONE
            adapter = StepDataAdapter(steps)
            layoutManager = LinearLayoutManager(requireContext())
            if (steps.isEmpty())
                Snackbar.make(this, R.string.nosteps, Snackbar.LENGTH_INDEFINITE).apply {
                    this.setAction(R.string.refresh){
                        viewModel.fetchData()
                        dismiss()
                    }
                    show()
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //can't use ActivityResultCallback in Fit until the api is updated
        if (requestCode == FIT_PERMISSION_REQUEST && resultCode == Activity.RESULT_OK) {
            viewModel.fetchData()
        } else
            Toast.makeText(requireContext(), "Well", Toast.LENGTH_LONG).show()
        super.onActivityResult(requestCode, resultCode, data)
    }

}