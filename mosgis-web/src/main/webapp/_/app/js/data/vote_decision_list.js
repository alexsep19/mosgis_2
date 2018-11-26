define ([], function () {

    $_DO.choose_tab_vote_decision_list = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('vote_decision_list.active_tab', name)
            
        use.block (name)
            
    }            

    return function (done) {       

        query ({type: 'vote_decision_lists', part: 'vocs', id: undefined}, {}, function (data) {

            if (data.vc_nsi_63) {    
                
                data.vc_nsi_63.forEach ((element, i, arr) => {
                    if (element['id'].indexOf ('.') < 0) element['fake'] = 1
                })

            }

            add_vocabularies (data, data)

            query ({type: 'vote_decision_lists'}, {}, function (d) {

                data.item = d.item
                data.cach = d.cach

                $('body').data ('data', data)
    
                done (data)

            })

        }) 
        
    }

})