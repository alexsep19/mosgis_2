define ([], function () {

    $_DO.choose_tab_interval = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('interval.active_tab', name)
            
        use.block (name)
            
    }            

    return function (done) {

        query ({type: 'intervals'}, {}, function (data) {        

            add_vocabularies (data, {
                vc_gis_status: 1,
                vc_nsi_3: 1,
                vc_nsi_239: 1,
                vc_actions: 1,
            })
            
            var it = data.item
            
            it._can = {cancel: 1}

            if (!it.is_deleted && it['sr_ctr.uuid_org'] == $_USER.uuid_org) {

                switch (it ['sr_ctr.id_ctr_status']) {

                        case 11:
                        case 40:
                        case 42:
                        case 43:

                        case 34:
                        case 11:
                        case 92:
                        case 93:
                        case 94:
                        case 100:

                        switch (it.id_ctr_status) {
                            case 10:
                            case 11:
                                it._can.edit = 1                            
                                it._can.approve = 1                            
                        }
                        
                        switch (it.id_ctr_status) {
                            case 14:
                            case 34:
                            case 40:
                                it._can.alter = 1
                        }

                        switch (it.id_ctr_status) {
                            case 10:
                            case 14:
                                it._can.delete = 1
                        }
                        
                }            
                
                it._can.update = it._can.edit

            }            

            $('body').data ('data', data)
    
            done (data)

        }) 
        
    }

})