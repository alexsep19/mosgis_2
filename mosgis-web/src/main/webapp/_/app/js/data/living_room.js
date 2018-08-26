define ([], function () {

    $_DO.choose_tab_living_room = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('living_room.active_tab', name)
            
        use.block (name)
            
    }            

    return function (done) {        

        query ({type: 'living_rooms'}, {}, function (data) {

            add_vocabularies (data, {
                "tb_entrances": 1,
                "vc_nsi_14": 1,
                "vc_nsi_273": 1,
                "vc_nsi_330": 1,
            })
            
            data.item.label = data.item.roomnumber
            data.item.premise_label = data.item ['tb_premises_res.premisesnum']
            data.item.block_label = data.item ['tb_blocks.blocknum']

            data.item.address = data.item ['tb_houses.address']
                                                                        
            $('body').data ('data', data)

            done (data)        
            
        })
        
    }

})