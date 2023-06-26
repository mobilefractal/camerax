Camerax
=====================
[![Release][jitpack-svg]][jitpack-link]

This library use [CameraX](https://developer.android.com/training/camerax) API

## How to use

1) Add the Jitpack repository to your project:
```groovy
          repositories {
              maven { url "https://jitpack.io" }
          }
```
2) Add a dependency on the library:
```groovy
          implementation 'com.github.mobilefractal:camerax:1.6.2'
```
3) Sets Java compatibility to Java 8:
```groovy
        android {
            ....
            compileOptions {
                setTargetCompatibility(1.8)
                setSourceCompatibility(1.8)
            }

        }
```

5) Start Camera in app:
```kotlin
        XCamera.instance.startCamera(
            this, object : XCamera.CameraListener {
                override fun onSuccess(path: String?) {
                }

                override fun onFailure(msg: String?) {
                }
            },
            isEnableSound = true,
            captions = listOf(
                Caption("Caption 1"),
                Caption("Caption 2"),
                Caption("Caption 3")
            )
        )
```

Your captured image is saved in **Android/media/{applicationId}/{imagePath}/yyyy-MM-dd-HH-mm-ss-SSS.png**

## Contact
- **Email**: mobilefractal1@gmail.com

[jitpack-svg]: https://jitpack.io/v/mobilefractal/camerax.svg
[jitpack-link]: https://jitpack.io/#mobilefractal/camerax
