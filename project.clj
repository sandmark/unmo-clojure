(defproject unmo "0.1.0-SNAPSHOT"
  :description "A japanese legacy chatbot using Sudachi, a morphological analyser for modern era."
  :url "https://github.com/sandmark/unmo-clojure/"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.0-RC5"]
                 [environ "1.1.0"]]
  :plugins [[lein-environ "1.1.0"]]
  :main ^:skip-aot unmo.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
