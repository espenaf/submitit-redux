(defproject submitit "1.0.SOS"
  :description "FIXME: write description"
  :repositories [["central-proxy" "http://repository.sonatype.org/content/repositories/central/"]]
  :dependencies [
  	[org.clojure/clojure "1.5.1"]
  	[org.apache.commons/commons-email "1.2"]
    [commons-io "2.4"]
    [commons-codec "1.7"]
    [ring "1.3.2"]
    [ring/ring-core "1.3.2"]
    [compojure "1.1.5"]
    [hiccup "1.0.5"]
  	[ring-middleware-format "0.1.1"]
    [cheshire "5.2.0"]
    [clj-http "0.6.3"]
    [org.clojure/data.codec "0.1.0"]
    [com.google.code.maven-play-plugin.org.playframework/jj-simplecaptcha "1.1"]
    [org.jsoup/jsoup "1.7.2"]
    [net.hamnaberg.rest/collection-json-clj "0.1.0"]
    [com.taoensso/timbre "3.0.0-RC2"]
  	]
  :dev-dependencies [[lein-ring "0.4.3"]]
  :main submitit.pages
  :aot [submitit.pages]
  :plugins [[lein-ring "0.8.2"]]
  )

