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
                "vc_house_status": 1,
                "vc_nsi_14": 1,
                "vc_nsi_273": 1,
                "vc_nsi_330": 1,
            })
            
            data.item.label = data.item.roomnumber
            data.item.premise_label = data.item ['tb_premises_res.premisesnum']
            data.item.block_label = data.item ['tb_blocks.blocknum']

            data.item.address = data.item ['tb_houses.address']

            it = data.item

            it._can = {}

            if (($_USER.role.admin || (data.cach && data.cach.is_own) || is_own_srca(data) && it.uuid_org == $_USER.uuid_org) && !it.is_deleted) {
                it._can.edit   = 1 - it.is_annuled
                it._can.delete = it._can.update = it._can.cancel = it._can.edit                
                it._can.annul = 1 ? !data.item.is_annuled && data.item.id_status == 20 : 0
                it._can.restore = it.is_annuled_in_gis
            }
                                                                        
            $('body').data ('data', data)

            done (data)        
            
        })
        
    }

})