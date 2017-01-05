package com.android.strahinja.stockhawk.sync;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;

public class JobSchedulerService extends JobService {
    private static final String LOG_TAG = JobSchedulerService.class.getSimpleName();

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        startService(new Intent(this, JobIntentService.class));
        jobFinished(jobParameters, false);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return true;
    }
}
