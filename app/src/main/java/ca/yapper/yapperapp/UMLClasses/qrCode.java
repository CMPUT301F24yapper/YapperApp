package ca.yapper.yapperapp.UMLClasses;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
/**
 * The qrCode class represents a QR code associated with an event.
 * It generates a QR code based on a unique identifier for the event and converts it into a bitmap.
 */
public class qrCode {

    private String QRCodeValue;
    private int hashData;
    private QRCodeWriter QRcodeManager;
    private BitMatrix qrCode;

    /**
     * Generates a QR Code as a BitMatrix using the currently stored string and stores both the
     * resulting bitMatrix and hash value.
     * */
    public qrCode(String QRCodeValue) throws WriterException {
        this.QRCodeValue = QRCodeValue;
        this.QRcodeManager = new QRCodeWriter();
        this.qrCode = QRcodeManager.encode(QRCodeValue, BarcodeFormat.QR_CODE, 500, 500);
        this.hashData = qrCode.hashCode();
    }

    /**
     * Takes a generated QR code BitMatrix and converts it into a Bitmap for use in ImageViews.
     **/
    public Bitmap convertToIMG() throws UnsupportedOperationException{
        if (this.qrCode.getHeight() != 500 || this.qrCode.getWidth() != 500){
            return null;
        }
        // Bitmap.Config.ARGB_8888 is color config
        Bitmap QRImg = Bitmap.createBitmap(this.qrCode.getHeight(), this.qrCode.getWidth(), Bitmap.Config.ARGB_8888);

        for (int i = 0; i < this.qrCode.getHeight(); i++) {
            for (int j = 0; j < this.qrCode.getWidth(); j++) {
                Boolean current = this.qrCode.get(i,j);
                if (current) {QRImg.setPixel(i, j, 0xFF000000);} // set pixel black
                else         {QRImg.setPixel(i, j, 0xFFFFFFFF);} // set pixel white
        }}
        return QRImg;
    }

    public String getQRCodeValue() {
        return QRCodeValue;
    }

    public void setQRCodeValue(String QRCodeValue) {
        this.QRCodeValue = QRCodeValue;
    }

    public int getHashData() {
        return hashData;
    }

    public void setHashData(int hashData) {
        this.hashData = hashData;
    }

    public BitMatrix getQrCode() {
        return qrCode;
    }

    public void setQrCode(BitMatrix qrCode) {
        this.qrCode = qrCode;
    }
}
