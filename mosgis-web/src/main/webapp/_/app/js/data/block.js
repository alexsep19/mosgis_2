define ([], function () {

    $_DO.choose_tab_block = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('block.active_tab', name)
            
        use.block (name)
            
    }            

    return function (done) {        

        query ({type: 'blocks'}, {}, function (data) {

            add_vocabularies (data, {
                "tb_entrances": 1,
                "vc_nsi_14": 1,
                "vc_nsi_30": 1,
                "vc_nsi_273": 1,
                "vc_nsi_330": 1,
            })

            
            data.item.label = '№ ' + data.item.blocknum

            data.item.address = data.item ['tb_houses.address']
                                                                        
            $('body').data ('data', data)

            done (data)        
            
        })
        
    }

})