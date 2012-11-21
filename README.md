How to use :

1 - Include PDFView in your layout

```xml
<fr.jzap.pdfview.impl.PDFView
        android:id="@+id/pdfview"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>
```

2 - Load a pdf file and display the first page

```java
final IPDFView pdfView = (IPDFView) findViewById(R.id.pdfview);
Uri fromFile = Uri.parse("file:///mnt/sdcard/appart.pdf");
pdfView.load(fromFile, new OnLoadCompleteListener() {
     public void loadComplete(int arg0) {
	pdfView.showPage(0);
     }
});
pdfView.enableSwipe();
```

