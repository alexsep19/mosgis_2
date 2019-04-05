define ([], function () {

    $_DO.choose_tab_overhaul_regional_program_house = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('overhaul_regional_program_house.active_tab', name)
            
        use.block (name)
            
    }            

    return function (done) {

        query ({type: 'overhaul_regional_program_houses'}, {}, function (data) {

            add_vocabularies (data, {
                vc_gis_status: 1,
                vc_actions: 1,
                vc_nsi_218: 1,
                vc_oh_wk_types: 1
            })
            
            var it = data.item

            it._can = {cancel: 1}

            if (!it.is_deleted && !it['program.is_deleted'] &&
                ($_USER.role.admin || it['program.org_uuid'] == $_USER.uuid_org)) {

                switch (it['program.last_succesfull_status']) {
                    case  10:
                    case -31:
                    case -21:
                        it._can.edit = 1
                }

                it._can.update = it._can.edit

            }

            $('body').data ('data', data)
    
            done (data)

        }) 
        
    }

})