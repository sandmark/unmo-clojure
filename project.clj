(defproject unmo "0.1.2"
  :description "Unmo: A simple japanese chatbot."
  :url "https://github.com/sandmark/unmo-clojure/"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [fipp "0.6.21"]
                 [sudachi-clj "0.1.0-SNAPSHOT"]]
  :plugins [[lein-eftest "0.5.3"]
            [lein-auto "0.1.3"]]
  :main ^:skip-aot unmo.core
  :target-path "target/%s"
  :profiles {:dev     {:dependencies [[eftest "0.5.9"]
                                      [fudje  "0.9.7"]]}
             :uberjar {:aot :all}})
