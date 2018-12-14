(ns unmo.responder)

(defmulti response :responder)

(defmethod response :what [{:keys [input] params}]
  (str input "ってなに？"))

(defmethod response :default [params]
  (throw (IllegalArgumentException.
          (str "Responder " (params :responder) " が存在しません。"))))
