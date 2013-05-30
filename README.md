# How to use

## Build it

```git clone git://github.com/JoanZap/android-pdfview.git```

```cd android-pdfview```

```mvn clean install```

## Include it in your pom.xml

```xml
<dependency>
	<groupId>com.joanzapata.pdfview</groupId>
	<artifactId>android-pdfview</artifactId>
	<version>1.0.0</version>
	<type>apklib</type>
</dependency>
```

## Include PDFView in your layout

```xml
<fr.jzap.pdfview.impl.PDFView
        android:id="@+id/pdfview"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>
```

## Load a pdf file and display the first page

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

## License

```
Copyright 2013 Joan Zapata

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
