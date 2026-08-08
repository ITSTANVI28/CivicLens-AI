package com.example.civiclensai.workers;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.civiclensai.utils.NotificationHelper;

public class ReportSyncWorker extends Worker {

    private static final String TAG = "ReportSyncWorker";

    public ReportSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.i(TAG, "Network connection restored! Executing background offline report synchronization...");

        try {
            // Simulate syncing queued offline reports to Cloud Firestore
            Thread.sleep(1500);

            NotificationHelper.showReportSubmittedNotification(
                    getApplicationContext(),
                    "Offline Report Synchronized with Cloud"
            );

            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Error syncing offline report: " + e.getMessage());
            return Result.retry();
        }
    }

    /**
     * Schedules a background sync task that executes automatically when network connectivity is restored.
     */
    public static void scheduleOfflineSync(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest syncRequest = new OneTimeWorkRequest.Builder(ReportSyncWorker.class)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context).enqueue(syncRequest);
    }
}
