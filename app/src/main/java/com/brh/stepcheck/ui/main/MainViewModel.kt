package com.brh.stepcheck.ui.main

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brh.stepcheck.FitApp
import com.brh.stepcheck.model.State
import com.brh.stepcheck.model.StepData
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataSource
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.result.DataReadResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainViewModel : ViewModel() {

    @Inject
    lateinit var account: GoogleSignInAccount

    @Inject
    lateinit var fitSignInOptions: FitnessOptions

    //the true LiveData
    private val liveData = MutableLiveData<State>(State.Loading)

    //this is a facade that will be accessed by Fragments, made as LiveData
    //so only this class can post/set values
    val stepsLiveData : LiveData<State> = liveData


    init {
        FitApp.appComponent.inject(this)
    }

    fun checkFitPermissions(fragment: Fragment, requestCode: Int) {
        if (!GoogleSignIn.hasPermissions(account, fitSignInOptions)) {
            /* if this call supported ActivityResultCallback, we could completely contain
               This work within this viewModel, since it does back the fragment has to handle
               the activity result.
             */
            GoogleSignIn.requestPermissions(
                fragment, // your activity
                requestCode, // e.g. 1
                account,
                fitSignInOptions
            );
        } else //permission already granted fetch the data
            fetchData()
    }

    /**
     * For more than simple apps this is sufficient, larger apps the actual data fetch would
     * be through a repository or a usecase class
     */
    fun fetchData() {
        val cal = Calendar.getInstance();
        cal.setTime(Date());
        val endTime = cal.getTimeInMillis ();
        cal.add(Calendar.DATE, -14);
        val startTime = cal.getTimeInMillis ();

        val ds= DataSource.Builder()
            .setAppPackageName("com.google.android.gms")
            .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
            .setType(DataSource.TYPE_DERIVED)
            .setStreamName("estimated_steps")
            .build()

        val readRequest = DataReadRequest.Builder()
            .aggregate(ds, DataType.AGGREGATE_STEP_COUNT_DELTA)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .bucketByTime(1, TimeUnit.DAYS)
            .build();

        Fitness.getHistoryClient(FitApp.ctx, account)
            .readData(readRequest)
            .addOnSuccessListener {dataResponse:DataReadResponse ->
                //spawn a coroutine to stay off of main thread
                viewModelScope.launch(Dispatchers.IO) {
                    val stepField = DataType.AGGREGATE_STEP_COUNT_DELTA.fields[0]
                    //filter only days that have actual data so there won't be 0s in the list
                    val dateStepsList =
                        dataResponse.buckets.filter { bucket -> !(bucket.dataSets[0].dataPoints.isEmpty()) }
                            .map { bucket ->
                                val steps =
                                    bucket.dataSets[0].dataPoints[0].getValue(stepField).asInt()
                                val date = Date(bucket.getStartTime(TimeUnit.MILLISECONDS))
                                StepData(date, steps)
                            }
                    liveData.postValue(State.Content(dateStepsList))
                }
            }
            .addOnFailureListener{ e:Exception ->
                liveData.value = State.Error(e)
            }
    }
}