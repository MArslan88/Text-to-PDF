package com.mhdarslan.texttopdf;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.itextpdf.barcodes.BarcodeQRCode;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.SimpleTimeZone;

public class MainActivity extends AppCompatActivity {

    EditText et_name, et_age, et_contact, et_location;
    Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PackageManager.PERMISSION_GRANTED);

        et_name = findViewById(R.id.et_name);
        et_age = findViewById(R.id.et_age);
        et_contact = findViewById(R.id.et_contact);
        et_location = findViewById(R.id.et_location);
        submitButton = findViewById(R.id.submit);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = et_name.getText().toString().trim();
                String age = et_age.getText().toString().trim();
                String contact = et_contact.getText().toString().trim();
                String location = et_location.getText().toString().trim();

                try {
                    createPdf(name,age,contact,location);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void createPdf(String name, String age, String contact, String location) throws FileNotFoundException {
        String pdfPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        File file = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            file = new File(pdfPath, "myPdf"+currentTime()+".pdf");
        }
        OutputStream outputStream = new FileOutputStream(file);

        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdfDocument = new PdfDocument(writer);
        Document document = new Document(pdfDocument);

        // page size
        pdfDocument.setDefaultPageSize(PageSize.A4);
        // margins of page
        document.setMargins(0,0,0,0);

        // image
        Drawable d = getDrawable(R.drawable.cow_farm);
        Bitmap bitmap = ((BitmapDrawable)d).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,stream);
        byte[] bitmapData = stream.toByteArray();

        ImageData imageData = ImageDataFactory.create(bitmapData);
        Image image = new Image(imageData);
        // ---------------------------  Image section complete ----------------------------

        // Text paragraph
        Paragraph visitorTickert = new Paragraph("Visitor Ticket").setBold().setFontSize(24).setTextAlignment(TextAlignment.CENTER);
        Paragraph goup = new Paragraph("Tourism Department\n"+
                "Government of Pakistan").setBold().setFontSize(12).setTextAlignment(TextAlignment.CENTER);
        Paragraph maGroup = new Paragraph("M.A Group").setBold().setFontSize(20).setTextAlignment(TextAlignment.CENTER);

        // --------------------------------- Text section complete ---------------------------

        // draw a table of data

        float[] width = {100f, 100f};
        Table table = new Table(width);
        table.setHorizontalAlignment(HorizontalAlignment.CENTER);

        table.addCell(new Cell().add(new Paragraph("Visitor Name")));
        table.addCell(new Cell().add(new Paragraph(name)));

        table.addCell(new Cell().add(new Paragraph("Age")));
        table.addCell(new Cell().add(new Paragraph(age)));

        table.addCell(new Cell().add(new Paragraph("Mobile No.")));
        table.addCell(new Cell().add(new Paragraph(contact)));

        table.addCell(new Cell().add(new Paragraph("Location")));
        table.addCell(new Cell().add(new Paragraph(location)));

        table.addCell(new Cell().add(new Paragraph("Date:")));
        table.addCell(new Cell().add(new Paragraph(currentDate())));

        table.addCell(new Cell().add(new Paragraph("Time:")));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            table.addCell(new Cell().add(new Paragraph(currentTime())));
        }
        // --------------------------------- Table end ------------------------------------


        // QR Code
        BarcodeQRCode qrCode = new BarcodeQRCode(name+"\n"+age+"\n"+contact+"\n"+location);
        PdfFormXObject qrCodeObject = qrCode.createFormXObject(ColorConstants.BLACK, pdfDocument);
        Image qrCodeImage = new Image(qrCodeObject).setWidth(80).setHorizontalAlignment(HorizontalAlignment.CENTER);
        // --------------  QR Code end -----------------------

        // add all the object in the document
        document.add(image);
        document.add(visitorTickert);
        document.add(goup);
        document.add(maGroup);
        document.add(table);
        document.add(qrCodeImage);

        document.close();
        Toast.makeText(this, "PDF created", Toast.LENGTH_SHORT).show();

    }

    public String currentDate(){
        String myFormat = "MM-dd-yyyy";
        SimpleDateFormat dateFormat = new SimpleDateFormat(myFormat, Locale.US);
        String currentDate = dateFormat.getDateInstance().format(new Date());
        return currentDate;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String currentTime(){
        String myFormat = "HH:mm:ss a";
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(myFormat);
        String currentTime = LocalTime.now().format(timeFormatter).toString();
        return currentTime;
    }
}