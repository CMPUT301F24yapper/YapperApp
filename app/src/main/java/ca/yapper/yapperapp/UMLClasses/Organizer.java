package ca.yapper.yapperapp.UMLClasses;

import java.util.Map;
//import com.google.zxing.BarcodeFormat;
//import com.google.zxing.EncodeHintType;
//import com.google.zxing.MultiFormatWriter;
//import com.google.zxing.NotFoundException;
//import com.google.zxing.WriterException;
//import com.google.zxing.client.j2se.MatrixToImageWriter;
//import com.google.zxing.common.BitMatrix;
//import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class Organizer {
    // example library that can handle QR codes: Zebra Crossing (“ZXing”) library because of its simplicity and popularity
    // steps to create QR code image (from: https://www.geeksforgeeks.org/how-to-generate-and-read-qr-code-with-java-using-zxing-library/)
    // 1. Download the ZXING library (https://jar-download.com/?search_box=%20zxing).
    // 2. Add ZXING dependency in the Maven file.
    // all below attributes used in function to create QR code
    private String qrCodeHashData;
    private String qrCodePath;
    private String  charset;
    private Map hashMap;
    private int height;
    private int width;

//    public Event createEvent() {
//        // method logic to create & return event
//    }

    // Program to generate QR code and save it as a jpg file in the native(?) folder:
//    public static void generateQRCode (String qrCodeHashData, String qrCodePath, String charset, Map hashMap, int height, int width) throws WriterException, IOException {
//        // method logic to generate QRcode
//    }

    public void createFacilityProfile() {
        // method logic
    }

    public void manageFacilityProfile() {
        // method logic
    }
}
