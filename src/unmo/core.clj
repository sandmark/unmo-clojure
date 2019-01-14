(ns unmo.core
  (:gen-class)
  (:require [unmo.responder :refer [response]]
            [unmo.dictionary :refer [study save-dictionary load-dictionary]]
            [unmo.morph :refer [analyze]]
            [environ.core :refer [env]]
            [bigml.sampling [simple :as simple]]))

(def ^{:private true
       :doc "デフォルトで使用される辞書ファイル名"}
  dictionary-file
  "dict.clj")

(defn- rand-responder
  "確率によって変動するResponderを返す。
  :what     10%
  :random   20%
  :pattern  30%
  :template 20%
  :markov   20%"
  []
  (-> [:what :random :pattern :template :markov]
      (simple/sample :weigh {:what     0.1
                             :random   0.2
                             :pattern  0.3
                             :template 0.2
                             :markov   0.2})
      (first)))

(defn- format-response
  "Responder からの結果を整形して返す。"
  [{:keys [responder response error]}]
  (let [responder-name (-> responder (name) (clojure.string/capitalize))]
    (if error
      (str responder-name "> 警告: " (:message error))
      (str responder-name "> " response))))

(defn- dialogue
  "ユーザーからの発言、形態素解析結果、辞書を受け取り、AIの思考結果を整形した文字列を返す。"
  ([input parts dictionary]
   (dialogue input parts dictionary (rand-responder)))
  ([input parts dictionary responder]
   (let [res (-> {:input input
                  :dictionary dictionary
                  :parts parts
                  :responder responder}
                 (response))]
     (case (get-in res [:error :type])
       :fatal (format-response res)
       nil    (format-response res)
       (recur input parts dictionary :random)))))

(defn -main
  "標準入力からユーザーの発言を受け取り、Responder の結果を表示して繰り返す。"
  [& args]
  (println (format "Unmo version %s launched." (:unmo-version env)))
  (print "> ")
  (flush)

  (loop [input (read-line)
         dictionary (load-dictionary dictionary-file)]
    (if (clojure.string/blank? input)
      (do (println "Saving dictionary...")
          (save-dictionary dictionary dictionary-file)
          (println "Quit."))
      (let [parts (analyze input)
            res (dialogue input parts dictionary)]
        (println res)
        (print "> ")
        (flush)
        (recur (read-line) (study dictionary input parts))))))
