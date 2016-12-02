(ns submitit.email
  (:use [submitit.base])
  (:require [taoensso.timbre :as timbre])
  )

(defn generate-mail-talk-mess [talk-result]
  (if (talk-result :submitError)
    (str "Pga. en feil så kan vi ikke vurdere presentasjonen din nå. Du vil motta en e-post når feilen er rettet. Feil " (talk-result :submitError))
    (str "Du kan redigere din presentasjon på " (read-setup :serverhostname) "/talkDetail?talkid=" (talk-result :resultid))))


(defn speaker-mail-list [talk]
  (map #(% "email") (talk "speakers")))

(defn handle-arr [template tkey tvalue]
  (if (re-find (re-pattern (str "%a" tkey "%")) template)
    (clojure.string/replace template (re-pattern (str "%a" tkey "%")) (if (empty? tvalue) 
      "None selected" (reduce (fn [a b] (str a ", " b)) tvalue)))
    template
    )
  )

(defn create-mail-sender [subject message]
  (if (= "true" (read-setup :mailSsl))
  (doto (org.apache.commons.mail.SimpleEmail.)
      (.setHostName (read-setup :hostname))
      (.setSslSmtpPort (read-setup :smtpport))
      (.setSSL true)
      (.setFrom (read-setup :mailFrom) "Programkomiteen for Sikkerhet og Sårbarhet")
      (.setSubject subject)
      (.setAuthentication (read-setup :user) (read-setup :password))
      (.setMsg message)
      (.setCharset "ISO-8859-15")
      )
  (doto (org.apache.commons.mail.SimpleEmail.)
      (.setHostName (read-setup :hostname))
      (.setSmtpPort (Integer/parseInt (read-setup :smtpport)))
      (.setFrom (read-setup :mailFrom) "Programkomiteen for Sikkerhet og Sårbarhet")
      (.setSubject subject)
      (.setMsg message)
      (.setCharset "ISO-8859-15")
      )

  ))


(defn send-mail [send-to subject message]
  (try
  (let [sender (create-mail-sender subject message)]
    (doseq [sto send-to] (.addTo sender sto))
    (.addBcc sender (read-setup :mailBcc))
    (.send sender))
    (catch Exception e (do
                                (timbre/error (str "Error sending mail " (.getMessage e)))
                                (throw e))
    )))


(defn replace-vector [generate-mail-text template value-vector result]
  (if (empty? value-vector)
    result
    (replace-vector generate-mail-text template (rest value-vector) (str result (generate-mail-text template (first value-vector))))
  )
)

(defn handle-template [generate-mail-text template tkey value-vector]
  (let [temp-no-lf (clojure.string/join "%newline%" (clojure.string/split template #"\n"))]
  (let [inner-template (re-find (re-pattern (str "%t" tkey "(.*)t%")) temp-no-lf)]
    (timbre/trace "inner template" inner-template)
    (if (nil? inner-template) template
      (clojure.string/join "\n" (clojure.string/split
        (clojure.string/replace temp-no-lf (re-pattern (str "%t" tkey ".*t%"))
        (replace-vector generate-mail-text (inner-template 1) value-vector ""))
        #"%newline%"))
  ))))

(defn replace-me [s text replace-with]
  (timbre/trace "replace-me " s ";" text ";" replace-with)
  (if (string? s)
  (let [index (if (or (nil? s) (nil? text)) -1 (.indexOf s text))]
  (if (= -1 index) s (replace-me (str (.substring s 0 index) replace-with (.substring s (+ index (count text)))) text replace-with)
  ))
  s))



(defn generate-mail-text [template value-map]
  (timbre/trace "** gen-mail-text **")
  (if (empty? value-map) template
    (let [[tkey tvalue] (first value-map)]
      (timbre/trace "Mailrep " tkey "+++" tvalue)
      (generate-mail-text
      (clojure.string/replace
       (handle-arr 
        (handle-template generate-mail-text template tkey tvalue) 
        tkey tvalue) 
       (re-pattern (str "%" tkey "%")) (replace-me (replace-me tvalue "$" "&#36;") "&#36;" "\\$"))
      (dissoc value-map tkey)))))

