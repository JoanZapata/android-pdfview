![Screenshot of the sample app](https://raw.github.com/JoanZapata/android-pdfview/master/device.png)

**Android PDFView** is a library which provides a fast PDFView component for Android, with ```animations```, ```gestures```, and ```zoom```.

# Get it

Android PDFView is **available in Maven Central**.

```xml
<dependency>
	<groupId>com.joanzapata.pdfview</groupId>
	<artifactId>android-pdfview</artifactId>
	<version>1.0.0</version>
	<type>apklib</type>
</dependency>
```

# Include PDFView in your layout

```xml
<com.joanzapata.pdfview.PDFView
        android:id="@+id/pdfview"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>
```

# Load a PDF file

```java
OnDrawListener onDrawListener = this;
OnPageChangeListener onPageChangeListener = this;
OnLoadCompleteListener onLoadCompleteListener = this;
pdfView.fromAsset(pdfName)
    .pages(0, 2, 1, 3, 3, 3)
    .defaultPage(1)
    .showMinimap(false)
    .enableSwipe(true)
    .onDraw(onDrawListener)
    .onLoad(onLoadCompleteListener)
    .onPageChange(onPageChangeListener)
    .load();
```

* ```pages``` is optional, it allows you to filter and order the pages of the PDF as you need
* ```onDraw``` is also optional, and allows you to draw something on a provided canvas, above the current page

# License

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
