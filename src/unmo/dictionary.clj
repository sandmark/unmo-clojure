(ns unmo.dictionary
  (:require [unmo.util :refer [conj-unique]]
            [fipp.edn :refer [pprint] :rename {pprint fipp}]))

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
