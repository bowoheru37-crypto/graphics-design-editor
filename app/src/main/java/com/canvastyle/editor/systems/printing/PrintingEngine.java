package com.canvastyle.editor.systems.printing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.print.PrintAttributes;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class PrintingEngine {
    
    private final Context context;
    private PrintingCallback callback;
    
    public interface PrintingCallback {
        void onPrintSuccess(String filePath);
        void onPrintError(String error);
        void onPrintProgress(int progress);
    }
    
    public PrintingEngine(Context context) {
        this.context = context;
    }
    
    public void printToPDF(Bitmap bitmap, String fileName) {
        new Thread(() -> {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    PdfDocument document = new PdfDocument();
                    
                    PrintAttributes.MediaSize mediaSize = PrintAttributes.MediaSize.ISO_A4;
                    int pageWidth = mediaSize.getWidthMils() / 1000 * 72;
                    int pageHeight = mediaSize.getHeightMils() / 1000 * 72;
                    
                    PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                            pageWidth, pageHeight, 1).create();
                    
                    PdfDocument.Page page = document.startPage(pageInfo);
                    Canvas canvas = page.getCanvas();
                    
                    canvas.drawBitmap(bitmap, 0, 0, null);
                    document.finishPage(page);
                    
                    File pdfDir = new File(context.getFilesDir(), "PDFs");
                    if (!pdfDir.exists()) {
                        pdfDir.mkdirs();
                    }
                    
                    File pdfFile = new File(pdfDir, fileName + ".pdf");
                    document.writeTo(new FileOutputStream(pdfFile));
                    document.close();
                    
                    if (callback != null) {
                        callback.onPrintSuccess(pdfFile.getAbsolutePath());
                    }
                }
            } catch (IOException e) {
                if (callback != null) {
                    callback.onPrintError("PDF generation failed: " + e.getMessage());
                }
            }
        }).start();
    }
    
    public void printToImage(Bitmap bitmap, String fileName, Bitmap.CompressFormat format) {
        new Thread(() -> {
            try {
                File imageDir = new File(context.getFilesDir(), "Prints");
                if (!imageDir.exists()) {
                    imageDir.mkdirs();
                }
                
                String extension = format == Bitmap.CompressFormat.PNG ? ".png" : ".jpg";
                File imageFile = new File(imageDir, fileName + extension);
                
                FileOutputStream fos = new FileOutputStream(imageFile);
                bitmap.compress(format, 100, fos);
                fos.close();
                
                if (callback != null) {
                    callback.onPrintSuccess(imageFile.getAbsolutePath());
                }
            } catch (IOException e) {
                if (callback != null) {
                    callback.onPrintError("Image printing failed: " + e.getMessage());
                }
            }
        }).start();
    }
    
    public void printMultiPage(List<Bitmap> bitmaps, String fileName) {
        new Thread(() -> {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    PdfDocument document = new PdfDocument();
                    
                    for (int i = 0; i < bitmaps.size(); i++) {
                        Bitmap bitmap = bitmaps.get(i);
                        PrintAttributes.MediaSize mediaSize = PrintAttributes.MediaSize.ISO_A4;
                        int pageWidth = mediaSize.getWidthMils() / 1000 * 72;
                        int pageHeight = mediaSize.getHeightMils() / 1000 * 72;
                        
                        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                                pageWidth, pageHeight, i + 1).create();
                        
                        PdfDocument.Page page = document.startPage(pageInfo);
                        Canvas canvas = page.getCanvas();
                        canvas.drawBitmap(bitmap, 0, 0, null);
                        document.finishPage(page);
                        
                        if (callback != null) {
                            callback.onPrintProgress((i + 1) * 100 / bitmaps.size());
                        }
                    }
                    
                    File pdfDir = new File(context.getFilesDir(), "PDFs");
                    if (!pdfDir.exists()) {
                        pdfDir.mkdirs();
                    }
                    
                    File pdfFile = new File(pdfDir, fileName + ".pdf");
                    document.writeTo(new FileOutputStream(pdfFile));
                    document.close();
                    
                    if (callback != null) {
                        callback.onPrintSuccess(pdfFile.getAbsolutePath());
                    }
                }
            } catch (IOException e) {
                if (callback != null) {
                    callback.onPrintError("Multi-page printing failed: " + e.getMessage());
                }
            }
        }).start();
    }
    
    public void setCallback(PrintingCallback callback) {
        this.callback = callback;
    }
}
