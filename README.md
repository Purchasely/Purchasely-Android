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
implementation 'io.purchasely:purchasely:0.2.3'
```

### Initialize the SDK

Iniatialize sdk when you want to have access to products, purchases or restoration. We advise you to do it as soon as your application start, our sdk is very light and all initialization process is done on another thread.

```kotlin
import io.purchasely.public.Purchasely

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

### Notify when the app is ready

The SDK needs to display messages above your UI. It can be the continuation of a purchase started on the App Store, the result from a notification linking to our product, …

Your app needs to tell Purchasely SDK when it is ready to be covered by our UI.

This is done to handle cases like:
* a loading screen that dismisses upon completion
* an on boarding that needs to be displayed before purchasing
* a subscribe process mandatory for app usage

When your app is ready, call the following method and the SDK will handle the continuation of whatever was in progress (purchase, push message, …)

```swift
Purchasely.isReadyToPurchase(true)
```

You can set it back to false when the app goes in the background when you have a screen that blocks UI in background mode and that is dismissed when the app is in foreground (like in banking apps).


### Present products

Purchasely handles all the presentation logic of your products configured in the back office.
You can ask for the SDK to give you the `UIViewController` presenting the purchase by calling the following :

```swift
// Show an activity indicator while the offer is being loaded
Purchasely.productController(for: "my_product_id", success: { [weak self](controller) in
	self?.present(controller, animated: true, completion: nil)
}, failure: { _ in
	// Display error and replace by your own fallback page ?
})
```

You can be alerted if the purchase was made by listening to the [Notifications](#notifications)

## ↕️ Choose how you want the offers to be presented

Once the `isReadyToPurchase` is set the SDK can pop windows above your UI, it can be Alerts, Product pages …

You might want to override them of chose to display them in another way. For example a side bar with the offer on iPad.
You can override the default behaviors using the `PLYUIDelegate`.

```swift
Purchasely.setUIDelegate(self)
```

To change the transition, size, position … of a presented controller (`PLYUIControllerType` gives you the type of controller displayed):
```
func display(controller: UIViewController, type: PLYUIControllerType)
```

## 📈 Integrate In App events to your analytics system

Purchasely tracks every action perfomed but you might also wish to insert these events to your own tracking system.
You can receive the events (`PLYEvent`) by setting yourself as a delegate (`PLYEventDelegate`), either from the `start` method:

```swift
Purchasely.start(withAPIKey: "API_KEY", eventDelegate: self)
```

or later 

```swift
Purchasely.setEventDelegate(self)
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

The SDK offers a way to display these using a standard `UIAlertController` message with a single `Ok` button to dismiss.

If you wish to offer a nicer way to display error messages, a way that reflects more your app, you can override by setting yourself as the delegate (`PLYUIDelegate`) you will then be responsible for displaying the messages yourself.

```swift
Purchasely.setUIDelegate(self)
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

Once the purchase is made to Apple Servers, registered in our systems, Purchasely sends a local `Notification` in the `NotificationCenter`.
You can use it to unlock the content or refresh it.

You can catch it like this

```swift
NotificationCenter.default.addObserver(self, selector: #selector(reloadContent(_:)), name: .ply_purchasedSubscription, object: nil)
```

And use it like that

```swift
@objc func reloadContent(_ notification: Notification) {
	// Reload the content
}
```

For example, this can be done in every controller that displays premium content. That way you won't have to reload the content each time the controller is displayed unless a payment was made

## 🎆 Promote your product

### … inside your app

Now everything is ready, you will want to advertise your In App Purchases from within your app to convert your users.
You migh want to create banners, splash screens, … but doing it right is complex:
* Your product is delivered in more than 150 countries and several currencies
* Prices can change from store to store, this is not an equivalent, you can set different prices by country (cheaper in 🇫🇷, more expensive in 🇬🇧) and Apple changes its price grid regularly to fit rate or taxes changes
* You must take into account the Locale of the user to place the currency at the right spot so that the user feels safe …
* A phone with a `en-US` Locale doesn't mean the user has a US App Store account. You need to interrogate the App Store to get the user price, currency, …
* You must take into account introductory price information and display the promotion correctly ($10 / month during 3 months ). Remember periods can be weeks, months, … but even 3 days, 2 weeks and more when your intro pricing is free.

We already did that job to display your products and plans and we know it is tough, so please don't try to hardcode the pricings, periods, …
Instead you can use the services we have exposed to display the pricing.

First you need to select which Plan of a product you want to expose (cheapest one ? most used ?), then you can proceed as following :

```swift
Purchasely.plan(with: "MONTHLY",
				success: { (plan) in
					// Get the regular price like "$1.99 / month"
					guard let price = plan.localizedFullPrice else { return }

					// In case there is an active promotion we display it followed by the regular price
					// for example: "$0.99 / week during 2 weeks then $1.99 / month"
					if plan.hasIntroductoryPrice,
						let introPrice = plan.localizedFullIntroductoryPrice,
						let introDuration = plan.localizedIntroductoryDuration {
						self.priceLabel.text = "\(introPrice) during \(introDuration) then \(price)"
					} else {
						self.priceLabel.text = price
					}
},
				failure: { (error) in
					// Hide advertising
})
```
## 🤕 Troubleshooting

Having troubles ?
You might find some answers by changing the log level :

```swift
Purchasely.start(withAPIKey: "API_KEY", logLevel: .debug)
```

or 

```swift
Purchasely.setLogLevel(.debug)
```

## 👀 Example

You can run the java sample or kotlin sample.

## Author

Purchasely SAS

## License

[Custom](https://www.purchasely.io)
