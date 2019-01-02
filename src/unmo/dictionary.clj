(ns unmo.dictionary
  (:require [unmo.util :refer [conj-unique file-exists?]]
            [unmo.morph :refer [noun?]]
            [fipp.edn :refer [pprint] :rename {pprint fipp}]))

(defn- study-pattern
  "形態素解析結果に基づき、名詞をキー、発言のベクタを値として学習する。重複は学習しない。
   学習した結果をdictionaryの:patternに定義して返す。"
  [dictinoary input parts]
  (let [nouns (->> parts (filter noun?) (map first))
        merge-unique (partial merge-with (comp distinct concat))
        make-pattern #(update %1 %2 conj-unique input)]
    (->> nouns
         (reduce make-pattern {})
         (update-in dictinoary [:pattern] merge-unique))))

(defn- study-random
  "文字列inputを辞書dictionaryの:randomベクタに追加して返す。重複は追加しない。"
  [dictionary input]
  (update dictionary :random conj-unique input))

(defn study
  "文字列inputを辞書dictionaryに保存したものを返す。"
  [dictionary input]
  (study-random dictionary input))

(defn save-dictionary
  "辞書dictionaryをpprintし、指定されたファイルに保存する。"
  [dictionary filename]
  (let [data (with-out-str (fipp dictionary))]
    (spit filename data :encoding "UTF-8")))

(defn load-dictionary
  "指定されたファイルから辞書をロードして返す。"
  [filename]
  (if (file-exists? filename)
    (-> filename (slurp :encoding "UTF-8") (read-string))
    {}))
