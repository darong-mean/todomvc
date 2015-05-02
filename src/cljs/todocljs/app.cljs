(ns todocljs.app
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [dispatch dispatch-sync
                                   register-handler subscribe
                                   register-sub
                                   path debug]]))

;; MODEL ----------------------

(def default-db {:tasks   {}
                 :showing :all})

(defn task-seq [db]
  (when (seq (:tasks db)) (vals (:tasks db))))

(register-sub
  :tasks
  (fn [db _]
    (reaction (filterv
                (fn [task] (case (:showing @db)
                             :completed (:done task)
                             :active (not (:done task))
                             true))
                (task-seq @db)))))

(register-sub :task-status
  (fn [db _]
    (reaction {:active-count (count (remove :done (task-seq @db)))
               :total-count  (count (task-seq @db))
               :showing      (:showing @db)})))

;; UPDATE ---------------------

; UTILS ------------------
(def tasks-middle-ware [(path :tasks) debug])

(defn next-id
  [todos]
  ((fnil inc 0) (last (keys todos))))

; HANDLERS ---------------
(register-handler                                           ;; disptached to on app startup
  :initialise-db                                            ;; event id being handled
  (fn [_ _]                                                 ;; the handler
    default-db))

(register-handler :add-task tasks-middle-ware
  (fn [db [_ text]]
    (let [id (next-id db)]
      (assoc db id {:id id :text text :done false}))))

(register-handler :set-showing [(path :showing) debug]
  (fn [db [_ showing-mode]]
    showing-mode))

(register-handler :toggle-task tasks-middle-ware
  (fn [tasks [_ task]]
    (update-in tasks [(:id task) :done] not)))

(register-handler :delete-task tasks-middle-ware
  (fn [tasks [_ task]]
    (dissoc tasks (:id task))))

(register-handler :clear-completed-task tasks-middle-ware
  (fn [tasks [_]]
    (->>
      (vals tasks)
      (filter :done)
      (map :id)
      (reduce dissoc tasks))))

(register-handler :toggle-all tasks-middle-ware
  (fn [tasks [_]]
    (let [new-done (not-every? :done (vals tasks))]         ;; toggle true or false?
      (reduce #(assoc-in %1 [%2 :done] new-done)
        tasks
        (keys tasks)))))

;; VIEWS ----------------------

(defn view-header-input [{:keys [on-save]}]
  (let [val  (reagent/atom "")
        stop #(reset! val "")
        save #(let [v (-> @val str clojure.string/trim)]
               (if-not (empty? v) (on-save v))
               (stop))]
    (fn [props]
      [:input.new-todo (merge props
                         {; :autofocus true ; autofocus attribute not support yet?
                          :type        "text"
                          :value       @val
                          :on-blur     save
                          :on-change   #(reset! val (-> % .-target .-value))
                          :on-key-down #(case (.-which %)
                                         13 (save)          ; key ENTER
                                         27 (stop)          ; key ESCAPE
                                         nil)
                          })])))

;; This section should be hidden by default and shown when there are todos
(defn view-main [dispatch tasks]
  (when (seq tasks)
    [:section.main
     [:input.toggle-all {:type "checkbox" :on-change #(dispatch [:toggle-all])}]
     [:label {:for "toggle-all"} "Mark all as complete"]
     [:ul.todo-list
      ; List items should get the class `editing` when editing and `completed` when marked as completed
      (for [task tasks] ^{:key (:id task)}
                        [:li {:class (when (:done task) "completed")}
                         [:div.view
                          [:input.toggle {:type      "checkbox" :checked (:done task)
                                          :on-change #(dispatch [:toggle-task task])}]
                          [:label (:text task)]
                          [:button.destroy {:on-click #(dispatch [:delete-task task])}]]
                         [:input.edit {:value "Create a Todo MVC Template"}]])]]))

;; This footer should hidden by default and shown when there are todos
(defn view-footer [dispatch status]
  (when (> (:total-count status) 0)
    [:footer.footer
     [:span.todo-count [:strong (:active-count status)] " item left"] ; This should be `0 items left` by default
     [:ul.filters                                           ; Remove this if you don't implement routing
      [:li [:a {:class    (when (= (:showing status) :all) "selected")
                :href     "#"
                :on-click #(dispatch [:set-showing :all])} "All"]]
      [:li [:a {:class    (when (= (:showing status) :active) "selected")
                :href     "#"
                :on-click #(dispatch [:set-showing :active])} "Active"]]
      [:li [:a {:class    (when (= (:showing status) :completed) "selected")
                :href     "#"
                :on-click #(dispatch [:set-showing :completed])} "Completed"]]]
     [:button.clear-completed {:on-click #(dispatch [:clear-completed-task])} "Clear completed"] ; Hidden if no completed items are left
     ]))

(defn view-container [dispatch {:keys [tasks status]}]
  [:section.todoapp
   [:header.header
    [:h1 "todos"]
    [view-header-input {:placeholder "What's need to be done?"
                        :on-save     #(dispatch [:add-task %])}]]
   [view-main dispatch tasks]
   [view-footer dispatch status]])

;; MAIN ---------------------
(defn main []
  (let [status (subscribe [:task-status])
        tasks  (subscribe [:tasks])]
    (fn [] [view-container dispatch {:tasks  @tasks
                                     :status @status}])))

(defn start-application []
  (reagent/render-component [main]
    (.getElementById js/document "app")))

(set! (.-onload js/window) #(do (dispatch-sync [:initialise-db])
                                (start-application)))
