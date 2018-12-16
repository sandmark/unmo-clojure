(ns unmo.dictionary
  (:require [unmo.util :refer [conj-unique]]))

(defn- study-random
  "文字列inputを辞書dictionaryの:randomベクタに追加して返す。重複は追加しない。"
  [dictionary input]
  (update dictionary :random conj-unique input))

(defn study
  "文字列inputを辞書dictionaryに保存したものを返す。"
  [dictionary input]
  (study-random dictionary input))
