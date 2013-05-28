# How to use

**1 - Build it**

```git clone git://github.com/JoanZap/android-pdfview.git```

```cd android-pdfview```

```mvn clean install```

**2 - Include it in your pom.xml**

```xml
<dependency>
	<groupId>fr.jzap.pdfview</groupId>
	<artifactId>android-pdfview</artifactId>
	<version>1.0.0</version>
	<type>apklib</type>
</dependency>
```

**3 - Include PDFView in your layout**

```xml
<fr.jzap.pdfview.impl.PDFView
        android:id="@+id/pdfview"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>
```

**4 - Load a pdf file and display the first page**

```java
final IPDFView pdfView = (IPDFView) findViewById(R.id.pdfview);
Uri fromFile = Uri.parse("file:///mnt/sdcard/sample.pdf");
pdfView.enableSwipe();
pdfView.load(fromFile, new OnLoadCompleteListener() {
     public void loadComplete(int arg0) {
	pdfView.showPage(0);
     }
});
```

