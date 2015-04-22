web: target/universal/stage/bin/tweet-engagement -Dhttp.port=${PORT}
fetchtweets: java -Dconfig.file=conf/application.conf -cp "target/universal/stage/lib/*" jobs.FetchTweets .
