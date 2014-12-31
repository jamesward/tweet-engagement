Tweet Engagement
================


Setup:

1. Create a Connected App on Salesforce
1. Get a Salesforce Access Token and append it to your Salesforce Password for the `SALESFORCE_PASSWORD` in the next step
1. Set environment vars for: `SALESFORCE_CONSUMER_KEY` `SALESFORCE_CONSUMER_SECRET` `SALESFORCE_USERNAME` `SALESFORCE_PASSWORD`
1. Create a Twitter API app
1. Set environment vars for: `TWITTER_CONSUMER_KEY` `TWITTER_CONSUMER_SECRET`

Run the tests:

    ./activator test

Run the app locally:

    ./activator "run-main jobs.FetchTweets"

Run the app on Heroku:

1. Create a new Heroku app: `heroku create`
1. Deploy the app: `git push heroku master`
1. Set the config vars: `SALESFORCE_CONSUMER_KEY` `SALESFORCE_CONSUMER_SECRET` `SALESFORCE_USERNAME` `SALESFORCE_PASSWORD` `TWITTER_CONSUMER_KEY` `TWITTER_CONSUMER_SECRET`
1. Run the app once: `heroku run fetchtweets`