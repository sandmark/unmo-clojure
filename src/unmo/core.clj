(ns unmo.core
  (:gen-class)
  (:require [unmo.responder :refer [response]]
            [unmo.dictionary :refer [study]]
            [environ.core :refer [env]]
            [bigml.sampling [simple :as simple]]))

(def ^{:private true
       :doc "デフォルトで使用される辞書ファイル名"}
  dictionary-file
  "dict.clj")

(defn- rand-responder
  "確率によって変動するResponderを返す。
  :what   10%
  :random 90%"
  []
  (-> [:what :random]
      (simple/sample :weigh {:what 0.1 :random 0.9})
      (first)))

(defn- format-response
  "Responder からの結果を整形して返す。"
  [{:keys [responder response error]}]
  (let [responder-name (-> responder (name) (clojure.string/capitalize))]
    (if error
      (str responder-name "> 警告: " (:message error))
      (str responder-name "> " response))))

(defn -main
  "標準入力からユーザーの発言を受け取り、Responder の結果を表示して繰り返す。"
  [& args]
  (println (format "Unmo version %s launched." (:unmo-version env)))
  (print "> ")
  (flush)

  (loop [input (read-line) dictionary {}]
    (if (clojure.string/blank? input)
      (println "Quit.")
      (let [res (-> {:input input :responder (rand-responder) :dictionary dictionary}
                    (response)
                    (format-response))]
        (println res)
        (print "> ")
        (flush)
        (recur (read-line) (study dictionary input))))))
