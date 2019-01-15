(ns unmo.morph
  (:import [com.worksap.nlp.sudachi DictionaryFactory Tokenizer$SplitMode]))

(def ^:private tokenizer
  "Sudachiの形態素解析インスタンス"
  (try
    (let [settings (slurp "sudachi_fulldict.json" :encoding "UTF-8")]
      (-> (DictionaryFactory.) (.create settings) (.create)))
    (catch java.io.FileNotFoundException e
      (println (.getMessage e))
      (println "形態素解析ライブラリSudachiの設定ファイルと辞書を用意してください。")
      (System/exit 1))))

(def ^:private split-mode
  "Sudachiの形態素解析オプション"
  {:a Tokenizer$SplitMode/A :b Tokenizer$SplitMode/B :c Tokenizer$SplitMode/C})

(defn analyze
  "与えられた文字列に対して形態素解析を行い、[形態素 表層系]のリストを返す"
  ([text] (analyze text :c))
  ([text mode]
   (letfn [(->parts [token]
             (let [parts   (->> (.partOfSpeech token) (clojure.string/join \,))
                   surface (.surface token)]
               [surface parts]))]
     (->> (.tokenize tokenizer (mode split-mode) text)
          (map ->parts)
          (filter (comp (complement empty?) first))))))

(defn noun?
  "与えられた形態素が名詞かどうかを判定する"
  [[word part]]
  (-> #"名詞,(一般|普通名詞|固有名詞|サ変接続|形容動詞語幹)" (re-find part) (boolean)))
