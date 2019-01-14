define ([], function () {
    
    $_DO.choose_tab_planned_examination = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('planned_examination.active_tab', name)
            
        use.block (name)
            
    }            

    return function (done) { 

        query ({type: 'planned_examinations', part: 'vocs', id: undefined}, {}, function (data) {
            
            query ({type: 'planned_examinations'}, {}, function (d) {

                console.log (d.item)

                add_vocabularies (data, data)

                data.item = d.item

                data.active_tab = localStorage.getItem ('planned_examination.active_tab') || 'planned_examination_common'

                var perms = 1 - data.item.sign && !data.item.is_deleted
                data.item._can = {
                    edit: perms,
                    delete: perms,
                    update: perms,
                    cancel: perms,
                }
                                                    
                $('body').data ('data', data)

                done (data)

            })
            
        })
    }

})