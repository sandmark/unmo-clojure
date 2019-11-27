(ns unmo.core-test
  (:require [clojure.string :as str]
            [clojure.test :as t]
            [unmo.core :as sut]))

(t/deftest load-dictionary-test
  (t/testing "Return {} if the file doesn't exist"
    (t/is (= {} (sut/load-dictionary "dummy.txt")))))

(t/deftest dialogue-test
  (t/testing "Call what-responder when the result of another responder is nil"
    (t/is (str/starts-with? (#'sut/dialogue "hello" [] {} :markov)
                            "What> "))))

(t/deftest rand-responder-test
  (t/testing "Returns one of the responder functions"
    (let [res (#'sut/rand-responder)]
      (t/is (keyword? res))
      (t/is (some #{res}
                  #{:what :random :pattern :template :markov})))))
