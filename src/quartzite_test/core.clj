(ns quartzite-test.core
  (:gen-class)
  (:require [clojurewerkz.quartzite.jobs :as jobs]
            [clojurewerkz.quartzite.scheduler :as scheduler]
            [clojurewerkz.quartzite.triggers :as triggers]))

(jobs/defjob DurableJob [ctx]
   (println "I'M IN UR DATABASE DROPPN UR USERS"))

(def my-job-detail
  (jobs/build
    (jobs/of-type DurableJob)
    (jobs/store-durably)
    (jobs/with-identity
      (jobs/key "jobs.durable"))))

(def my-trigger
  (triggers/build
    (triggers/with-identity (triggers/key "triggers.durable"))
    (triggers/start-now)))

(def my-scheduler
  (scheduler/initialize))

(defn -main []
  (scheduler/start quartzite-test.core/my-scheduler)
  (scheduler/schedule my-scheduler my-job-detail my-trigger))