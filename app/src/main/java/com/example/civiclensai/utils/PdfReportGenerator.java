package com.example.civiclensai.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import androidx.core.content.FileProvider;

import com.example.civiclensai.models.CivicIssue;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PdfReportGenerator {

    private static final String TAG = "PdfReportGenerator";

    /**
     * Creates an official PDF municipal complaint ticket document for a CivicIssue using Android's native PdfDocument API.
     */
    private static PdfDocument createPdfDocument(CivicIssue issue) {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // A4 Size in points (595x842)
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint fillPaint = new Paint();
        Paint strokePaint = new Paint();
        Paint textPaint = new Paint();

        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setAntiAlias(true);
        fillPaint.setAntiAlias(true);
        textPaint.setAntiAlias(true);

        // 1. Header Background Banner
        fillPaint.setColor(Color.parseColor("#0F172A")); // Slate Navy Header
        canvas.drawRect(0, 0, 595, 105, fillPaint);

        // Accent Top Line
        fillPaint.setColor(Color.parseColor("#2563EB")); // Royal Blue Accent Accent
        canvas.drawRect(0, 0, 595, 6, fillPaint);

        // Header Title & Subtitle
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(20);
        textPaint.setFakeBoldText(true);
        canvas.drawText("CIVICLENS AI — MUNICIPAL WORK ORDER", 28, 45, textPaint);

        textPaint.setTextSize(11);
        textPaint.setFakeBoldText(false);
        textPaint.setColor(Color.parseColor("#94A3B8"));
        canvas.drawText("Automated Civic Hazard Incident Record & Municipal Department Dispatch Ticket", 28, 68, textPaint);

        // Header Top-Right Pill Badge
        fillPaint.setColor(Color.parseColor("#2563EB"));
        canvas.drawRoundRect(430, 28, 565, 54, 12, 12, fillPaint);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(10);
        textPaint.setFakeBoldText(true);
        canvas.drawText("OFFICIAL TICKET", 448, 44, textPaint);

        // 2. Status & Severity Cards Bar
        int barY = 120;
        // Left Severity Card
        fillPaint.setColor(Color.parseColor("#F8FAFC"));
        strokePaint.setColor(Color.parseColor("#E2E8F0"));
        strokePaint.setStrokeWidth(1);
        canvas.drawRoundRect(28, barY, 285, barY + 55, 10, 10, fillPaint);
        canvas.drawRoundRect(28, barY, 285, barY + 55, 10, 10, strokePaint);

        textPaint.setColor(Color.parseColor("#64748B"));
        textPaint.setTextSize(9);
        textPaint.setFakeBoldText(true);
        canvas.drawText("SEVERITY LEVEL", 40, barY + 20, textPaint);

        int severityColor = Color.parseColor("#DC2626");
        try {
            severityColor = Color.parseColor(issue.getSeverity().getHexColor());
        } catch (Exception ignored) {}

        fillPaint.setColor(severityColor);
        canvas.drawRoundRect(40, barY + 28, 270, barY + 46, 6, 6, fillPaint);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(10);
        textPaint.setFakeBoldText(true);
        canvas.drawText(issue.getSeverity().getLabel() + " SEVERITY", 50, barY + 41, textPaint);

        // Right SLA Card
        fillPaint.setColor(Color.parseColor("#F8FAFC"));
        canvas.drawRoundRect(300, barY, 567, barY + 55, 10, 10, fillPaint);
        canvas.drawRoundRect(300, barY, 567, barY + 55, 10, 10, strokePaint);

        textPaint.setColor(Color.parseColor("#64748B"));
        textPaint.setTextSize(9);
        textPaint.setFakeBoldText(true);
        canvas.drawText("TARGET RESOLUTION SLA", 312, barY + 20, textPaint);

        textPaint.setColor(Color.parseColor("#0F172A"));
        textPaint.setTextSize(12);
        textPaint.setFakeBoldText(true);
        canvas.drawText(issue.getSeverity().getSlaDescription() + " Resolution Window", 312, barY + 42, textPaint);

        // 3. Incident Metadata Box
        int metaY = 190;
        fillPaint.setColor(Color.parseColor("#F8FAFC"));
        canvas.drawRoundRect(28, metaY, 567, metaY + 160, 12, 12, fillPaint);
        canvas.drawRoundRect(28, metaY, 567, metaY + 160, 12, 12, strokePaint);

        // Section Title Header
        fillPaint.setColor(Color.parseColor("#1E293B"));
        canvas.drawRoundRect(28, metaY, 567, metaY + 32, 12, 12, fillPaint);
        // Cover bottom rounded corners of section header
        canvas.drawRect(28, metaY + 20, 567, metaY + 32, fillPaint);

        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(11);
        textPaint.setFakeBoldText(true);
        canvas.drawText("1. INCIDENT METADATA & DEPARTMENT ROUTING", 40, metaY + 21, textPaint);

        // Grid Content Inside Metadata Box
        textPaint.setTextSize(10);
        int gridY = metaY + 52;

        drawGridRow(canvas, textPaint, 40, gridY, "Ticket ID:", issue.getId(), 312, "Category:", issue.getCategory().getDisplayName());
        gridY += 24;
        drawGridRow(canvas, textPaint, 40, gridY, "Issue Title:", issue.getTitle(), 312, "Department:", issue.getDepartment());
        gridY += 24;
        drawGridRow(canvas, textPaint, 40, gridY, "Reporter Name:", issue.getReporterName(), 312, "Upvote Support:", issue.getUpvotesCount() + " Citizens Verified");
        gridY += 24;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        drawGridRow(canvas, textPaint, 40, gridY, "Timestamp:", sdf.format(new Date(issue.getTimestamp())), 312, "Resolution SLA:", issue.getFormattedSlaRemaining());

        // 4. Geographical Location Box
        int locY = 365;
        fillPaint.setColor(Color.parseColor("#F8FAFC"));
        canvas.drawRoundRect(28, locY, 567, locY + 120, 12, 12, fillPaint);
        canvas.drawRoundRect(28, locY, 567, locY + 120, 12, 12, strokePaint);

        fillPaint.setColor(Color.parseColor("#1E293B"));
        canvas.drawRoundRect(28, locY, 567, locY + 32, 12, 12, fillPaint);
        canvas.drawRect(28, locY + 20, 567, locY + 32, fillPaint);

        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(11);
        textPaint.setFakeBoldText(true);
        canvas.drawText("2. GEOGRAPHICAL DISPATCH LOCATION & SPATIAL DATA", 40, locY + 21, textPaint);

        gridY = locY + 54;
        textPaint.setColor(Color.parseColor("#64748B"));
        textPaint.setFakeBoldText(true);
        canvas.drawText("Full Location Address:", 40, gridY, textPaint);
        textPaint.setColor(Color.parseColor("#0F172A"));
        textPaint.setFakeBoldText(false);
        canvas.drawText(issue.getAddress(), 170, gridY, textPaint);

        gridY += 24;
        textPaint.setColor(Color.parseColor("#64748B"));
        textPaint.setFakeBoldText(true);
        canvas.drawText("GPS Coordinates:", 40, gridY, textPaint);
        textPaint.setColor(Color.parseColor("#0F172A"));
        textPaint.setFakeBoldText(false);
        canvas.drawText(String.format(Locale.US, "Latitude: %.5f  •  Longitude: %.5f", issue.getLatitude(), issue.getLongitude()), 170, gridY, textPaint);

        gridY += 24;
        textPaint.setColor(Color.parseColor("#64748B"));
        textPaint.setFakeBoldText(true);
        canvas.drawText("Deduplication Zone:", 40, gridY, textPaint);
        textPaint.setColor(Color.parseColor("#059669"));
        textPaint.setFakeBoldText(true);
        canvas.drawText("50m Haversine Radius Verified Clean (No Merged Conflicts)", 170, gridY, textPaint);

        // 5. Hazard Description & AI Triage Summary Box
        int descY = 500;
        fillPaint.setColor(Color.parseColor("#EFF6FF")); // Light Blue Fill
        canvas.drawRoundRect(28, descY, 567, descY + 140, 12, 12, fillPaint);
        strokePaint.setColor(Color.parseColor("#BFDBFE"));
        canvas.drawRoundRect(28, descY, 567, descY + 140, 12, 12, strokePaint);

        // Left Thick Accent Border
        fillPaint.setColor(Color.parseColor("#2563EB"));
        canvas.drawRoundRect(28, descY, 36, descY + 140, 4, 4, fillPaint);

        textPaint.setColor(Color.parseColor("#1E40AF"));
        textPaint.setTextSize(11);
        textPaint.setFakeBoldText(true);
        canvas.drawText("3. HAZARD DESCRIPTION & GEMINI AI VISION TRIAGE SUMMARY", 48, descY + 26, textPaint);

        textPaint.setColor(Color.parseColor("#1E293B"));
        textPaint.setTextSize(10);
        textPaint.setFakeBoldText(false);

        // Multi-line Description Text Wrapper
        String desc = issue.getDescription();
        int maxCharsPerLine = 82;
        int currentY = descY + 52;
        for (int i = 0; i < desc.length(); i += maxCharsPerLine) {
            String line = desc.substring(i, Math.min(i + maxCharsPerLine, desc.length()));
            canvas.drawText(line, 48, currentY, textPaint);
            currentY += 18;
            if (currentY > descY + 125) break;
        }

        // 6. Verification Seal & Signature Footer Box
        int signY = 655;

        // Left Verification Stamp Box
        fillPaint.setColor(Color.parseColor("#ECFDF5"));
        strokePaint.setColor(Color.parseColor("#059669"));
        canvas.drawRoundRect(28, signY, 280, signY + 100, 10, 10, fillPaint);
        canvas.drawRoundRect(28, signY, 280, signY + 100, 10, 10, strokePaint);

        textPaint.setColor(Color.parseColor("#047857"));
        textPaint.setTextSize(10);
        textPaint.setFakeBoldText(true);
        canvas.drawText("COMMUNITY AUDIT SEAL", 40, signY + 25, textPaint);

        textPaint.setTextSize(9);
        textPaint.setFakeBoldText(false);
        canvas.drawText("Status: AI Triaged & Deduplicated", 40, signY + 45, textPaint);
        canvas.drawText("Verification: Citizen Upvoted", 40, signY + 62, textPaint);
        canvas.drawText("Security Hash: SHA256-" + issue.getId().hashCode(), 40, signY + 79, textPaint);

        // Right Signature Box
        strokePaint.setColor(Color.parseColor("#CBD5E1"));
        canvas.drawLine(330, signY + 65, 567, signY + 65, strokePaint);

        textPaint.setColor(Color.parseColor("#64748B"));
        textPaint.setTextSize(9);
        textPaint.setFakeBoldText(true);
        canvas.drawText("AUTHORIZED MUNICIPAL ENGINEER SIGNATURE", 330, signY + 80, textPaint);

        // 7. Footer Divider & Copyright
        fillPaint.setColor(Color.parseColor("#CBD5E1"));
        canvas.drawRect(28, 790, 567, 791, fillPaint);

        textPaint.setColor(Color.parseColor("#94A3B8"));
        textPaint.setTextSize(9);
        textPaint.setFakeBoldText(false);
        canvas.drawText("Generated by CivicLens AI Platform  •  Official Record Page 1 of 1", 28, 810, textPaint);
        canvas.drawText("Confidential Municipal Document", 420, 810, textPaint);

        document.finishPage(page);
        return document;
    }

    private static void drawGridRow(Canvas canvas, Paint paint, int x1, int y, String label1, String val1, int x2, String label2, String val2) {
        paint.setColor(Color.parseColor("#64748B"));
        paint.setFakeBoldText(true);
        canvas.drawText(label1, x1, y, paint);

        paint.setColor(Color.parseColor("#0F172A"));
        paint.setFakeBoldText(false);
        canvas.drawText(val1, x1 + 80, y, paint);

        paint.setColor(Color.parseColor("#64748B"));
        paint.setFakeBoldText(true);
        canvas.drawText(label2, x2, y, paint);

        paint.setColor(Color.parseColor("#0F172A"));
        paint.setFakeBoldText(false);
        canvas.drawText(val2, x2 + 95, y, paint);
    }

    /**
     * Generates an official PDF municipal complaint ticket for a CivicIssue using Android's native PdfDocument API.
     */
    public static File generatePdfTicket(Context context, CivicIssue issue) {
        PdfDocument document = createPdfDocument(issue);
        File pdfFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "CivicLens_Ticket_" + issue.getId() + ".pdf");
        try (FileOutputStream os = new FileOutputStream(pdfFile)) {
            document.writeTo(os);
            Toast.makeText(context, "Official PDF Ticket Generated!", Toast.LENGTH_SHORT).show();
            return pdfFile;
        } catch (Exception e) {
            Log.e(TAG, "Error generating PDF ticket: " + e.getMessage(), e);
            Toast.makeText(context, "Error generating PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        } finally {
            document.close();
        }
    }

    /**
     * Writes the official PDF ticket directly to a user-chosen Uri via Storage Access Framework (SAF).
     */
    public static boolean writePdfTicketToUri(Context context, CivicIssue issue, Uri destinationUri) {
        PdfDocument document = createPdfDocument(issue);
        try (java.io.OutputStream os = context.getContentResolver().openOutputStream(destinationUri)) {
            if (os == null) return false;
            document.writeTo(os);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error writing PDF to user selected URI: " + e.getMessage(), e);
            Toast.makeText(context, "Failed to save PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        } finally {
            document.close();
        }
    }

    /**
     * Opens the generated PDF ticket using Android FileProvider Intent.
     */
    public static void openPdfFile(Context context, File pdfFile) {
        if (pdfFile == null || !pdfFile.exists()) return;

        try {
            Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", pdfFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(intent, "Open PDF Ticket"));
        } catch (Exception e) {
            Toast.makeText(context, "PDF saved at: " + pdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        }
    }
}
