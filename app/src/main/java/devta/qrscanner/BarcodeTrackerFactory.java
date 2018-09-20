package devta.qrscanner;

import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;

/**
 * author @Divyanshu Tayal
 */
class BarcodeTrackerFactory implements MultiProcessor.Factory<Barcode> {
    private GraphicOverlay mGraphicOverlay;

    BarcodeTrackerFactory(GraphicOverlay graphicOverlay) {
        mGraphicOverlay = graphicOverlay;
    }

    @Override
    public Tracker<Barcode> create(Barcode barcode) {
        BarcodeGraphic graphic = new BarcodeGraphic(mGraphicOverlay);
        return new GraphicTracker<>(mGraphicOverlay, graphic);
    }
}

