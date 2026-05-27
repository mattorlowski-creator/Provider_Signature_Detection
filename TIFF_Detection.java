import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.highgui.HighGui;

public class SignatureDetection {

    static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    public static void main(String[] args) {

        String inputPath = "path_to_your_image.tiff";
        String outputPath = "output_image.tiff";

        // Load the TIFF image
        Mat image = Imgcodecs.imread(inputPath, Imgcodecs.IMREAD_GRAYSCALE);
        if (image.empty()) {
            System.err.println("Could not load image: " + inputPath);
            return;
        }

        // Preprocess the image
        Mat blurred = new Mat();
        Imgproc.GaussianBlur(image, blurred, new Size(5, 5), 0);

        // Adaptive threshold
        Mat thresh = new Mat();
        Imgproc.adaptiveThreshold(
                blurred, thresh, 255,
                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                Imgproc.THRESH_BINARY_INV,
                11, 2
        );

        // Morphological cleanup
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Mat morph = new Mat();
        Imgproc.morphologyEx(thresh, morph, Imgproc.MORPH_CLOSE, kernel, new Point(-1, -1), 2);

        // Connected components
        Mat labels = new Mat();
        Mat stats = new Mat();
        Mat centroids = new Mat();

        int numComponents = Imgproc.connectedComponentsWithStats(
                morph, labels, stats, centroids, Imgproc.CV_32S
        );

        // Convert original grayscale to color for drawing
        Mat output = new Mat();
        Imgproc.cvtColor(image, output, Imgproc.COLOR_GRAY2BGR);

        // Loop through components (skip background at index 0)
        for (int i = 1; i < numComponents; i++) {
            int area = (int) stats.get(i, Imgproc.CC_STAT_AREA)[0];

            if (area > 100) { // adjust threshold as needed
                int x = (int) stats.get(i, Imgproc.CC_STAT_LEFT)[0];
                int y = (int) stats.get(i, Imgproc.CC_STAT_TOP)[0];
                int w = (int) stats.get(i, Imgproc.CC_STAT_WIDTH)[0];
                int h = (int) stats.get(i, Imgproc.CC_STAT_HEIGHT)[0];

                Imgproc.rectangle(output, new Point(x, y), new Point(x + w, y + h),
                        new Scalar(0, 0, 255), 2);
            }
        }

        // Save output
        Imgcodecs.imwrite(outputPath, output);
        System.out.println("Saved output to: " + outputPath);

        // Display
        HighGui.imshow("Detected Signatures", output);
        HighGui.waitKey(0);
        HighGui.destroyAllWindows();
    }
}
