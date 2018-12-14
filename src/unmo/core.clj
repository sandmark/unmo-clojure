(ns unmo.core
  (:gen-class)
  (:require [unmo.responder :refer [response]]
            [environ.core :refer [env]]))

(defn- format-response
  "Responder からの結果を整形して返す。"
  [{:keys [responder response]}]
  (let [responder-name (-> responder (name) (clojure.string/capitalize))]
    (str responder-name "> " response)))

(defn -main
  "標準入力からユーザーの発言を受け取り、Responder の結果を表示して繰り返す。"
  [& args]
  (println (format "Unmo version %s launched." (:unmo-version env)))
  (print "> ")
  (flush)

  (loop [input (read-line)]
    (if (clojure.string/blank? input)
      (println "Quit.")
      (do (-> {:responder :what :input input} (response) (format-response) (println))
          (print "> ")
          (flush)
          (recur (read-line))))))
