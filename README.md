# How to use

**1 - Build it**

```git clone git://github.com/JoanZap/android-pdfview.git```

```cd android-pdfview```

```mvn clean install```

**2 - Include it in your pom.xml**

```xml
<dependency>
	<groupId>com.joanzapata.pdfview</groupId>
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
OnDrawListener onDrawListener = this;
OnPageChangeListener onPageChangeListener = this;
OnLoadCompleteListener onLoadCompleteListener = this;
pdfView.fromAsset(pdfName)
    .pages(0, 2, 1, 3) // default: Original pages
    .defaultPage(0) // default: 0
    .showMinimap(false) // default: false
    .enableSwipe(true) // default: true
    .onDraw(onDrawListener) // default: null
    .onLoad(onLoadCompleteListener) // default: null
    .onPageChange(onPageChangeListener) // default: null
    .load(); // Start decoding the PDF file
```

