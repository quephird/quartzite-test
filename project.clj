(defproject quartzite-test "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :java-source-paths ["src/java"]
  ;:aot [quartzite-test.MyDynamicClassLoader]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clojurewerkz/quartzite "2.0.0"]
                 [org.slf4j/slf4j-api "1.7.22"]
                 [org.slf4j/slf4j-log4j12 "1.7.22"]
                 [org.postgresql/postgresql "9.4.1212.jre7"]])
