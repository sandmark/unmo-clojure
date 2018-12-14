(ns unmo.core
  (:gen-class))
(defn- format-response
  "Responder からの結果を整形して返す。"
  [{:keys [responder response]}]
  (let [responder-name (-> responder (name) (clojure.string/capitalize))]
    (str responder-name "> " response)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
