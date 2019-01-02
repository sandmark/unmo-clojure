(ns unmo.responder)

(defmulti response
  "渡された発言オブジェクトに対する返答を :response キーに設定して返す。どの思考エンジンが使用されるかは :responder キーの値で変わる。"
  :responder)

(defmethod
  ^{:doc "PatternResponderは入力inputに正規表現でマッチするパターンを探し、そのうちランダムなものを返す。"}
  response :pattern [{:keys [input dictionary] :as params}]
  (letfn [(match? [[pattern phrases]]
            (-> (re-pattern pattern) (re-find input)))]
    (if-let [[pattern phrases] (->> (:pattern dictionary) (filter match?) (first))]
      (let [match  (-> (re-pattern pattern) (re-find input) (first))
            phrase (rand-nth phrases)
            text   (clojure.string/replace phrase #"%match%" match)]
        (assoc params :response text))
      (assoc params :error {:type :no-match :message "パターンがありません"}))))

(defmethod
  ^{:doc "RandomResponderは入力に関係なく、:dictionary -> :random に定義されたVectorからランダムな値を返す。"}
  response :random [{:keys [dictionary] :as params}]
  (let [random (:random dictionary)]
    (if (empty? random)
      (assoc params :error {:message "ランダム辞書が空です。"})
      (assoc params :response (rand-nth random)))))

(defmethod
  ^{:doc "WhatResponderは入力 :input に対し、常に 'inputってなに？' と返す。"}
  response :what [{:keys [input] :as params}]
  (->> (str input "ってなに？")
       (assoc params :response)))

(defmethod
  ^{:doc "Responderの指定がない、もしくは存在しないResponderを指定された場合、IllegalArgumentException例外を投げる。"}
  response :default [{:keys [responder]}]
  (throw (IllegalArgumentException.
          (str "Responder " responder " が存在しません。"))))
