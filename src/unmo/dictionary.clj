(ns unmo.dictionary
  (:require [unmo.util :as util]
            [unmo.morph :as morph]))

(defn- parts->markov
  "形態素解析結果をマルコフ辞書形式に変換する。"
  ([dictionary [[prefix1 _] [prefix2 _] & parts]]
   (->> parts
        (map first)
        (parts->markov dictionary prefix1 prefix2)))

  ([dictionary prefix1 prefix2 [suffix & rest]]
   (if (not suffix)
     (update-in dictionary [prefix1 prefix2] util/conj-unique "%ENDMARK%")
     (-> dictionary
         (update-in [prefix1 prefix2] util/conj-unique suffix)
         (recur prefix2 suffix rest)))))

(defn- study-markov
  "形態素解析結果から文節のつながりを記録し、学習する。
  実装を簡単にするため、3単語以上で構成された文章のみ学習する。"
  [dictionary parts]
  (if (< (count parts) 3)
    dictionary
    (let [[start _] (first parts)]
      (-> dictionary
          (update-in [:markov :starts start] (fnil inc 0))
          (update-in [:markov :dictionary] parts->markov parts)))))

(defn- study-template
  "形態素解析結果に基づき、名詞の数をキー、名詞を%noun%に置き換えた発言のリストを値として学習する。
  重複、ないし名詞が無い発言は学習しない。
  学習した結果をdictionaryの:templateに定義して返す。"
  [dictionary parts]
  (letfn [(->noun [[word _ :as part]]
            (if (morph/noun? part)
              "%noun%"
              word))]
    (let [nouns-count (->> parts (filter morph/noun?) (count))
          template    (->> parts (map ->noun)   (apply str))]
      (if (zero? nouns-count)
        dictionary
        (update-in dictionary [:template nouns-count] util/conj-unique template)))))

(defn- study-pattern
  "Returns a new map with the input and the parts added to a map,
  referred by :pattern key. The pattern dictionary is formed
  {noun [sentences-includes-noun...]}

  For instance:
      if the `input` was あたしはプログラムの女の子です and
      if the `parts` was [[プログラム 名詞] [女の子 名詞]] roughly, then

      result will be {:pattern {プログラム [あたしはプログラムの女の子です]
                                女の子    [あたしはプログラムの女の子です]}}

  Note that `parts` could have more informations though
  this function handles only nouns, not verbs nor others.
  When the `input` has a noun which is already in the dictionary,
  it'll be added to the value vector, keeping uniqueness.
  "
  [dictionary input parts]
  (let [nouns (->> parts (filter morph/noun?) (mapv first))]  ; this must be vector for `reduce-kv`
    (letfn [(update-noun [m _ v]
              (update m v (fnil util/conj-unique []) input))]
      (update dictionary :pattern #(reduce-kv update-noun % nouns)))))

(defn- study-random
  "Returns a new map with the input added to a vector, referred by :random key.
  If the input already exists, just returns dictionary.

  (study-random {:random []}
                 こんにちは)              => {:random [こんにちは]}

  (study-random {:random [こんにちは]}
                 こんにちは)              => {:random [こんにちは]}
  "
  [dictionary input]
  (update dictionary :random (fnil util/conj-unique []) input))

(defn study
  "文字列inputと形態素解析結果partsを受け取り、辞書dictionaryに保存したものを返す。"
  [dictionary input parts]
  (-> dictionary
      (study-random input)
      (study-pattern input parts)
      (study-template parts)
      (study-markov parts)))
