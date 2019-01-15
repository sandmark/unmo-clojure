(ns unmo.util-test
  (:require [clojure.test :refer :all]
            [unmo.util :refer :all]))

(deftest conj-unique-test
  (testing "conj-uniqueは"
    (testing "要素をコレクションに追加する"
      (is (= [:x]
             (conj-unique [] :x))))

    (testing "コレクションに存在する要素は追加しない"
      (is (= [:x]
             (conj-unique [:x] :x))))))

(deftest file-exists?-test
  (testing "file-exists?は"
    (testing "ファイルが存在しない場合falseを返す"
      (is (false? (file-exists? "dummy.txt"))))

    (testing "ファイルが存在する場合trueを返す"
      (is (true? (file-exists? "project.clj"))))))
