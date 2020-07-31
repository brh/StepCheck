package com.brh.stepcheck.ui.main

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.brh.stepcheck.FitApp
import com.brh.stepcheck.R
import com.brh.stepcheck.databinding.ItemHeaderBinding
import com.brh.stepcheck.databinding.ItemStepDataBinding
import com.brh.stepcheck.model.StepData

private const val HEADER = 0
private const val DATA = 1

class StepDataAdapter(var stepList : List<StepData>) : RecyclerView.Adapter<StepDataVH>() {

    var mirrorList : List<StepData>? = null

    override fun getItemViewType(position: Int): Int = when (position) {
        0 -> HEADER
        else -> DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StepDataVH {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            HEADER -> {
                StepDataVH(ItemHeaderBinding.inflate(inflater, parent, false))
            }
            else -> {
                StepDataVH(ItemStepDataBinding.inflate(inflater, parent, false))
            }
        }
    }

    override fun getItemCount(): Int {
        return stepList.size + 1
    }

    override fun onBindViewHolder(holder: StepDataVH, position: Int) {
        when (position) {
            0 -> {
                val binding = holder.binder as ItemHeaderBinding
                binding.tvHeaderDate.setOnClickListener {
                    //if mirrorlist is null switch to descending else ascending
                    mirrorList?.let {
                        mirrorList = null
                        binding.tvHeaderDate.text = FitApp.ctx.getString(R.string.date_desc)
                    } ?: run {
                        mirrorList = stepList.reversed()
                        binding.tvHeaderDate.text = FitApp.ctx.getString(R.string.date)
                    }

                    //just notify the actual list data not the header, nanoseconds saved
                    notifyItemRangeChanged(1, stepList!!.size+1)
                }
            }
            else -> {
                val binding = holder.binder as ItemStepDataBinding
                //if mirrorList is active use it
                val item = (mirrorList ?: stepList)[position-1]
                binding.tvDate.text = DateFormat.getMediumDateFormat(binding.root.context).format(item.date)
                binding.tvSteps.text = item.steps.toString()

            }
        }
    }
}

class StepDataVH(var binder : ViewBinding) : RecyclerView.ViewHolder(binder.root) {

}
