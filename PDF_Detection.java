import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.highgui.HighGui;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PDFSignatureDetection {

    static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    public static void main(String[] args) throws IOException {

        String pdfPath = "path_to_your_pdf.pdf";

        File file = new File(pdfPath);
        if (!file.exists()) {
            System.err.println("PDF not found: " + pdfPath);
            return;
        }

        PDDocument document = PDDocument.load(file);
        PDFRenderer renderer = new PDFRenderer(document);

        for (int pageNum = 0; pageNum < document.getNumberOfPages(); pageNum++) {

            // Render PDF page → image
            BufferedImage bufferedImage = renderer.renderImageWithDPI(pageNum, 300);
            File pageImageFile = new File("page_" + pageNum + ".png");
            ImageIO.write(bufferedImage, "png", pageImageFile);

            // Load into OpenCV
            Mat image = Imgcodecs.imread(pageImageFile.getAbsolutePath(), Imgcodecs.IMREAD_GRAYSCALE);
            if (image.empty()) {
                System.err.println("Failed to load rendered page image.");
                continue;
            }

            // Preprocessing
            Mat blurred = new Mat();
            Imgproc.GaussianBlur(image, blurred, new Size(5, 5), 0);

            Mat thresh = new Mat();
            Imgproc.adaptiveThreshold(
                    blurred, thresh, 255,
                    Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                    Imgproc.THRESH_BINARY_INV,
                    11, 2
            );

            Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
            Mat morph = new Mat();
            Imgproc.morphologyEx(thresh, morph, Imgproc.MORPH_CLOSE, kernel, new Point(-1, -1), 2);

            // Connected components
            Mat labels = new Mat();
            Mat stats = new Mat();
            Mat centroids = new Mat();

            int numComponents = Imgproc.connectedComponentsWithStats(
                    morph, labels, stats
