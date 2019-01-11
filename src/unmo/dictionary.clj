(ns unmo.dictionary
  (:require [unmo.util :refer [conj-unique file-exists?]]
            [unmo.morph :refer [noun?]]
            [fipp.edn :refer [pprint] :rename {pprint fipp}]))

(defn- parts->markov
  "形態素解析結果をマルコフ辞書形式に変換する。"
  ([dictionary [[prefix1 _] [prefix2 _] & parts]]
   (->> parts
        (map first)
        (parts->markov dictionary prefix1 prefix2)))

  ([dictionary prefix1 prefix2 [suffix & rest]]
   (if (not suffix)
     (update-in dictionary [prefix1 prefix2] conj-unique "%ENDMARK%")
     (-> dictionary
         (update-in [prefix1 prefix2] conj-unique suffix)
         (recur prefix2 suffix rest)))))

(defn- study-template
  "形態素解析結果に基づき、名詞の数をキー、名詞を%noun%に置き換えた発言のリストを値として学習する。
  重複、ないし名詞が無い発言は学習しない。
  学習した結果をdictionaryの:templateに定義して返す。"
  [dictionary parts]
  (letfn [(->noun [[word _ :as part]]
            (if (noun? part)
              "%noun%"
              word))]
    (let [nouns-count (->> parts (filter noun?) (count))
          template    (->> parts (map ->noun)   (apply str))]
      (if (zero? nouns-count)
        dictionary
        (update-in dictionary [:template nouns-count] conj-unique template)))))

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
  "文字列inputと形態素解析結果partsを受け取り、辞書dictionaryに保存したものを返す。"
  [dictionary input parts]
  (-> dictionary
      (study-random input)
      (study-pattern input parts)
      (study-template parts)))

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
