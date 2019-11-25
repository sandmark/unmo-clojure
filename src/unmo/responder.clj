(ns unmo.responder
  (:require [unmo.morph :refer [noun?]]
            [clojure.string :as str]))

(defmulti response
  "渡された発言オブジェクトに対する返答を :response キーに設定して返す。どの思考エンジンが使用されるかは :responder キーの値で変わる。"
  :responder)

(defn- markov-generate
  "単語と辞書を受け取り、その単語から始まる文章をマルコフ連鎖で生成して返す。"
  ([dictionary prefix1 prefix2] (markov-generate 30 dictionary prefix1 prefix2 [prefix1 prefix2]))
  ([times dictionary prefix1 prefix2 result]
   (let [suffix (-> dictionary (get-in [prefix1 prefix2]) (rand-nth))]
     (cond (zero? times)          (apply str result)
           (= "%ENDMARK%" suffix) (apply str result)
           :else (->> (conj result suffix)
                      (recur (dec times) dictionary prefix2 suffix))))))

(defmethod
  ^{:doc "MarkovResponderは形態素解析結果partsを受け取り、最初の単語またはランダムな単語から始まる文章を生成して返す。"}
  response :markov [{:keys [parts dictionary] :as params}]
  (if (empty? (:markov dictionary))
    (assoc params :error {:type :dictionary-empty
                          :message "マルコフ辞書が空です"})
    (let [starts (get-in dictionary [:markov :starts])
          markov (get-in dictionary [:markov :dictionary])
          word   (ffirst parts)
          prefix1 (if (contains? starts word)
                    word
                    (-> starts (keys) (rand-nth)))
          prefix2 (-> (get markov prefix1) (keys) (rand-nth))]
      (assoc params :response (markov-generate markov prefix1 prefix2)))))

(defmethod
  ^{:doc "TemplateResponderは入力inputの名詞を調べ、辞書のテンプレートの%noun%をその名詞で置き換えて返す。"}
  response :template [{:keys [parts dictionary] :as params}]
  (let [nouns (->> parts (filter noun?) (map first))
        nouns-count (count nouns)
        ->response #(clojure.string/replace-first %1 #"%noun%" %2)]
    (if-let [templates (get-in dictionary [:template nouns-count])]
      (->> nouns
           (reduce ->response (rand-nth templates))
           (assoc params :response))
      (-> params
          (assoc :error {:type :no-match
                         :message "一致するテンプレートがありません。"})))))

(defn response-what
  "Returns a string with a question mark appended to the end of
  the :input value of the given map."
  [{:keys [input]}]
  (str input "？"))

(defmethod
  ^{:doc "Responderの指定がない、もしくは存在しないResponderを指定された場合、IllegalArgumentException例外を投げる。"}
  response :default [{:keys [responder]}]
  (throw (IllegalArgumentException.
          (str "Responder " responder " が存在しません。"))))
(defn response-random
  "Returns a random value from the set, :dictionary -> :random, of the given map.
  When the set is empty, returns nil."
  [{{random :random} :dictionary}]
  (-> random seq rand-nth))

(defn response-pattern
  "Searches the dictionary (:dictionary :pattern) for a pattern that
  matches the given input, and returns a random response corresponding to the pattern,
  replacing %match% with the matched string."
  [{input :input {pattern :pattern} :dictionary}]
  (letfn [(match [[r phrases]]
            (when-let [matched (-> r re-pattern (re-find input) first)]
              [matched phrases]))]
    (when-let [[matched phrases] (first (keep match pattern))]
      (let [phrase (-> phrases seq rand-nth)]
        (str/replace phrase #"%match%" matched)))))
