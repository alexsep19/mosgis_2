define ([], function () {
    
    $_DO.choose_tab_check_plan = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('check_plan.active_tab', name)
            
        use.block (name)
            
    }            

    return function (done) { 

        query ({type: 'check_plans', part: 'vocs', id: undefined}, {}, function (data) {
            
            query ({type: 'check_plans'}, {}, function (d) {

                add_vocabularies (data, data)

                data.item = d.item

                data.active_tab = localStorage.getItem ('check_plan.active_tab') || 'check_plan_common'

                data.item._can = {

                    edit: 1 - data.item.sign,
                    delete: 1 - data.item.sign,

                }
                                                    
                $('body').data ('data', data)

                done (data)

            })
            
        })
    }

})