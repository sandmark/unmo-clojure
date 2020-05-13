(ns unmo.morph-test
  (:require [unmo.morph :as sut]
            [clojure.test :as t]
            [fudje.sweet :as fs]))

(defn morph-system-fixture [f]
  (sut/restart)
  (f)
  (sut/stop))

(t/use-fixtures :once morph-system-fixture)

(defn first-noun [parts]
  (-> parts first second))

(defmacro nouns-are [pred & nouns]
  `(t/are [x#] (~pred (first-noun (sut/analyze x#)))
     ~@nouns))

(t/deftest analysis-test
  (t/testing "Count of words"
    (t/is (= 6
             (count (sut/analyze "あたしはプログラムの女の子です"))))
    (t/is (= 10
             (count (sut/analyze "あたしが好きなのはプログラムと月餅です")))))

  (t/testing "Structure"
    (t/is
     (compatible
      (sut/analyze "あたしはプログラムの女の子です")
      (fs/contains-in [[(fs/checker string?) (fs/checker boolean?)]])))))

(t/deftest noun?-test
  (t/testing "Count of nouns"
    (t/are [x y] (= x
                    (count (filter sut/noun? (sut/analyze y))))
      2 "あたしはプログラムの女の子です"
      3 "好きなものはプログラムと月餅です"
      3 "今日の天気は晴れです")))

(t/deftest nouns-to-use-test
  (t/testing "一般名詞"
    (nouns-are true? "女の子" "プログラム" "月餅"))

  (t/testing "固有名詞"
    (nouns-are true? "ビートルズ" "ノーベル"))

  (t/testing "サ変接続"
    (nouns-are true? "該当" "写経"))

  (t/testing "副詞可能"
    (nouns-are true? "月曜日" "ところ"))

  (t/testing "形容動詞語幹"
    (nouns-are true? "きらびやか" "無限" "好き")))

(t/deftest nouns-not-to-use-test
  (t/testing "数"
    (nouns-are false? "一" "1" "万"))

  (t/testing "特殊"
    (nouns-are false? "そう" "そ"))

  (t/testing "代名詞"
    (nouns-are false? "私" "あなた" "彼"))

  (t/testing "非自立"
    (nouns-are false? "かぎり" "ため" "以下"))

  (t/testing "接続詞的"
    (nouns-are false? "ＶＳ" "対" "兼"))

  (t/testing "ナイ形容詞語幹"
    (nouns-are false? "味け" "だらし")))
