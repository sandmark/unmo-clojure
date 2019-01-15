(defproject unmo "0.1.0-SNAPSHOT"
  :description "A japanese legacy chatbot using Sudachi, a morphological analyser for modern era."
  :url "https://github.com/sandmark/unmo-clojure/"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :repositories [["Sonatype" "https://oss.sonatype.org/content/repositories/snapshots"]]
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [bigml/sampling "3.2"]
                 [fipp "0.6.14"]
                 [com.worksap.nlp/sudachi "0.1.1-SNAPSHOT"]]
  :plugins [[lein-environ "1.1.0"]
            [lein-eftest "0.5.3"]
            [lein-auto "0.1.3"]]
  :main ^:skip-aot unmo.core
  :target-path "target/%s"
  :profiles {:dev {:dependencies [[alembic "0.3.2"]
                                  [eftest "0.5.3"]]}
             :uberjar {:aot :all}})
