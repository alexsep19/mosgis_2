define ([], function () {

    $_DO.choose_tab_premise_residental = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('premise_residental.active_tab', name)
            
        use.block (name)
            
    }            

    return function (done) {        

        query ({type: 'premises_residental'}, {}, function (data) {

            add_vocabularies (data, {
                "tb_entrances": 1,
                "vc_nsi_30": 1,
                "vc_nsi_273": 1,
                "vc_nsi_330": 1,
            })

            var type = data.vc_nsi_30 [data.item.code_vc_nsi_30] || 'помещение'
            
            var prefix = type.indexOf ('вартира') < 0 ? type : 'кв.'
            
            data.item.label = prefix + ' ' + data.item.premisesnum

            data.item.address = data.item ['tb_houses.address']
                                                                        
            $('body').data ('data', data)

            done (data)        
            
        })
        
    }

})