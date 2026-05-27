# Provider_Signature_Detection
Simple java based signature detection for electronic agreement processing

PDF Steps:
1.	Loads PDF document using Apache PDFBox
2.	Converts each page of PDF to image
3.	Load image using OpenCV
4.	Apply Gaussian blur, adaptive threshold, and morphological ops. to preprocess the image
5.	Filters out small components based on area
6.	Draws bounding boxes around detected signatures and saves the output image

TIFF Steps:
1.	Loads TIFF image in grayscale
2.	Applies Gaussian blur
3.	Uses adaptive thresholding to enhance signature regions
4.	Performs morphological operations to remove small noise
5.	Filters out small components based on area
6.	Draws bounding boxes around detected signatures and saves the output image.
