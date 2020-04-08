# Purchasely-Android

## Features

|   | Purchasely |
| --- | --- |
🔥 | In App purchase in your app using 6 lines of code (really !)
🌎 | Multi language supported (17 supported languages) and overrides possible 
✨ | Product presentation pages ready to display on Android Smartphones, Tablets and TV and fully customizable from the admin site
📱 | Android support
✅ | Server-side receipt validation
🔔 | Receive user subscription status events with server-to-server notifications (Webhooks) including events like new purchase, renewal, cancellation, billing issue, …
📊 | Analytics available in our dashboard (conversion rate, MRR, …) and sent by our SDK to the app for dispatching to your custom analytics tracking system
🕐 | Detailed user activity (viewed product, subscribed, churned, …)

## ✅ Requirements

- Android 5+

## 🏁 Quick start

### Installation

#### Maven

Add our maven repository url to your project build.gradle file
```groovy
allprojects {
    repositories {
        google()
        jcenter()
        maven { url  "https://dl.bintray.com/purchasely/Purchasely" } //Add this line
    }
}
```

Add the sdk to your dependencies
```groovy
implementation 'io.purchasely:purchasely:0.2.4'
```

### Initialize the SDK

Iniatialize sdk when you want to have access to products, purchases or restoration. We advise you to do it as soon as your application start, our sdk is very light and all initialization process is done on another thread.

```kotlin
import io.purchasely.ext.Purchasely

Purchasely.start(applicationContext, "API_KEY", "USER_ID", eventListener, uiListener)
```

The `userId` parameter is optional and allows you to associate the purchase to a user instead of a device (see next)

The `eventListener` parameter is optional and allows you to listen to all purchase events. You should implement it at least to know when the purchase is successfull.

The `uiListener` parameter is optional and allows you to override UI dialog presented to user in case of error or success.

### Setup User Id

Once your user is logged in and you can send us a userId, please do it otherwise the purchase will be tied to the device and your user won't be able to enjoy from another device.
Setting it will allow you to tie a purchase to a user to use it on other devices.

```kotlin
Purchasely.userId = "123456789"
```

To remove the user (logged out) you can perform a :

```kotlin
Purchasely.userId = null
```

Or in Java
```java
Purchasey.setUserId("null");
```

### Present products

Purchasely handles all the presentation logic of your products configured in the back office.
You can ask for the SDK to give you the `androidx.fragment.app.Fragment` presenting the purchase by calling the following :

```kotlin
Purchasely.displayProduct(/*Your Product id*/ "",
    success = { fragment ->
	supportFragmentManager.beginTransaction()
	    .addToBackStack(null)
	    .replace(R.id.inappFragment, fragment, "InAppFragment")
	    .commitAllowingStateLoss()

	progressBar.isVisible = false
    },
    failure = { error ->
	Log.e("Product", "Error", error)
	Snackbar.make(window.decorView, error.message ?: "error", Snackbar.LENGTH_SHORT).show()
    }
)
```
A coroutine version also exists `suspend fun displayProduct(productId: String) : Fragment?`

Or in Java

```java
Purchasely.displayProduct("YOUR_PRODUCT_ID", new DisplayProductListener() {
    @Override
    public void onFailure(@NotNull Throwable throwable) {
	Log.e("Product", "Error", throwable);
	String message = "error";
	if(throwable.getMessage() != null) {
	    message = throwable.getMessage();
	}
	Snackbar.make(getWindow().getDecorView(), message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onSuccess(@NotNull Fragment fragment) {
	getSupportFragmentManager().beginTransaction()
		.addToBackStack(null)
		.replace(R.id.inappFragment, fragment, "InAppFragment")
		.commitAllowingStateLoss();

	findViewById(R.id.progressBar).setVisibility(View.GONE);
    }
});
```

You can place the fragment wherever your want in another fragment or activity.

You can be alerted if the purchase was made by listening to the LiveData `livePurchase()`

## 📈 Integrate In App events to your analytics system

Purchasely tracks every action perfomed but you might also wish to insert these events to your own tracking system.
You can receive the events (`PLYEvent`) by setting yourself as a listener (`EventListener`), either from the `start` method:

```kotlin
Purchasely.start(applicationContext, "API_KEY", eventListener = myEventListener)
```

or later 

```kotlin
Purchasely.eventListener = myEventListener
```

## 🚨 Custom error and alert views

Some information messages are also displayed to the user during the purchase life cycle like :
- Purchase completed
- Restoration completed

These alerts are listed in `PLYAlertMessage` enum. 

Many errors can occure during the purchase process and are embedded in these messages liek:
- Network error
- Product not found
- Purchase impossible (or canceled)
- Restoration incomplete
- …
These errors are listed in `PLYError` object and translated in the supported languages of the SDK.

The SDK offers a way to display these using a standard `AlertDialog` message with a single `Ok` button to dismiss.

If you wish to offer a nicer way to display error messages, a way that reflects more your app, you can override by setting yourself as the listener (`UIListener`) you will then be responsible for displaying the messages yourself.

