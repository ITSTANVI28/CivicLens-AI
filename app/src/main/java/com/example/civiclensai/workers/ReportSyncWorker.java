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

import com.example.civiclensai.models.CivicIssue;
import com.example.civiclensai.repository.IssueRepository;
import com.example.civiclensai.utils.NotificationHelper;

import java.util.List;

public class ReportSyncWorker extends Worker {

    private static final String TAG = "ReportSyncWorker";

    public ReportSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.i(TAG, "Network connection restored! Executing background offline report synchronization with Firebase Firestore...");

        try {
            List<CivicIssue> issues = IssueRepository.getInstance().getIssues().getValue();
            if (issues != null) {
                for (CivicIssue issue : issues) {
                    IssueRepository.getInstance().updateIssue(issue);
                }
            }

            NotificationHelper.showReportSubmittedNotification(
                    getApplicationContext(),
                    "Offline Reports Synchronized with Firebase Firestore"
            );

            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Error syncing offline report to Firebase: " + e.getMessage());
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

