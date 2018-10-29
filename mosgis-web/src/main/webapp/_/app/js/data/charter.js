define ([], function () {

    $_DO.choose_tab_charter = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('charter.active_tab', name)
            
        use.block (name)
            
    }            

    return function (done) {        

        query ({type: 'charters', part: 'vocs', id: undefined}, {}, function (data) {

            add_vocabularies (data, data)

            query ({type: 'charters'}, {}, function (d) {

                var it = data.item = d.item
                
                it.last_termination = d.last_termination

                it._can = {}

                if (it.uuid_org == $_USER.uuid_org || $_USER.role.admin) {

                    switch (it.id_ctr_status) {

                        case 10:
                        case 11:
                            it._can.edit    = 1
                            it._can.approve = 1
                            if (it.id_ctr_status_gis == 40 && !it.is_annuled) it._can.annul = 1
                            break
                        case 40:
                        case 14:
                        case 34:
                        case 90:
                            it._can.alter   = 1
                            break;

                    }
                    
                    switch (it.id_ctr_status) {
                        case 11:
                        case 40:
                            it._can.annul   = 1
                            break;
                    }
                                        
                    if (it.id_ctr_state_gis == 50) {
                    
                        switch (it.id_ctr_status) {
                            case 40:
                                it._can.rollover = 1
                                break;
                        }
                        
                    }

                    switch (it.id_ctr_status) {
                        case 40:
                        case 94:
                            it._can.terminate = 1
                            break;
                    }
                    
                    switch (it.id_ctr_status) {
                        case 10: // Project
                        case 14: // _failed_placing
                            break;
                        default:
                            switch ((0 + it.id_ctr_status) % 10) {
                                case 2: // _pending_rq_...
                                case 3: // _pending_rp_...
                                    break;
                                default:
                                    it._can.refresh = 1
                                    it._can.reload = 1
                            }
                    }                    

                    if (!it.contractobjectversionguid) it._can.delete = 1

                    it._can.update = it._can.cancel = it._can.edit

                }

                $('body').data ('data', data)                

                done (data) 
            
            })

        })

    }

})