```kotlin
Purchasely.uiListener = object: UIListener {
    override fun onAlert(alert: PLYAlertMessage) {
	when(alert) {
	    PLYAlertMessage.InAppSuccess -> displaySuccessDialog(alert)
	    PLYAlertMessage.InAppSuccessUnauthentified -> displaySuccessDialog(alert)
	    is PLYAlertMessage.InAppError -> displayErrorDialog(alert)
	}
    }
}
```

Or in Java

```java
Purchasely.setUiListener(new UIListener() {
    @Override
    public void onAlert(@NotNull PLYAlertMessage alert) {
	if(alert instanceof PLYAlertMessage.InAppSuccess) {
	    //TODO display success view
	}
    }
});
```

That way you could also override the behaviour and trigger some specific actions when the user taps on the button for example.


## 🌍 Supported languages and override messages

The SDK displays some text directly to the user (error messages, restore or login button text, …). These texts are translated in 17 languages:
* English
* French
* German
* Spanish
* Portuguese
* Italian
* Czech
* Polish
* Greek
* Chinese (Simplifed and traditional)
* Japanese
* Russian
* Turkish
* Swedish
* Korean
* Indonesian

That means that every error message and UI element will automatically translated in the user device language (if matching). 

If you want to change the tone of the messages, you can override our translations and set yours.
To do so, you just need to set the key and value corresponding to the message you want to change in your own `strings.xml` file.


## 🔓 Unlock content / service once a purchase is made

Once the purchase is made to Google Servers, registered in our systems, Purchasely sends a LiveData.
You can use it to unlock the content or refresh it.

You can catch it like this

```kotlin
Purchasely.livePurchase().observe(this, Observer {
    Log.d("Purchasely", "User purchased $it")
    Snackbar.make(recyclerView, "Purchased ${it?.vendorId}", Snackbar.LENGTH_SHORT).show()
})
```

Java

```java
Purchasely.livePurchase().observe(this, product -> {
    Log.d("Purchasely", "User purchased " + product);
    Snackbar.make(findViewById(R.id.recyclerView), "Purchased " + product.getVendorId(), Snackbar.LENGTH_SHORT).show();
});
```

For example, this can be done in every controller that displays premium content. That way you won't have to reload the content each time the controller is displayed unless a payment was made

## 🎆 Promote your product

Now everything is ready, you will want to advertise your In App Purchases from within your app to convert your users.
You migh want to create banners, splash screens, … but doing it right is complex:
* Your product is delivered in more than 150 countries and several currencies
* Prices can change from store to store, this is not an equivalent, you can set different prices by country (cheaper in 🇫🇷, more expensive in 🇬🇧) and Google changes its price grid regularly to fit rate or taxes changes
* You must take into account the Locale of the user to place the currency at the right spot so that the user feels safe …
* A phone with a `en-US` Locale doesn't mean the user has a US Play Store account. You need to interrogate the Google Play Store to get the user price, currency, …
* You must take into account introductory price information and display the promotion correctly ($10 / month during 3 months ). Remember periods can be weeks, months, … but even 3 days, 2 weeks and more when your intro pricing is free.

We already did that job to display your products and plans and we know it is tough, so please don't try to hardcode the pricings, periods, …
Instead you can use the services we have exposed to display the pricing.

```kotlin
Purchasely.getProducts(
    onSuccess = { products ->
    	//get all plans with pricing info
	val plans = products.flatMap { it.plans })
    },
    onError = {
	Toast.makeText(applicationContext, "Error ${it.message}", Toast.LENGTH_SHORT).show()
    }
)
```

Java

```java
Purchasely.getProducts(new ProductsListener() {
    @Override
    public void onSuccess(@NotNull List<PLYProduct> list) {
    	//list contains all your product, filter with the product id you want
    	//get all plans with pricing info
	List<PLYPlan> plans = new ArrayList<>();
	for(int i = 0; i < list.size(); i++) {
	    plans.addAll(list.get(i).getPlans());
	}
    }

    @Override
    public void onFailure(@NotNull Throwable throwable) {
	Toast.makeText(getApplicationContext(), "Error " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
    }
});
```

## 🔗 Open Link
You may decide to add some links to you product in the presentation. When the user clicks on it, an activity webview is open.
You may choose to not do that, in such case, do not add the Webview Acivivity to the manifest. Instead, listen to the PLYEvent `LinkOpened`, it contains the url to open.

If you want the sdk to open the link in a webview, add the activity to your manfiest.

```xml
<!-- Purchasely WebView -->
<activity
    android:name="io.purchasely.views.PLYWebViewActivity"
    android:theme="@style/Theme.AppCompat.Light.NoActionBar" />
```

## 🤕 Troubleshooting

Having troubles ?
You might find some answers by changing the log level :

```kotlin
Purchasely.logLevel = LogLevel.Debug
```

## 👀 Example

You can run the java sample or kotlin sample.

## Author

Purchasely SAS

## License

[Custom](https://www.purchasely.io)
