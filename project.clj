(defproject unmo "0.1.1"
  :description "Unmo: A simple japanese chatbot."
  :url "https://github.com/sandmark/unmo-clojure/"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :repositories [["Sonatype" "https://oss.sonatype.org/content/repositories/snapshots"]]
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [bigml/sampling "3.2"]
                 [fipp "0.6.21"]
                 [com.worksap.nlp/sudachi "0.3.0"]]
  :plugins [[lein-eftest "0.5.3"]
            [lein-auto "0.1.3"]]
  :main ^:skip-aot unmo.core
  :target-path "target/%s"
  :profiles {:dev     {:dependencies [[eftest "0.5.9"]
                                      [fudje  "0.9.7"]]}
             :uberjar {:aot :all}})
