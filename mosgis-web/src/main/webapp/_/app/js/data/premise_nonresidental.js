define ([], function () {

    $_DO.choose_tab_premise_nonresidental = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('premise_nonresidental.active_tab', name)
            
        use.block (name)
            
    }            

    return function (done) {        

        query ({type: 'premises_nonresidental'}, {}, function (data) {

            add_vocabularies (data, {
                "vc_nsi_17": 1,
                "vc_nsi_330": 1,
            })
            
            data.item.label = data.item.premisesnum

            data.active_tab = localStorage.getItem ('premise_nonresidental.active_tab') || 'premise_nonresidental_common'

            data.item.address = data.item ['tb_houses.address']
                                                                        
            $('body').data ('data', data)

            done (data)        
            
        })
        
    }

})