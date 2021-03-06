# Project 3 - *SimpleTweet*

**SimpleTweet** is an android app that allows a user to view their Twitter timeline and post a new tweet. The app utilizes [Twitter REST API](https://dev.twitter.com/rest/public).

Time spent: **30** hours spent in total

## User Stories

The following **required** functionality is completed:

* [x]	User can **sign in to Twitter** using OAuth login
* [x]	User can **view tweets from their home timeline**
  * [x] User is displayed the username, name, and body for each tweet
  * [x] User is displayed the [relative timestamp](https://gist.github.com/nesquena/f786232f5ef72f6e10a7) for each tweet "8m", "7h"
* [x] User can **compose and post a new tweet**
  * [x] User can click a “Compose” icon in the Action Bar on the top right
  * [x] User can then enter a new tweet and post this to twitter
  * [x] User is taken back to home timeline with **new tweet visible** in timeline
  * [x] Newly created tweet should be manually inserted into the timeline and not rely on a full refresh
* [x] User can **see a counter with total number of characters left for tweet** on compose tweet page
* [x] User can **pull down to refresh tweets timeline**
* [x] User can **see embedded image media within a tweet** on list or detail view.

The following **stretch** features are implemented:

* [x] User is using **"Twitter branded" colors and styles**
* [x] User sees an **indeterminate progress indicator** when any background or network task is happening
* [x] User can **select "reply" from detail view to respond to a tweet**
  * [x] User that wrote the original tweet is **automatically "@" replied in compose**
* [x] User can tap a tweet to **open a detailed tweet view**
  * [x] User can **take favorite (and unfavorite) or reweet** actions on a tweet
* [x] User can view more tweets as they scroll with infinite pagination
* [x] Compose tweet functionality is build using modal overlay
* [x] User can **click a link within a tweet body** on tweet details view. The click will launch the web browser with relevant page opened.
* [x] Use Parcelable instead of Serializable using the popular [Parceler library](http://guides.codepath.org/android/Using-Parceler).
* [x] Replace all icon drawables and other static image assets with [vector drawables](http://guides.codepath.org/android/Drawables#vector-drawables) where appropriate.
* [x] User can view following / followers list through any profile they view.
* [x] Use the View Binding library to reduce view boilerplate.
* [x] On the Twitter timeline, leverage the [CoordinatorLayout](http://guides.codepath.org/android/Handling-Scrolls-with-CoordinatorLayout#responding-to-scroll-events) to apply scrolling behavior that [hides / shows the toolbar](http://guides.codepath.org/android/Using-the-App-ToolBar#reacting-to-scroll).
* [x] User can **open the twitter app offline and see last loaded tweets**. Persisted in SQLite tweets are refreshed on every application launch. While "live data" is displayed when app can get it from Twitter API, it is also saved for use in offline mode.

The following **additional** features are implemented:

* [x] Users can favorite/unfavorite and retweet/unretweet from the main timeline
* [x] Users can see which of their followers retweeted a Tweet from the main timeline
* [x] Users can press the home button to scroll back to the top
* [x] Users can see user-specific timelines on their profile page
* [x] Users can tap profile pictures or items in a followers/following tab to view the selected user's profile page
* [x] Users can tap whoever retweeted a Tweet on their feed to see the retweeter's profile page
* [x] Users can tap image previews to pull up the full image
* [x] Replying to tweets and composing tweets are modal dialogs
* [x] Snackbar appears whenever a user composes or replies to a tweet, allowing them to view their new Tweet immediately
* [x] Users can follow and unfollow other users on their profile page or through their followers/following tabs

## Video Walkthrough

Here's some walkthroughs of implemented user stories:

Endless scrolling of Tweets. Click images to see them larger. View user's profile page to see their own user timeline and their followers/following.

![App Demo Feed Link](walkthroughs/walkthroughtweet.gif)

Compose Tweets and reply to Tweets with modal dialog options. See how many characters you have remaining.

![App Demo Tweet Link](walkthroughs/walkthroughsending.gif)

Scroll up to refresh your feed. Click links to see them. Latest tweets persists even when there is no connection.

![App Demo Refresh Link](walkthroughs/walkthroughlogout.gif)

GIF created with [LiceCap](http://www.cockos.com/licecap/).

## Notes

Ran into rate-exceeded limits a lot, so had to create another Twitter account to get past that. Learned a lot about selectors for creating custom buttons and text click actions. Learned a lot about tab layouts.

## Open-source libraries used

- [Android Async HTTP](https://github.com/loopj/android-async-http) - Simple asynchronous HTTP requests with JSON parsing
- [Glide](https://github.com/bumptech/glide) - Image loading and caching library for Android

## License

    Copyright 2020 Emily Park

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
